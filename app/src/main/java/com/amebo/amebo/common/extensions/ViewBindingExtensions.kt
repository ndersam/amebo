package com.amebo.amebo.common.extensions

import android.content.Context
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import com.amebo.core.domain.ErrorResponse

val ViewBinding.context: Context get() = root.context

fun ViewBinding.snack(errorResponse: ErrorResponse) = this.root.snack(errorResponse)

fun ViewBinding.snack(@StringRes res: Int) = this.root.snack(res)
