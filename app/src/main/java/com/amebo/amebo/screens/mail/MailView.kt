package com.amebo.amebo.screens.mail

import com.amebo.amebo.common.Resource

interface MailView {
    fun setSubmissionEnabled(isEnabled: Boolean)
    fun onFormLoadEventSuccess(resource: Resource.Success<MailFormData>)
    fun onFormLoadEventLoading(resource: Resource.Loading<MailFormData>)
    fun onFormLoadEventError(resource: Resource.Error<MailFormData>)
    fun onFormSubmissionEventSuccess(resource: Resource.Success<*>)
    fun onFormSubmissionEventError(resource: Resource.Error<*>)
    fun onFormSubmissionEventLoading(resource: Resource.Loading<*>)
    fun showKeyboard()

    interface Listener {
        val isEditingEnabled: Boolean
        fun setSubject(subject: String)
        fun setBody(text: String)
        fun submit()
        fun showRules()
        fun goBack()
        fun retryLastRequest()
    }
}