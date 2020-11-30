/*
 * Copyright (c) 2020. Yaser Rajabi https://github.com/yrajabi
 * Based on code by https://github.com/ddekanski
 */

package com.amebo.amebo.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import pl.droidsonroids.gif.GifDrawable;

public class GlideImageGetter implements Html.ImageGetter {
    private final WeakReference<TextView> container;
    private final boolean matchParentWidth;
    private final HtmlImagesHandler imagesHandler;
    private float density = 1.0f;

    public GlideImageGetter(TextView textView) {
        this(textView, false, false, null);
    }

    public GlideImageGetter(TextView textView, boolean matchParentWidth, HtmlImagesHandler imagesHandler) {
        this(textView, matchParentWidth, false, imagesHandler);
    }

    public GlideImageGetter(TextView textView, boolean matchParentWidth, boolean densityAware,
                            @Nullable HtmlImagesHandler imagesHandler) {
        this.container = new WeakReference<>(textView);
        this.matchParentWidth = matchParentWidth;
        this.imagesHandler = imagesHandler;
        if (densityAware) {
            density = container.get().getResources().getDisplayMetrics().density;
        }
    }

    @Override
    public Drawable getDrawable(String source) {
        if (imagesHandler != null) {
            imagesHandler.addImage(source);
        }

        Drawable output;
        if (source.endsWith(".gif")) {
            GifPlaceholder drawable = new GifPlaceholder(container.get().getContext());
            container.get().post(() -> Glide.with(container.get().getContext())
                    .asFile()
                    .load(source)
                    .into(drawable));
            output = drawable;
        } else {
            BitmapDrawablePlaceholder drawable = new BitmapDrawablePlaceholder(container.get().getContext());
            container.get().post(() -> Glide.with(container.get().getContext())
                    .asBitmap()
                    .load(source)
                    .into(drawable));
            output = drawable;
        }

        return output;
    }

    private class BitmapDrawablePlaceholder extends BitmapDrawable implements Target<Bitmap> {

        protected Drawable drawable;
        private final CircularProgressDrawable progressDrawable;

        BitmapDrawablePlaceholder(@NonNull Context context) {
            super(container.get().getResources(), Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888));
            progressDrawable = AppUtil.progressDrawable(context);
        }

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            } else {
                progressDrawable.draw(canvas);
            }
        }

        private void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            int drawableWidth = (int) (drawable.getIntrinsicWidth() * density);
            int drawableHeight = (int) (drawable.getIntrinsicHeight() * density);
            int maxWidth = container.get().getMeasuredWidth();
            if ((drawableWidth > maxWidth) || matchParentWidth) {
                int calculatedHeight = maxWidth * drawableHeight / drawableWidth;
                drawable.setBounds(0, 0, maxWidth, calculatedHeight);
                setBounds(0, 0, maxWidth, calculatedHeight);
            } else {
                drawable.setBounds(0, 0, drawableWidth, drawableHeight);
                setBounds(0, 0, drawableWidth, drawableHeight);
            }

            container.get().setText(container.get().getText());
        }

        @Override
        public void onLoadStarted(@Nullable Drawable placeholderDrawable) {
            if (placeholderDrawable != null) {
                setDrawable(placeholderDrawable);
            }
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            if (errorDrawable != null) {
                setDrawable(errorDrawable);
            }
        }

        @Override
        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            if (container.get() != null) {
                setDrawable(new BitmapDrawable(container.get().getResources(), bitmap));
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholderDrawable) {
            if (placeholderDrawable != null) {
                setDrawable(placeholderDrawable);
            }
        }

        @Override
        public void getSize(@NonNull SizeReadyCallback cb) {
            cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        }

        @Override
        public void removeCallback(@NonNull SizeReadyCallback cb) {
        }

        @Override
        public void setRequest(@Nullable Request request) {
        }

        @Nullable
        @Override
        public Request getRequest() {
            return null;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onDestroy() {
        }

    }


    private class GifPlaceholder extends Drawable implements Target<File> {

        protected Drawable drawable;
        private final CircularProgressDrawable progressDrawable;

        GifPlaceholder(Context context) {
            progressDrawable = AppUtil.progressDrawable(context);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            } else {
                progressDrawable.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            if (drawable != null) {
                return drawable.getOpacity();
            }
            return progressDrawable.getOpacity();
        }

        @Override
        public void onLoadStarted(@Nullable Drawable placeholder) {
            setDrawable(progressDrawable);
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            if (errorDrawable != null) {
                setDrawable(errorDrawable);
            }
        }

        @Override
        public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
            if (container.get() != null) {
                try {
                    drawable = new GifDrawable(resource);
                    setDrawable(drawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            setDrawable(progressDrawable);
        }

        @Override
        public void getSize(@NonNull SizeReadyCallback cb) {
            cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        }

        @Override
        public void removeCallback(@NonNull SizeReadyCallback cb) {

        }

        @Override
        public void setRequest(@Nullable Request request) {

        }

        @Nullable
        @Override
        public Request getRequest() {
            return null;
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onDestroy() {

        }

        private void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            int drawableWidth = (int) (drawable.getIntrinsicWidth() * density);
            int drawableHeight = (int) (drawable.getIntrinsicHeight() * density);
            int maxWidth = container.get().getMeasuredWidth();
            if ((drawableWidth > maxWidth) || matchParentWidth) {
                int calculatedHeight = maxWidth * drawableHeight / drawableWidth;
                drawable.setBounds(0, 0, maxWidth, calculatedHeight);
                setBounds(0, 0, maxWidth, calculatedHeight);
            } else {
                drawable.setBounds(0, 0, drawableWidth, drawableHeight);
                setBounds(0, 0, drawableWidth, drawableHeight);
            }

            container.get().setText(container.get().getText());
        }
    }

    public interface HtmlImagesHandler {
        void addImage(String uri);
    }
}
