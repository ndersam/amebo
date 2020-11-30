package com.amebo.amebo.screens.search

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.hideKeyboard
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.SearchScreenBinding
import com.amebo.core.domain.Board
import com.amebo.core.domain.SearchQuery
import com.amebo.core.domain.TopicList

class SearchScreen : BaseFragment(R.layout.search_screen), SearchView.Listener {

    companion object {
        private const val SEARCH_QUERY = "searchQuery"

        fun newBundle(query: SearchQuery) = bundleOf(SEARCH_QUERY to query)
    }

    private val binding by viewBinding(SearchScreenBinding::bind)
    private val viewModel by viewModels<SearchViewModel>()

    private lateinit var searchView: SearchView

    private var searchOptions: Bundle? = null
    private val onlyTopics
        get() = searchOptions?.getBoolean(FragKeys.BUNDLE_ONLY_TOPICS) ?: false
    private val onlyImages
        get() = searchOptions?.getBoolean(FragKeys.BUNDLE_ONLY_IMAGES) ?: false
    private val selectedBoard
        get() = searchOptions?.getParcelable<Board>(FragKeys.BUNDLE_SELECTED_BOARD)

    private var savedSearchTerm
        get() = arguments?.getString("_saved_search_term")
        set(value) {
            if (value != null) {
                val bundle = arguments ?: Bundle()
                bundle.putString("_saved_search_term", value)
                arguments = bundle
            }
        }
    private var searchTermFromBundle: String? = null
    private var navigatedToResultsScreen: Boolean
        get() = arguments?.getBoolean("wentToResultsScreen", false) ?: false
        set(value) {
            val arguments = arguments ?: Bundle()
            arguments.putBoolean("wentToResultsScreen", value)
            this.arguments = arguments
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_SEARCH_OPTIONS) { _, bundle ->
            searchOptions = bundle
        }
        when (val query = arguments?.getParcelable<SearchQuery>(SEARCH_QUERY)) {
            is SearchQuery -> {
                searchOptions = bundleOf(
                    FragKeys.BUNDLE_ONLY_TOPICS to query.onlyShowTopicPosts,
                    FragKeys.BUNDLE_ONLY_IMAGES to query.onlyShowImages,
                    FragKeys.BUNDLE_SELECTED_BOARD to query.board
                )
                searchTermFromBundle = query.query
            }
        }
    }


    override fun onViewCreated(savedInstanceState: Bundle?) {
        /**
         * If navigated to results page and returning, pop back stack again.
         */
        if (navigatedToResultsScreen) router.back()

        searchView = SearchView(this, binding, this)

        viewModel.initialize()
        viewModel.searchHistoryEvent.observe(viewLifecycleOwner, EventObserver {
            searchView.setRecentSearch(it)
        })
        viewModel.suggestionsEvent.observe(viewLifecycleOwner, EventObserver {
            searchView.showSearchSuggestions(it)
        })


        // restore view data
        val savedSearchTerm = savedSearchTerm
        val searchTermFromBundle = searchTermFromBundle
        when {
            savedSearchTerm != null -> searchView.setText(savedSearchTerm)
            searchTermFromBundle != null -> searchView.setText(searchTermFromBundle)
        }
        router.setOnDialogDismissListener(viewLifecycleOwner) {
            searchView.showKeyboard()
        }

        searchView.showKeyboard()
    }

    override fun goBack() {
        hideKeyboard()
        router.back()
    }

    override fun performSearch(query: String) {
        navigatedToResultsScreen = true
        hideKeyboard()
        savedSearchTerm = query
        val searchQuery = SearchQuery(
            query,
            onlyShowTopicPosts = onlyTopics,
            onlyShowImages = onlyImages,
            board = selectedBoard
        )
        router.toSearchResults(searchQuery)
        viewModel.saveSearch(searchQuery)
    }

    override fun findSearchSuggestions(term: String) = viewModel.findSuggestions(term)

    override fun showSearchOptions() {
        hideKeyboard()
        router.toExploreSearchOptions(onlyTopics, onlyImages, selectedBoard)
    }

    override fun onTopicListClicked(topicList: TopicList) {
        navigatedToResultsScreen = true
        hideKeyboard()
        router.toTopicList(topicList)
    }

    override fun dismissFollowedBoardHint() {
        pref.showFollowedBoardHint = false
    }

    override val isLoggedIn: Boolean get() = pref.isLoggedIn

    override val showFollowedBoardHint: Boolean get() = pref.showFollowedBoardHint

    override fun removeRecentSearch(query: String) = viewModel.removeSearchHistory(query)
}