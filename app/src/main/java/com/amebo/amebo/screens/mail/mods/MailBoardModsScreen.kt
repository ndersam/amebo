package com.amebo.amebo.screens.mail.mods

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.screens.mail.BaseMailScreen
import com.amebo.core.domain.Board
import com.amebo.core.domain.BoardsDataPage

class MailBoardModsScreen : BaseMailScreen<BoardsDataPage>() {
    override val viewModel by viewModels<MailBoardModsViewModel>()

    private val boardId get() = requireArguments().getInt(BOARD_ID)

    val board get() = requireArguments().getParcelable<Board>(BOARD)!!

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        viewModel.initialize(boardId)
    }

    override fun onSubmissionSuccess(content: BoardsDataPage) {
        setFragmentResult(
            FragKeys.RESULT_TOPIC_LIST,
            bundleOf(FragKeys.BUNDLE_TOPIC_LIST to content)
        )
    }

    companion object {
        private const val BOARD_ID = "BOARD_ID"
        private const val BOARD = "BOARD"
        fun bundle(board: Board, boardId: Int) = bundleOf(BOARD to board, BOARD_ID to boardId)
    }
}