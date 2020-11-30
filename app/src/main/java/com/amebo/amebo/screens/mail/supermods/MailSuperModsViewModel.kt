package com.amebo.amebo.screens.mail.supermods

import com.amebo.amebo.screens.mail.BaseMailScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.MailSuperModsForm
import com.amebo.core.domain.ResultWrapper
import javax.inject.Inject

class MailSuperModsViewModel @Inject constructor(private val nairaland: Nairaland) :
    BaseMailScreenViewModel<MailSuperModsForm, Unit>() {
    override suspend fun doSubmitForm(): ResultWrapper<Unit, ErrorResponse> {
        return nairaland.sources.submissions.newMail(form!!)
    }

    override suspend fun doLoadForm(): ResultWrapper<MailSuperModsForm, ErrorResponse> {
        return nairaland.sources.forms.mailSuperMods()
    }

}