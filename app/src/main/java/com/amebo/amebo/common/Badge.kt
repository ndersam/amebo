package com.amebo.amebo.common

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.amebo.amebo.R

class Badge(
    context: Context,
    res: Int = R.drawable.ic_menu_black_24dp,
    count: String = ""
) {
    val icon: LayerDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_badge_drawable) as LayerDrawable
    private val badge = BadgeDrawableBadge(context)

    init {
        val mainIcon = ContextCompat.getDrawable(context, res)!!
        badge.setCount(count)
        icon.mutate()
        icon.setDrawableByLayerId(R.id.ic_badge, badge)
        icon.setDrawableByLayerId(R.id.ic_main_icon, mainIcon)
    }

    fun setCount(count: String) {
        badge.setCount(count)
    }


}

/**
 * Created by Admin on 2/25/2016.
 */
private class BadgeDrawableBadge(context: Context) : Drawable() {
    private val mTextSize: Float
    private val mBadgePaint: Paint
    private val mBadgePaint1: Paint
    private val mTextPaint: Paint
    private val mTxtRect = Rect()
    private var mCount = ""

    init {
        val theme = context.asTheme()

        mTextSize = dpToPx(context, 8f) //text size
        mBadgePaint = Paint()
        mBadgePaint.color = theme.colorAccent
        mBadgePaint.isAntiAlias = true
        mBadgePaint.style = Paint.Style.FILL
        mBadgePaint1 = Paint()
        mBadgePaint1.color = theme.colorOnPrimary
        mBadgePaint1.isAntiAlias = true
        mBadgePaint1.style = Paint.Style.FILL
        mTextPaint = Paint()
        mTextPaint.color = Color.WHITE
        mTextPaint.typeface = Typeface.DEFAULT
        mTextPaint.textSize = mTextSize
        mTextPaint.isAntiAlias = true
        mTextPaint.textAlign = Paint.Align.CENTER
    }

    private fun dpToPx(context: Context, value: Float): Float {
        val r = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            r.displayMetrics
        )
    }

    override fun draw(canvas: Canvas) {
        if (mCount.isEmpty()) {
            return
        }
        val bounds = bounds
        val width = bounds.right - bounds.left.toFloat()
        // Position the badge in the top-right quadrant of the icon.

        /*Using Math.max rather than Math.min */
        val radius = width * 0.15f
        val centerX = width - radius - 1 + 10
        val centerY = radius - 5
        if (mCount.length <= 2) {
            // Draw badge circle.
            canvas.drawCircle(centerX, centerY, radius + 9, mBadgePaint1)
            canvas.drawCircle(centerX, centerY, radius + 7, mBadgePaint)
        } else {
            canvas.drawCircle(centerX, centerY, radius + 10, mBadgePaint1)
            canvas.drawCircle(centerX, centerY, radius + 8, mBadgePaint)
        }
        // Draw badge count text inside the circle.
        mTextPaint.getTextBounds(mCount, 0, mCount.length, mTxtRect)
        val textHeight = mTxtRect.bottom - mTxtRect.top.toFloat()
        val textY = centerY + textHeight / 2f
        if (mCount.length > 2) canvas.drawText(
            "99+",
            centerX,
            textY,
            mTextPaint
        ) else canvas.drawText(mCount, centerX, textY, mTextPaint)
    }

    /*
     Sets the count (i.e notifications) to display.
      */
    fun setCount(count: String) {
        mCount = count
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        // do nothing
    }

    override fun setColorFilter(cf: ColorFilter?) {
        // do nothing
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}