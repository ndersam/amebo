package com.amebo.amebo.screens.newpost.newtopic

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.NewTopicScreenBinding
import com.amebo.amebo.screens.newpost.FormScreen
import com.amebo.core.domain.Board
import com.amebo.core.domain.NewTopicForm
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.TopicPostListDataPage
import javax.inject.Inject
import javax.inject.Provider

class NewTopicScreen : FormScreen<NewTopicForm>(R.layout.new_topic_screen), NewTopicView.Listener {
    val binding: NewTopicScreenBinding by viewBinding(NewTopicScreenBinding::bind)

    val board get() = arguments?.getParcelable<Board>(BOARD)

    override val viewModel by viewModels<NewTopicScreenViewModel>()

    @Inject
    lateinit var formViewProvider: Provider<NewTopicView>

    override lateinit var formView: NewTopicView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_SELECTED_BOARD) { _, bundle ->
            viewModel.formData.board = bundle.getParcelable(FragKeys.BUNDLE_SELECTED_BOARD)
            setSelectedBoard(viewModel.formData.board)
        }
    }

    override fun initializeViews() {
        formView = formViewProvider.get()
    }

    override fun initializeViewModelState() {
        viewModel.initialize(board)
    }

    override fun selectBoard() = router.toSelectBoardDialog(null, false)

    override fun onSubmissionSuccess(postListDataPage: PostListDataPage) {
        super.onSubmissionSuccess(postListDataPage)
        val topicPostList = postListDataPage as TopicPostListDataPage
        router.toTopic(topicPostList.topic)
    }

    private fun setSelectedBoard(board: Board?) {
        formView.setSelectedBoard(board)
    }

    companion object {
        private const val BOARD = "board"
        fun newBundle(board: Board) = bundleOf(BOARD to board)
    }
}
