package com.amebo.amebo.common.widgets;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import pl.droidsonroids.gif.GifTextView;

public class HackyTextView extends GifTextView {
    public HackyTextView(Context context) {
        super(context);
    }

    public HackyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HackyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        int startSelection = getSelectionStart();
        int endSelection = getSelectionEnd();
        if (startSelection < 0 || endSelection < 0) {
            if (getText() instanceof Spannable) {
                Selection.setSelection((Spannable) getText(), getText().length());
            }
        } else if (startSelection != endSelection) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                final CharSequence text = getText();
                setText(null);
                setText(text);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}