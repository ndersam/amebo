package com.amebo.amebo.screens.mail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.extensions.toResource
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.MailForm
import com.amebo.core.domain.ResultWrapper
import kotlinx.coroutines.launch

abstract class BaseMailScreenViewModel<T : MailForm, U : Any> : ViewModel() {
    private val _formLoadingEvent = MutableLiveData<Event<Resource<MailFormData>>>()
    val formLoadingEvent: LiveData<Event<Resource<MailFormData>>> = _formLoadingEvent

    private val _submissionEnabledEvent = MutableLiveData<Boolean>()
    val submissionEnabledEvent: LiveData<Boolean> = _submissionEnabledEvent

    private val _formSubmissionEvent = MutableLiveData<Event<Resource<U>>>()
    val formSubmissionEvent: LiveData<Event<Resource<U>>> = _formSubmissionEvent

    private var formData: MailFormData? = null
    protected var form: T? = null

    private var lastRequestType: RequestType? = null

    private val canSubmit: Boolean
        get() {
            if (form == null) {
                return false
            }
            return when (_formSubmissionEvent.value?.peekContent()) {
                is Resource.Loading -> false
                else -> !formData?.body.isNullOrBlank() && !formData?.title.isNullOrBlank()
            }
        }

    val editingEnabled get() = form?.canSendMail == true


    fun initializeFormLoading() {
        when (val formData = formData) {
            null -> {
                loadForm()
            }
            else -> {
                _formLoadingEvent.value = Event(Resource.Success(formData))
                notifySubmission()
            }
        }
    }

    private fun notifySubmission() {
        _submissionEnabledEvent.value = canSubmit
    }

    fun setBody(text: String) {
        formData?.body = text
        notifySubmission()
    }

    fun setSubject(text: String) {
        formData?.title = text
        notifySubmission()
    }

    fun retry() {
        when (lastRequestType) {
            RequestType.FormLoading -> loadForm()
            RequestType.FormSubmission -> submit()
        }
    }

    fun submit() {
        if (form == null) return
        viewModelScope.launch {
            _submissionEnabledEvent.value = false
            _formSubmissionEvent.value = Event(Resource.Loading(null))
            lastRequestType = RequestType.FormSubmission
            _formSubmissionEvent.value = Event(doSubmitForm().toResource(null))
            notifySubmission()
        }
    }


    private fun loadForm() {
        viewModelScope.launch {
            _submissionEnabledEvent.value = false
            _formLoadingEvent.value = Event(Resource.Loading(formData))
            lastRequestType = RequestType.FormLoading

            when (val result = doLoadForm()) {
                is ResultWrapper.Success -> {
                    form = result.data
                    formData = MailFormData(
                        body = result.data.body, title = result.data.subject,
                        editable = result.data.canSendMail
                    )
                    _formLoadingEvent.value = Event(Resource.Success(formData!!))
                }
                is ResultWrapper.Failure -> {
                    _formLoadingEvent.value = Event(Resource.Error(result.data, formData))
                }
            }
            notifySubmission()
        }
    }

    protected abstract suspend fun doSubmitForm(): ResultWrapper<U, ErrorResponse>

    protected abstract suspend fun doLoadForm(): ResultWrapper<T, ErrorResponse>

    private enum class RequestType {
        FormLoading,
        FormSubmission
    }
}