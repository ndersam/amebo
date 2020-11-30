package com.amebo.amebo.common.extensions

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import kotlin.math.absoluteValue

fun RecyclerView.dividerDrawable(
    @DrawableRes drawableRes: Int = R.drawable.divider,
    @ColorInt color: Int? = null,
    decoration: DividerItemDecoration = DividerItemDecoration(
        context,
        DividerItemDecoration.VERTICAL
    )
) {
    val drawable = context.getDrawable(drawableRes)!!
    if (color != null) {
        drawable.setColor(color)
    }
    decoration.setDrawable(drawable)
    addItemDecoration(decoration)
}

fun RecyclerView.autoScrollSmoothlyTo(newPosition: Int, fromPosition: Int) {
    (fromPosition - newPosition).absoluteValue
    when (val mgr = layoutManager as? LinearLayoutManager) {
        is LinearLayoutManager -> {
            val first = mgr.findFirstCompletelyVisibleItemPosition()
            val last = mgr.findLastCompletelyVisibleItemPosition()

            // newPosition, newPosition + 1, ..., first
            if (first - newPosition > 2) {
                scrollToPosition(newPosition + 1)
            }
            // last,  last + 1, ..., newPosition
            else if (newPosition - last > 2) {
                scrollToPosition(newPosition - 1)
            }
        }
    }
    smoothScrollToPosition(newPosition)
}