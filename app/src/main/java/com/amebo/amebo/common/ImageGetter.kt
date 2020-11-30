package com.amebo.amebo.common

import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView

class ImageGetter(private val textView: TextView) : Html.ImageGetter {
    private val webGetter = GlideImageGetter(textView, true, false) {}

    override fun getDrawable(source: String): Drawable {
        val drawable = EmojiGetter.getDrawable(textView.context, source)
        if (drawable != null) {
            return drawable
        }
        return webGetter.getDrawable(source)
    }
}