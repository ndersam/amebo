package com.amebo.amebo.screens.reportpost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.ReportPostForm
import com.amebo.core.domain.SimplePost
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReportPostSharedScreenViewModel @Inject constructor(private val nairaland: Nairaland) :
    ViewModel() {
    var data = ReportPostFormData()
    private lateinit var post: SimplePost

    private val _formLoadEvent = MutableLiveData<Event<ReportPostFormData>>()
    val formLoadEvent: LiveData<Event<ReportPostFormData>> = _formLoadEvent

    private val _submissionEvent = MutableLiveData<Event<Resource<PostListDataPage>>>()
    val submissionEvent: LiveData<Event<Resource<PostListDataPage>>> = _submissionEvent

    val canSubmit get() = _submissionEvent.value?.peekContent() !is Resource.Loading && data.reason.isNotBlank()

    fun initialize(
        post: SimplePost
    ) {
        this.post = post
        _formLoadEvent.value = Event(data)
    }

    fun submit() {
        _submissionEvent.value = Event(Resource.Loading(null))
        viewModelScope.launch {
            var form: ReportPostForm? = null
            when(val result = nairaland.sources.forms.reportPost(post)){
                is Err -> {
                    _submissionEvent.value = Event(Resource.Error(result.error, null))
                    return@launch
                }
                is Ok -> {
                    form = result.value
                }
            }
            form.reason = data.reason
            val result = nairaland.sources.submissions.reportPost(form)
            _submissionEvent.value = Event(result.toResource(null))
        }
    }
}
