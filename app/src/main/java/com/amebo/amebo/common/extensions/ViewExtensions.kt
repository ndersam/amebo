package com.amebo.amebo.common.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import com.amebo.core.domain.ErrorResponse
import com.google.android.material.snackbar.Snackbar

fun View.showKeyboard() {
    post {
        performClick()
        if (requestFocus()) {
            val mgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}


fun View.snack(errorResponse: ErrorResponse) =
    Snackbar.make(this, errorResponse.getMessage(this.context), Snackbar.LENGTH_SHORT).show()

fun View.snack(@StringRes res: Int) =
    Snackbar.make(this, res, Snackbar.LENGTH_SHORT).show()

fun View.snack(msg: String) =
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()
