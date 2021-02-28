package com.amebo.amebo.screens.explore

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import com.amebo.amebo.R
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectableBaseDialogFragment
import com.amebo.amebo.databinding.SelectBoardDialogBinding
import com.amebo.amebo.screens.explore.adapters.SearchAdapter
import com.amebo.amebo.screens.explore.adapters.TopicListAdapter
import com.amebo.core.domain.Board
import com.amebo.core.domain.Session
import com.amebo.core.domain.TopicList

class SelectBoardDialog : InjectableBaseDialogFragment(R.layout.select_board_dialog),
    SearchAdapter.Listener, TopicListAdapter.Listener {

    private val viewModel by lazy { createViewModel<SelectBoardViewModel>(this) }
    private val binding by viewBinding(SelectBoardDialogBinding::bind)


    private var searchHasFocus = false
        set(value) {
            field = value
            binding.rvBoards.isInvisible = value
            binding.rvSearch.isVisible = value
        }

    private var searchTerm: CharSequence = ""
        set(value) {
            if (value.toString() != field.toString()) {
                field = value
                if (field.isNotBlank()) {
                    performSearch(field.toString())
                }
                searchHasFocus = field.isNotBlank()
            }
        }


    private lateinit var adapter: TopicListAdapter
    private lateinit var searchAdapter: SearchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TopicListAdapter(
            this,
            true,
            requireArguments().getBoolean(USE_ALL_BOARDS, true)
        )
        searchAdapter = SearchAdapter(this)
        binding.searchBox.addTextChangedListener(
            onTextChanged = { _, _, _, _ ->
                searchTerm = binding.searchBox.text.toString()
            }
        )
        binding.rvBoards.adapter = adapter
        binding.rvSearch.adapter = searchAdapter
        viewModel.setView(::setLiveData)
    }

    override fun onTopicListClicked(topicList: TopicList) {
        onBoardSelected(topicList as Board)
    }

    override fun getSession(): Session? = null

    override fun dismissFollowedBoardHint() {
        pref.showFollowedBoardHint = false
    }

    override fun performSearch(query: String) {
        viewModel.findSuggestions(query)
    }

    override fun removeRecentSearch(query: String) {

    }

    private fun setLiveData(
        liveData: MutableLiveData<TopicListData>,
        suggestions: MutableLiveData<Event<List<Board>>>
    ) {
        liveData.observe(viewLifecycleOwner, {
            adapter.setData(it)
        })
        suggestions.observe(viewLifecycleOwner, EventObserver {
            searchAdapter.suggestedBoards = it
        })
    }

    override val isLoggedIn: Boolean get() = pref.isLoggedIn

    override val showFollowedBoardHint: Boolean get() = pref.showFollowedBoardHint

    override fun onAllBoardsClicked() {
        onBoardSelected(null)
    }

    private fun onBoardSelected(board: Board?) {
        setFragmentResult(
            FragKeys.RESULT_SELECTED_BOARD,
            bundleOf(FragKeys.BUNDLE_SELECTED_BOARD to board)
        )
        dismiss()
    }

    companion object {
        private const val SELECTED_BOARD = "selectedBoard"
        private const val USE_ALL_BOARDS = "useAllBoards"
        fun newBundle(board: Board?, showAllBoards: Boolean = true) =
            bundleOf(SELECTED_BOARD to board, USE_ALL_BOARDS to showAllBoards)
    }
}