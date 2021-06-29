package com.amebo.amebo.common

import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.amebo.core.common.Values

class ImageGetter(private val textView: TextView, imageGetterState: ImageGetterState) :
    Html.ImageGetter {
    private val webGetter = GlideImageGetter(
        textView,
        imageGetterState,
        matchParentWidth = true,
        densityAware = false
    )

    override fun getDrawable(source: String): Drawable {
        val drawable = EmojiGetter.getDrawable(textView.context, source.replace(Values.URL, ""))
        return drawable ?: webGetter.getDrawable(source)
    }
}

class ImageGetterState(
    private val map: MutableMap<String, ImageFetchState> = mutableMapOf()
) : MutableMap<String, ImageFetchState> by map

enum class ImageFetchState {
    Loading,
    Error,
    Success
}