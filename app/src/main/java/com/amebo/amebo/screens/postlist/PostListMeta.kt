package com.amebo.amebo.screens.postlist

import android.content.Context
import com.amebo.amebo.R

class PostListMeta(
    val currentPage: Int,
    val lastPage: Int?
) {
    fun toString(context: Context): String {
        R.string.page
        return when {
            lastPage != null -> context.getString(
                R.string.page_x_of_x,
                (currentPage + 1).toString(),
                (lastPage + 1).toString()
            )
            else -> context.getString(R.string.page_x, currentPage + 1)
        }
    }
}