package com.amebo.amebo.screens.explore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.databinding.ItemRecentSearchQueryBinding
import com.amebo.amebo.databinding.ItemSearchBoardSuggestionBinding
import com.amebo.amebo.databinding.ItemSearchQueryBinding
import com.amebo.core.domain.Board
import com.amebo.core.domain.TopicList

class SearchAdapter(private val listener: Listener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var query: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var suggestedBoards: List<Board> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var recentSearch: MutableList<String> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val showSuggestions get() = query.isBlank() && suggestedBoards.isEmpty()

    fun clear() {
        query = ""
        suggestedBoards = emptyList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            QUERY -> QueryVH(
                ItemSearchQueryBinding.inflate(inflater, parent, false),
                listener
            )
            BOARD -> SuggestionVH(
                ItemSearchBoardSuggestionBinding.inflate(inflater, parent, false),
                listener
            )
            RECENT_QUERY -> RecentQueryVH(
                ItemRecentSearchQueryBinding.inflate(inflater, parent, false),
                listener
            ) { term, position ->
                listener.removeRecentSearch(term)
                recentSearch.removeAt(position)
                notifyItemRemoved(position)
            }
            else -> throw IllegalArgumentException("You shouldn't be here")
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (showSuggestions)
            return RECENT_QUERY
        if (position < suggestedBoards.size)
            return BOARD
        return QUERY
    }

    override fun getItemCount(): Int {
        if (showSuggestions)
            return recentSearch.size
        if (query.isBlank())
            return suggestedBoards.size
        return suggestedBoards.size + 1 // `+1` for query
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is QueryVH -> holder.bind(query)
            is SuggestionVH -> holder.bind(suggestedBoards[position])
            is RecentQueryVH -> holder.bind(recentSearch[position])
        }
    }

    class QueryVH(private val binding: ItemSearchQueryBinding, listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var query: String

        init {
            binding.text.setTextColor(itemView.context.asTheme().colorAccent)
            binding.root.setOnClickListener {
                listener.performSearch(query)
            }
        }

        fun bind(query: String) {
            this.query = query
            binding.text.text = itemView.context.getString(R.string.results_for, query)
        }
    }

    class RecentQueryVH(
        private val binding: ItemRecentSearchQueryBinding,
        private val listener: Listener,
        private val onRemoveClicked: (String, Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var query: String

        init {
            binding.btnRemove.setOnClickListener {
                onRemoveClicked(query, bindingAdapterPosition)
            }
            binding.root.setOnClickListener { listener.performSearch(query) }
        }

        fun bind(query: String) {
            this.query = query
            binding.text.text = query
        }
    }

    class SuggestionVH(private val binding: ItemSearchBoardSuggestionBinding, listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var board: Board

        init {
            binding.root.setOnClickListener { listener.onTopicListClicked(board) }
        }

        fun bind(board: Board) {
            this.board = board
            binding.text.text = board.name
        }
    }

    interface Listener {
        fun onTopicListClicked(topicList: TopicList)
        fun performSearch(query: String)
        fun removeRecentSearch(query: String)
    }

    companion object {
        private const val QUERY = 0
        private const val BOARD = 1
        private const val RECENT_QUERY = 2
    }
}