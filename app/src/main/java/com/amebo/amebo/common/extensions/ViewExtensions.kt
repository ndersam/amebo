package com.amebo.amebo.common.extensions

import android.view.View
import androidx.annotation.StringRes
import com.amebo.core.domain.ErrorResponse
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar


fun View.snack(errorResponse: ErrorResponse) =
    Snackbar.make(this, errorResponse.getMessage(this.context), Snackbar.LENGTH_SHORT).show()

fun View.snack(@StringRes res: Int) =
    Snackbar.make(this, res, Snackbar.LENGTH_SHORT).show()

fun View.snack(msg: String) =
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()


fun BottomAppBar.show() {
    behavior.slideDown(this)
}