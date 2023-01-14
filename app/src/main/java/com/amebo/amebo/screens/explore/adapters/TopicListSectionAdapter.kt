package com.amebo.amebo.screens.explore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.extensions.getTitle
import com.amebo.amebo.databinding.ItemExploreBoardBinding
import com.amebo.amebo.databinding.ItemExploreBoardTitleBinding
import com.amebo.amebo.databinding.ItemNoBoardsBinding
import com.amebo.core.domain.TopicList


class TopicListSectionAdapter(
    title: Int,
    topicLists: List<TopicList>,
    private val listener: Listener,
    private val onDismissNoBoardsItem: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: MutableList<Item> = mutableListOf()

    init {
        items.add(Item.Title(title))
        items.addAll(topicLists.map { Item.TopicListItem(it) })
        if (topicLists.isEmpty()) {
            items.add(Item.NoBoardsItem)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TITLE ->
                TitleVH(
                    ItemExploreBoardTitleBinding.inflate(layoutInflater, parent, false)
                )
            TYPE_BOARD ->
                BoardVH(
                    ItemExploreBoardBinding.inflate(layoutInflater, parent, false),
                    listener
                )
            TYPE_NO_BOARDS -> NoBoardVH(
                ItemNoBoardsBinding.inflate(layoutInflater, parent, false),
                onDismissNoBoardsItem
            )
            else -> throw IllegalArgumentException("Unknown viewType `${viewType}`")
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Item.TopicListItem -> TYPE_BOARD
            is Item.Title -> TYPE_TITLE
            is Item.NoBoardsItem -> TYPE_NO_BOARDS
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Item.TopicListItem -> {
                val vh = holder as BoardVH
                vh.bind(item.topicList)
            }
            is Item.Title -> {
                val vh = holder as TitleVH
                vh.bind(vh.itemView.context.getString(item.title))
            }
            Item.NoBoardsItem -> {}
        }
    }


    private sealed class Item {
        class Title(@StringRes val title: Int) : Item()
        class TopicListItem(val topicList: TopicList) : Item()
        object NoBoardsItem : Item()
    }

    class BoardVH(private val binding: ItemExploreBoardBinding, listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var topicList: TopicList

        init {
            binding.root.setOnClickListener { listener.onTopicListClicked(topicList) }
        }

        fun bind(topicList: TopicList) {
            this.topicList = topicList
            binding.topicList.text = topicList.getTitle(itemView.context)
        }
    }


    class TitleVH(private val binding: ItemExploreBoardTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.title.text = title
        }
    }

    class NoBoardVH(binding: ItemNoBoardsBinding, onDismissNoBoardsItem: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnOkay.setOnClickListener { onDismissNoBoardsItem() }
        }
    }

    interface Listener {
        fun onTopicListClicked(topicList: TopicList)
    }

    companion object {
        const val TYPE_TITLE = 1
        const val TYPE_BOARD = 2
        const val TYPE_NO_BOARDS = 3
    }
}