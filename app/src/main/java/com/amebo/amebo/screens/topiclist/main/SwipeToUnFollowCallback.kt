package com.amebo.amebo.screens.topiclist.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.dpToPx
import com.amebo.amebo.common.extensions.spToPx
import java.util.*

class SwipeToUnFollowCallback(context: Context, private val listener: Listener) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background: ColorDrawable = ColorDrawable(context.asTheme().colorAccent)
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.RIGHT
        textSize = context.spToPx(18)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // used for up and down movements
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.deleteItemAt(viewHolder.layoutPosition)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20 // so background is behind the rounded corners of itemView
        when {
            dX > 0 -> { // Swiping to the right
                background.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
                )
            }
            dX < 0 -> { // Swiping to the left
                background.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom
                )
            }
            else -> { // view is unSwiped
                background.setBounds(0, 0, 0, 0)
            }
        }
        background.draw(c)

       if (dX < 0) {
           val context = viewHolder.itemView.context
           val x = (itemView.right - context.dpToPx(18)).toFloat()
           val y = (itemView.top + itemView.bottom + textPaint.textSize) / 2f
           c.drawText(context.getString( R.string.unfollow).toUpperCase(Locale.ENGLISH), x, y, textPaint)
       }
    }

    fun interface Listener {
        fun deleteItemAt(position: Int)
    }


}