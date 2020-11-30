package com.amebo.amebo.screens.search

import androidx.fragment.app.Fragment
import com.amebo.amebo.common.extensions.disableCopyPaste
import com.amebo.amebo.databinding.SearchResultsScreenBinding
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.core.domain.SearchQuery
import java.lang.ref.WeakReference

class SearchResultsView(
    fragment: Fragment,
    contentAdapter: ItemAdapter,
    binding: SearchResultsScreenBinding,
    query: SearchQuery,
    listener: Listener
) : PostListView(
    fragment = fragment,
    listener = listener,
    recyclerView = binding.recyclerView,
    nextPage = binding.bottomBar.btnNextPage,
    postList = query,
    prevPage = binding.bottomBar.btnPrevPage,
    refreshPage = binding.bottomBar.btnRefreshPage,
    showContext = binding.bottomBar.btnMore,
    swipeRefreshLayout = binding.swipeRefreshLayout,
    toolbar = binding.toolbar,
    contentAdapter = contentAdapter
) {
    private val bindingRef = WeakReference(binding)
    private val binding: SearchResultsScreenBinding get() = bindingRef.get()!!

    init {
        binding.searchBox.disableCopyPaste()
        binding.searchBox.isFocusable = false
        binding.searchBox.isFocusableInTouchMode = false
        binding.searchBox.setOnClickListener {
            listener.visitSearch(query.query)
        }
        binding.subtitle.setOnClickListener {
            listener.onPageMetaClicked()
        }
    }

    override fun setTitle(title: String) {
        super.setTitle(title)
        binding.searchBox.isCursorVisible = false
        binding.searchBox.setText((postList as SearchQuery).query)
        binding.searchTerm.text = title
    }

    override fun setPostListMeta(meta: PostListMeta) {
        super.setPostListMeta(meta)
        binding.subtitle.text = toolbar.subtitle
    }

    override fun scrollToPosition(position: Int) {
        super.scrollToPosition(position)
        val bottomBar = binding.bottomBar.bottomBar
        bottomBar.behavior.slideDown(bottomBar)
    }

    interface Listener : IPostListView.Listener {
        fun visitSearch(query: String)
    }
}