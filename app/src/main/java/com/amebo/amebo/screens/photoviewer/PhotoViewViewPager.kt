package com.amebo.amebo.screens.photoviewer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * ViewPager that "fixes" issues regular ViewPagers have with [com.github.chrisbanes.photoview.PhotoView]
 *
 * @see [](https://github.com/chrisbanes/PhotoView.issues-with-viewgroups)
 */
class PhotoViewViewPager : ViewPager {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

