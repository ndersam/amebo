package com.amebo.amebo.common.extensions

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

fun Drawable.setColor(@ColorInt color: Int){
    mutate()
    colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
}