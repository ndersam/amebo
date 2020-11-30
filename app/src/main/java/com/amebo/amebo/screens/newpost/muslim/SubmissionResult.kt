package com.amebo.amebo.screens.newpost.muslim

import com.amebo.core.domain.Form

sealed class SubmissionResult {
    class Confirmed(val data: Form) : SubmissionResult()
    object Declined : SubmissionResult()
}