package com.amebo.amebo.screens.search

import android.os.Bundle
import androidx.core.os.bundleOf
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.SearchResultsScreenBinding
import com.amebo.amebo.screens.postlist.BasePostListScreen
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.core.domain.SearchQuery
import javax.inject.Inject
import javax.inject.Provider

class SearchResultScreen : BasePostListScreen<SearchQuery>(R.layout.search_results_screen), SearchResultsView.Listener {

    val binding: SearchResultsScreenBinding by viewBinding(SearchResultsScreenBinding::bind)
    override var postListView: IPostListView? = null

    @Inject
    lateinit var viewProvider: Provider<SearchResultsView>

    override val viewModel: PostListScreenViewModel<SearchQuery> by viewModels<SearchResultsViewModel>()


    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        binding.toolbar.setOnClickListener {
            router.toSearch(query = postList)
        }
    }

    override fun initializeViews() {
        postListView = viewProvider.get()
    }

    override fun visitSearch(query: String) {
        router.toSearch(binding.toolbar, postList)
    }

    override val postList: SearchQuery get() = requireArguments().getParcelable(QUERY)!!


    companion object {
        private const val QUERY = "query"
        fun newBundle(query: SearchQuery) = bundleOf(QUERY to query)
    }
}