/*
 * Copyright (c) 2020. Yaser Rajabi https://github.com/yrajabi
 * Based on code by https://github.com/ddekanski
 */
package com.amebo.amebo.common

import android.content.Context
import android.content.res.Resources
import android.widget.TextView
import android.text.Html
import java.lang.ref.WeakReference
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.bumptech.glide.request.target.SizeReadyCallback
import android.graphics.ColorFilter
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil.gifProgressDrawable
import com.amebo.amebo.common.extensions.setColor
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber
import java.io.File
import java.io.IOException

class GlideImageGetter(
    textView: TextView,
    private val imageGetterState: ImageGetterState,
    private val matchParentWidth: Boolean = false,
    densityAware: Boolean = false
) : Html.ImageGetter {
    private val container: WeakReference<TextView> = WeakReference(textView)
    private val textView get() = container.get()
    private val context get() = textView?.context
    private val resources: Resources? get() = container.get()?.resources
    private val density: Float =
        if (densityAware) textView.resources.displayMetrics.density else 1.0f

    private val cache = mutableMapOf<String, WeakReference<DrawableHolder<*>>>()

    override fun getDrawable(source: String): Drawable {
        val existing = cache[source]?.get()
        val holder: DrawableHolder<*> = when {
            existing != null -> {
                cache.remove(source)
                existing
            }
            source.endsWith(".gif") -> {
                GifPlaceholder(source, context!!)
            }
            else -> {
                BitmapDrawablePlaceholder(source, context!!)
            }
        }
        holder.load()
        cache[source] = WeakReference(holder)
        return holder
    }

    private abstract inner class DrawableHolder<T>(protected val source: String, context: Context) :
        Drawable(), Target<T>,
        Drawable.Callback {

        init {
            imageGetterState[source] = ImageFetchState.Loading
        }


        private var ignoreBoundsOnce = false

        private val progressDrawable = gifProgressDrawable(context).apply {
            callback = this@DrawableHolder
        }

        private val errorDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_replay_24dp)!!.apply {
                val theme = context.asTheme()
                setColor(if (theme.isDark) Color.WHITE else Color.BLACK)
            }

        protected var drawable: Drawable? = null
            set(value) {
                field = value
                value?.let {
                    val textView = textView ?: return@let
                    val drawableWidth = (value.intrinsicWidth * density).toInt()
                    val drawableHeight = (value.intrinsicHeight * density).toInt()
                    val maxWidth = textView.measuredWidth
                    if (drawableWidth > maxWidth || matchParentWidth) {
                        val calculatedHeight = maxWidth * drawableHeight / drawableWidth
                        if (ignoreBoundsOnce) {
                            value.center()
                        } else {
                            value.setBounds(0, 0, maxWidth, calculatedHeight)
                        }
                        setBounds(0, 0, maxWidth, calculatedHeight)
                    } else {
                        if (ignoreBoundsOnce) {
                            value.center()
                        } else {
                            value.setBounds(0, 0, drawableWidth, drawableHeight)
                        }
                        setBounds(0, 0, drawableWidth, drawableHeight)
                    }
                    textView.text = textView.text
                }
                ignoreBoundsOnce = false
            }

        override fun draw(canvas: Canvas) {
            when (val drawable = drawable) {
                is Drawable -> drawable.draw(canvas)
                else -> progressDrawable.draw(canvas)
            }
        }

        override fun setAlpha(alpha: Int) {
            drawable?.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            drawable?.colorFilter = colorFilter
        }

        @Suppress("DEPRECATION")
        override fun getOpacity(): Int = drawable?.opacity ?: progressDrawable.opacity


        override fun invalidateDrawable(who: Drawable) {
            textView?.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            textView?.postDelayed(what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            textView?.removeCallbacks(what)
        }

        override fun onLoadStarted(placeholder: Drawable?) {
            imageGetterState[source] = ImageFetchState.Loading
            ignoreBoundsOnce = true
            drawable = progressDrawable
            progressDrawable.start()
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            imageGetterState[source] = ImageFetchState.Error
            ignoreBoundsOnce = true
            drawable = this.errorDrawable
        }

        @CallSuper
        override fun onResourceReady(resource: T, transition: Transition<in T>?) {
            imageGetterState[source] = ImageFetchState.Success
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            drawable = progressDrawable
        }

        override fun getSize(cb: SizeReadyCallback) =
            cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

        override fun removeCallback(cb: SizeReadyCallback) {}

        override fun setRequest(request: Request?) {}

        override fun getRequest(): Request? = null

        override fun onStart() {}

        override fun onStop() {}

        override fun onDestroy() {}

        abstract fun load()

        private fun Drawable.center() {
            val drawableWidth = (this.intrinsicWidth * density).toInt()
            val drawableHeight = (this.intrinsicHeight * density).toInt()
            val maxWidth = textView!!.measuredWidth
            val calculatedHeight = maxWidth * drawableHeight / drawableWidth
            val widthOffset = (maxWidth - drawableWidth) / 2
            val heightOffset = (calculatedHeight - drawableHeight) / 2
            this.setBounds(
                widthOffset,
                heightOffset,
                drawableWidth + widthOffset,
                drawableHeight + heightOffset
            )
        }
    }

    private inner class BitmapDrawablePlaceholder(source: String, context: Context) :
        DrawableHolder<Bitmap>(source, context) {


        override fun load() {
            textView!!.post {
                Glide.with(context ?: return@post)
                    .asBitmap()
                    .load(source)
                    .into(this)
            }
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            super.onResourceReady(resource, transition)
            drawable = BitmapDrawable(resources ?: return, resource)
        }
    }


    private inner class GifPlaceholder(source: String, context: Context) :
        DrawableHolder<File>(source, context) {

        override fun load() {
            textView!!.post {
                Glide.with(context ?: return@post)
                    .asFile()
                    .load(source)
                    .into(this)
            }
        }

        override fun onResourceReady(resource: File, transition: Transition<in File>?) {
            super.onResourceReady(resource, transition)
            try {
                val gifDrawable = GifDrawable(resource)
                gifDrawable.callback = this
                drawable = gifDrawable
                gifDrawable.start()
            } catch (e: IOException) {
                Timber.e(e)
            }
        }

    }

}