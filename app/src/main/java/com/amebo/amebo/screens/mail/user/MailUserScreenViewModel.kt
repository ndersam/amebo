package com.amebo.amebo.screens.mail.user

import com.amebo.amebo.screens.mail.BaseMailScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.MailUserForm
import com.amebo.core.domain.User
import com.github.michaelbull.result.Result
import javax.inject.Inject

class MailUserScreenViewModel @Inject constructor(private val nairaland: Nairaland) :
    BaseMailScreenViewModel<MailUserForm, User.Data>() {

    private lateinit var user: User


    fun initialize(user: User) {
        this.user = user
        initializeFormLoading()
    }

    override suspend fun doLoadForm(): Result<MailUserForm, ErrorResponse> {
        return nairaland.sources.forms.mailUser(user)
    }

    override suspend fun doSubmitForm(): Result<User.Data, ErrorResponse> {
        return nairaland.sources.submissions.newMail(form!!)
    }
}