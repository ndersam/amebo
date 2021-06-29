package com.amebo.amebo.screens.mail.mods

import com.amebo.amebo.screens.mail.BaseMailScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.BoardsDataPage
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.MailBoardModsForm
import com.github.michaelbull.result.Result
import javax.inject.Inject

class MailBoardModsViewModel @Inject constructor(private val nairaland: Nairaland) :
    BaseMailScreenViewModel<MailBoardModsForm, BoardsDataPage>() {

    private var boardId = -1

    fun initialize(boardId: Int) {
        this.boardId = boardId
        initializeFormLoading()
    }

    override suspend fun doSubmitForm(): Result<BoardsDataPage, ErrorResponse> {
        return nairaland.sources.submissions.newMail(form!!)
    }

    override suspend fun doLoadForm(): Result<MailBoardModsForm, ErrorResponse> {
        return nairaland.sources.forms.mailBoardMods(boardId)
    }

}