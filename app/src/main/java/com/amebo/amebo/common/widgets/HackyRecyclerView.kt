package com.amebo.amebo.common.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class HackyRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (adapter != null) {
            adapter = null
        }
    }
}