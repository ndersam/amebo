package com.amebo.amebo.common.extensions

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children

fun Toolbar.drawableColor(@ColorInt color: Int) {
    val drawable = this.navigationIcon ?: return
    drawable.mutate()
    drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    navigationIcon = drawable
}

fun Toolbar.setMenu(@MenuRes menuResId: Int){
    menu.clear()
    inflateMenu(menuResId)
}

fun Toolbar.navigationImageView()
   = children.first { it is ImageView } as ImageView
