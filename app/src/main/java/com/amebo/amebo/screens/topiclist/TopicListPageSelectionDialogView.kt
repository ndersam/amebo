package com.amebo.amebo.screens.topiclist

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.getTitle
import com.amebo.amebo.databinding.DialogTopiclistPageSelectionBinding
import com.amebo.core.domain.BaseTopicListDataPage
import com.amebo.core.domain.Board
import com.amebo.core.domain.BoardsDataPage
import com.amebo.core.domain.TopicList

class TopicListPageSelectionDialogView(
    private val currentPage: Int,
    private val lastPage: Int,
    private val topicList: TopicList,
    private val dataPage: BaseTopicListDataPage,
    private val binding: DialogTopiclistPageSelectionBinding,
    private val listener: Listener
) {
    var currentPageSelection: Int = 0
        private set

    init {
        binding.btnVisitPage.setOnClickListener {
            listener.onVisitPageClick()
        }
        binding.editPage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO && binding.btnVisitPage.isEnabled) {
                listener.onVisitPageClick()
                true
            } else {
                false
            }
        }

        setPageInformation()
        setBaseTopicListData()
    }

    private fun setPageInformation() {
        currentPageSelection = currentPage

        val onTextChanged = {
            when (val num = binding.editPage.text.toString().toIntOrNull()) {
                null -> { // empty string
                    binding.btnPrevPage.isEnabled = true // this allows for reset
                    binding.btnNextPage.isEnabled = true // this allows for reset
                    binding.btnVisitPage.isEnabled = false
                    binding.editPage.imeOptions = EditorInfo.IME_ACTION_NONE
                }
                else -> {
                    binding.btnPrevPage.isEnabled = num > 1
                    binding.btnNextPage.isEnabled = num < lastPage
                    val isOkay = num in 1..lastPage
                    if (isOkay) {
                        currentPageSelection = num
                    }
                    binding.btnVisitPage.isEnabled = isOkay
                    binding.editPage.imeOptions =
                        if (isOkay) EditorInfo.IME_ACTION_GO else EditorInfo.IME_ACTION_NONE
                }
            }
        }

        val onNavClick = { isNext: Boolean ->
            when (val num = binding.editPage.text.toString().toIntOrNull()) {
                null -> binding.editPage.setText(currentPageSelection.toString())
                else -> {
                    val newNum = if (isNext) {
                        num + 1
                    } else {
                        num - 1
                    }
                    binding.editPage.setText(newNum.toString())
                }
            }
        }

        val onEndPageClick = { isLast: Boolean ->
            if (isLast) {
                binding.editPage.setText(lastPage.toString())
            } else { // set first page
                binding.editPage.setText("1")
            }
            true
        }

        binding.btnNextPage.setOnClickListener { onNavClick(true) }
        binding.btnNextPage.setOnLongClickListener { onEndPageClick(true) }
        binding.btnPrevPage.setOnClickListener { onNavClick(false) }
        binding.btnPrevPage.setOnLongClickListener { onEndPageClick(false) }

        binding.editPage.setText(currentPage.toString())
        binding.editPage.doOnTextChanged { _, _, _, _ -> onTextChanged() }
        onTextChanged()
    }


    private fun setBaseTopicListData() {
        val context = binding.root.context
        binding.topicList.text = topicList.getTitle(context)
        binding.layoutBoard.isVisible = when (topicList) {
            is Board -> {
                val dataPage = dataPage as BoardsDataPage
                binding.btnNewTopic.setOnClickListener { listener.onNewTopicClick() }
                binding.btnFollowBoard.setOnClickListener { listener.onFollowBoardClick() }
                binding.btnFollowBoard.setText(
                    if (dataPage.isFollowing) {
                        R.string.following
                    } else {
                        R.string.follow
                    }
                )
                listener.isLoggedIn
            }
            else -> false
        }
    }

    interface Listener {
        val isLoggedIn: Boolean
        fun onNewTopicClick()
        fun onFollowBoardClick()
        fun onVisitPageClick()
    }
}