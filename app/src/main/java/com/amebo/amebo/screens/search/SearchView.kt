package com.amebo.amebo.screens.search

import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.amebo.amebo.common.extensions.cursorAtEnd
import com.amebo.amebo.common.extensions.showKeyboard
import com.amebo.amebo.databinding.SearchScreenBinding
import com.amebo.amebo.screens.explore.adapters.SearchAdapter
import com.amebo.amebo.screens.explore.adapters.TopicListAdapter
import com.amebo.core.domain.Board
import java.lang.ref.WeakReference

class SearchView(
    fragment: Fragment,
    binding: SearchScreenBinding,
    private val listener: Listener
) : LifecycleObserver{
    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!
    private val searchAdapter = SearchAdapter(listener)

    private var searchTerm: String
        get() = binding.searchBox.text.toString()
        set(value) {
            binding.searchBox.setText(value)
            binding.searchBox.cursorAtEnd()
        }

    private var state: State = State.History
        set(value) {
            when (value) {
                is State.History -> {
                    searchAdapter.clear()
                }
                is State.Suggestions -> {
                    searchAdapter.query = searchTerm
                    listener.findSearchSuggestions(searchTerm.trim())
                }
            }
            field = value
        }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    fun lifecycleDestroyed() {
        bindingRef.get()?.recyclerView?.adapter = null
    }

    init {
        fragment.viewLifecycleOwner.lifecycle.addObserver(this)
        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (searchTerm.isNotBlank() && actionId == EditorInfo.IME_ACTION_SEARCH) {
                listener.performSearch(searchTerm)
                true
            } else {
                false
            }
        }
        binding.searchBox.doOnTextChanged { text, _, _, _ ->
            state = if (text?.isNotBlank() == true) State.Suggestions else State.History
        }

        binding.toolbar.setNavigationOnClickListener { listener.goBack() }
        binding.btnShowSearchOptions.setOnClickListener { listener.showSearchOptions() }
        binding.recyclerView.adapter = searchAdapter

        state = State.History
        binding.searchBox.showKeyboard()
    }


    fun showSearchSuggestions(boards: List<Board>) {
        searchAdapter.suggestedBoards = boards
    }

    fun setText(term: String) {
        searchTerm = term
    }

    fun setRecentSearch(recentSearch: List<String>) {
        searchAdapter.recentSearch = recentSearch.toMutableList()
    }

    fun showKeyboard() = binding.searchBox.showKeyboard()

    interface Listener : TopicListAdapter.Listener, SearchAdapter.Listener {
        fun goBack()
        override fun performSearch(query: String)
        fun findSearchSuggestions(term: String)
        fun showSearchOptions()
    }

    private sealed class State {
        object History : State()
        object Suggestions : State()
    }
}