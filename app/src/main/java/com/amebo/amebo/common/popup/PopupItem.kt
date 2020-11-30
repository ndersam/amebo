package com.amebo.amebo.common.popup

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.StringRes

sealed class PopupItem{
    class Font(@StringRes val titleRes: Int, val value: String, val typeFace: Typeface): PopupItem() {
        fun name(context: Context) = context.getString(titleRes)
    }
    class Color(@StringRes val name: Int, @ColorInt val value: Int): PopupItem()
}

