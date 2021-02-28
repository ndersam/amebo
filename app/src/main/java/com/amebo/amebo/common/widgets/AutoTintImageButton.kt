package com.amebo.amebo.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageButton
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.setColor

class AutoTintImageButton : AppCompatImageButton {

    @get:ColorInt
    var enabledTint: Int = -1

    @get:ColorInt
    var disabledTint: Int = -1

    constructor(context: Context) : super(context) {
        applyTint()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)

        applyTint()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context, attrs)
        applyTint()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        applyTint()
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AutoTintImageButton, 0, 0)
        enabledTint = ta.getColor(
            R.styleable.AutoTintImageButton_enabledTint,
            R.attr.colorOnPrimary.resolveColor()
        )
        disabledTint = ta.getColor(
            R.styleable.AutoTintImageButton_disabledTint,
            android.R.attr.textColorTertiary.resolveColor()
        )
        ta.recycle()
    }

    private fun applyTint(isEnabled: Boolean = this.isEnabled) {
        drawable?.setColor(if (isEnabled) enabledTint else disabledTint)
    }

    private fun Int.resolveColor(): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(this, typedValue, true)
        return typedValue.data
    }
}