package com.amebo.amebo.screens.newpost.newtopic

import android.app.Application
import com.amebo.amebo.screens.newpost.FormViewModel
import com.amebo.amebo.screens.newpost.NewTopicFormData
import com.amebo.core.Nairaland
import com.amebo.core.common.Either
import com.amebo.core.domain.*
import com.github.michaelbull.result.Result
import javax.inject.Inject

class NewTopicScreenViewModel @Inject constructor(nairaland: Nairaland, application: Application) :
    FormViewModel<NewTopicForm>(nairaland, application) {

    override val loadFormFirst: Boolean get() = false
    override val formData = NewTopicFormData()
    override val canSubmit: Boolean
        get() = super.canSubmit && formData.board != null

    fun initialize(
        board: Board?
    ) {
        if (board != null) {
            formData.board = board
        }
        initialize()
    }

    override suspend fun doFetchFormData(): Result<Either<NewTopicForm, AreYouMuslimDeclarationForm>, ErrorResponse> {
        val board = formData.board
        checkNotNull(board)
        var boardId = board.id
        if (boardId == -1) {
            boardId = nairaland.sources.boards.selectFromSlug(board.url).id
        }
        return nairaland.sources.forms.newTopic(boardId)
    }

    override suspend fun doSubmitFormData(form: NewTopicForm): Result<PostListDataPage, ErrorResponse> {
        return nairaland.sources.submissions.newTopic(form)
    }
}
