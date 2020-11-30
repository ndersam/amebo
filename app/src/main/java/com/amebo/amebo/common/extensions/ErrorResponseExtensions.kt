package com.amebo.amebo.common.extensions

import android.content.Context
import com.amebo.amebo.R
import com.amebo.amebo.common.widgets.OuchView
import com.amebo.core.domain.ErrorResponse

fun ErrorResponse.getMessage(context: Context): String {
    val res = when (this) {
        ErrorResponse.Login -> R.string.login_failed
        ErrorResponse.Network -> OuchView.State.NetworkError.subtitleRes
        ErrorResponse.Parse -> R.string.parse_error
        ErrorResponse.NotFound -> OuchView.State.NotFound.subtitleRes
        ErrorResponse.TopicLocked -> R.string.closed_topic
        ErrorResponse.TooManyAnonymousMails -> R.string.error_too_many_mails
        ErrorResponse.InsufficientMailingPermissions -> R.string.error_insufficient_mailing_permissions
        ErrorResponse.UnAuthorized -> R.string.unauthorized_access
        ErrorResponse.Muslim -> R.string.only_muslims_allowed
        ErrorResponse.TooManyModEmails -> R.string.please_wait_for_5_mins
        is ErrorResponse.Unknown -> return msg ?: exception!!.message
        ?: context.getString(R.string.unknown_error)
    }
    return context.getString(res)
}

fun ErrorResponse.getDrawableRes() = when (this) {
    ErrorResponse.Network -> OuchView.State.NetworkError.drawableRes
    ErrorResponse.NotFound -> OuchView.State.NotFound.drawableRes
    else -> OuchView.State.Unknown.drawableRes
}

