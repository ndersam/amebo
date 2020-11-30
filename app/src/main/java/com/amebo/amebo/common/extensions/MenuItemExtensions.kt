package com.amebo.amebo.common.extensions

import android.content.Context
import android.view.MenuItem
import com.amebo.amebo.common.asTheme

fun MenuItem.applyEnableTint(context: Context) {
    val theme = context.asTheme()
    icon?.setColor(if (isEnabled) theme.colorOnPrimary else theme.textColorTertiary)
}