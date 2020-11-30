package com.amebo.amebo.screens.explore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.databinding.ItemExploreTopiclistAdapterBinding
import com.amebo.amebo.databinding.ItemSelectAllBoardsBinding
import com.amebo.amebo.screens.explore.TopicListData
import com.amebo.core.domain.*
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class TopicListAdapter(
    private val listener: Listener,
    private val configureForBoardSelection: Boolean = false,
    private val useAllBoards: Boolean = true,
    private val showHistory: Boolean = false,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: MutableList<Item> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ALL_BOARDS_TITLE -> {
                AllBoardsVH(ItemSelectAllBoardsBinding.inflate(layoutInflater, parent, false))
            }
            TYPE_RECENT_SECTION -> {
                HistorySectionVH(
                    ItemExploreTopiclistAdapterBinding.inflate(layoutInflater, parent, false)
                )
            }
            else -> TopicListSectionVH(
                ItemExploreTopiclistAdapterBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is Item.TopicListsItem -> TYPE_TOPIC_LISTS
        is Item.AllBoardsItem -> TYPE_ALL_BOARDS_TITLE
        Item.RecentItem -> TYPE_RECENT_SECTION
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Item.TopicListsItem -> {
                val vh = holder as TopicListSectionVH
                vh.bind(item.title, item.data, listener, ::removeFollowedBoardItem)
            }
            is Item.AllBoardsItem -> {
                val vh = holder as AllBoardsVH
                vh.bind(listener)
            }
            is Item.RecentItem -> {
                val vh = holder as HistorySectionVH
                vh.bind(listener)
            }
        }
    }

    /**
     * This is for authenticated users only
     */
    fun setFollowedBoards(boards: List<Board>) {
        if (boards.isNotEmpty() && listener.showFollowedBoardHint) {
            val index =
                items.indexOfFirst { it is Item.TopicListsItem && it.title == R.string.followed_boards }
            val newItem = Item.TopicListsItem(R.string.followed_boards, boards.sortedBy { it.name })
            if (index != -1) {
                items[index] = newItem
            } else {
                items.add(items.size - 1, newItem)
            }
            notifyDataSetChanged()
        }
    }

    private fun removeFollowedBoardItem() {
        val idx =
            items.indexOfFirst { it is Item.TopicListsItem && it.title == R.string.followed_boards }
        items.removeAt(idx)
        notifyItemRemoved(idx)
        listener.dismissFollowedBoardHint()
    }

    fun setData(data: TopicListData) {
        items.clear()

        if (configureForBoardSelection) {
            if (useAllBoards) items.add(Item.AllBoardsItem)
        } else {
            // multi-boards
            items.add(
                Item.TopicListsItem(
                    R.string.multi_boards,
                    if (listener.isLoggedIn)
                        listOf(Featured, Trending, NewTopics, FollowedBoards, FollowedTopics)
                    else
                        listOf(Featured, Trending, NewTopics)
                )
            )
        }

        if (showHistory) {
            items.add(Item.RecentItem)
        }

        if (listener.isLoggedIn && (data.followed.isNotEmpty() || listener.showFollowedBoardHint)) {
            items.add(
                Item.TopicListsItem(R.string.followed_boards, data.followed.sortedBy { it.name })
            )
        }

        items.add(
            Item.TopicListsItem(R.string.nairaland_picks, data.allBoards.sortedBy { it.name })
        )
        notifyDataSetChanged()
    }

    private sealed class Item {
        class TopicListsItem(@StringRes val title: Int, val data: List<TopicList>) : Item()
        object RecentItem : Item()
        object AllBoardsItem : Item()
    }

    class TopicListSectionVH(private val binding: ItemExploreTopiclistAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val layoutManager = FlexboxLayoutManager(binding.root.context)

        init {
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
        }

        fun bind(
            title: Int,
            data: List<TopicList>,
            listener: Listener,
            onDismissNoBoardItem: () -> Unit
        ) {
            binding.recyclerView.layoutManager = layoutManager
            binding.recyclerView.adapter =
                TopicListSectionAdapter(title, data, listener, onDismissNoBoardItem)
            binding.recyclerView.setHasFixedSize(true)
        }
    }

    class AllBoardsVH(private val binding: ItemSelectAllBoardsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: Listener) {
            binding.root.setOnClickListener { listener.onAllBoardsClicked() }
        }
    }

    class HistorySectionVH(private val binding: ItemExploreTopiclistAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val layoutManager = FlexboxLayoutManager(binding.root.context)

        init {
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
        }

        fun bind(listener: Listener) {
            binding.recyclerView.layoutManager = layoutManager
            binding.recyclerView.adapter = HistorySectionAdapter(listener)
            binding.recyclerView.setHasFixedSize(true)
        }
    }

    interface Listener : TopicListSectionAdapter.Listener, HistorySectionAdapter.Listener {
        val isLoggedIn: Boolean
        val showFollowedBoardHint: Boolean
        fun onAllBoardsClicked() {}
        fun dismissFollowedBoardHint()
    }

    companion object {
        private const val TYPE_ALL_BOARDS_TITLE = 0
        private const val TYPE_TOPIC_LISTS = 1
        private const val TYPE_RECENT_SECTION = 2
    }
}