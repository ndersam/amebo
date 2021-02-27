package com.amebo.amebo.common.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.setColor

class AutoTintImageButton : AppCompatImageButton {

    private val theme by lazy { context.asTheme() }

    constructor(context: Context?) : super(context) {
        applyTint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        applyTint()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        applyTint()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        applyTint()
    }

    private fun applyTint(isEnabled: Boolean = this.isEnabled) {
        drawable?.setColor(if (isEnabled) theme.colorOnPrimary else theme.textColorTertiary)
    }
}