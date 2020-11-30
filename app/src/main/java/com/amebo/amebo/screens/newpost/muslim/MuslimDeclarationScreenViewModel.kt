package com.amebo.amebo.screens.newpost.muslim

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.core.Nairaland
import com.amebo.core.domain.AreYouMuslimDeclarationForm
import com.amebo.core.domain.Form
import com.amebo.core.domain.ResultWrapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class MuslimDeclarationScreenViewModel @Inject constructor(private val nairaland: Nairaland) :
    ViewModel() {
    private val _submissionEvent = MutableLiveData<Event<Resource<SubmissionResult>>>()
    val submissionEvent: LiveData<Event<Resource<SubmissionResult>>> = _submissionEvent

    fun submit(form: AreYouMuslimDeclarationForm) {
        viewModelScope.launch {
            _submissionEvent.value = Event(Resource.Loading())

            when (val result = nairaland.sources.submissions.areYouMuslim(form)) {
                is ResultWrapper.Success -> {
                    _submissionEvent.value = Event(
                        Resource.Success(
                            when (val postForm = result.data) {
                                is Form -> {
                                    SubmissionResult.Confirmed(postForm)
                                }
                                else -> SubmissionResult.Declined
                            }
                        )
                    )
                }
                is ResultWrapper.Failure -> {
                    _submissionEvent.value = Event(Resource.Error(result.data))
                }
            }
        }
    }
}
