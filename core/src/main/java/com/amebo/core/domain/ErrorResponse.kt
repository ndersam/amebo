package com.amebo.core.domain

sealed class ErrorResponse {
    object Network : ErrorResponse()
    object Login : ErrorResponse()
    object Muslim : ErrorResponse()
    object NotFound : ErrorResponse()
    object Parse : ErrorResponse()
    object UnAuthorized : ErrorResponse()
    object TopicLocked : ErrorResponse()
    object TooManyAnonymousMails : ErrorResponse()
    object InsufficientMailingPermissions : ErrorResponse()
    object TooManyModEmails : ErrorResponse()
    class Unknown(val msg: String? = null, val exception: Exception? = null) : ErrorResponse() {
        init {
            require(msg != null || exception != null)
        }
    }
}
