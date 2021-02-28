package com.amebo.amebo.screens.explore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.context
import com.amebo.amebo.common.extensions.getTitle
import com.amebo.amebo.common.extensions.wrap100
import com.amebo.amebo.databinding.ItemExploreBoardBinding
import com.amebo.amebo.databinding.ItemExploreBoardTitleBinding
import com.amebo.core.domain.FollowedBoards
import com.amebo.core.domain.FollowedTopics
import com.amebo.core.domain.Session
import com.amebo.core.domain.TopicList


class MultiBoardsAdapter(
    private val listener: Listener,
    private val items: List<TopicList>,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TOPIC_LIST ->
                ItemVH(
                    ItemExploreBoardBinding.inflate(layoutInflater, parent, false),
                    listener
                )
            TITLE -> TitleVH(
                ItemExploreBoardTitleBinding.inflate(layoutInflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown viewType `${viewType}`")
        }
    }


    override fun getItemViewType(position: Int): Int =
        if (position < items.size) TOPIC_LIST else TITLE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                holder.bind(items[position])
            }
        }
    }


    private class ItemVH(
        private val binding: ItemExploreBoardBinding,
        private var listener: Listener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var item: TopicList

        init {
            binding.root.setOnClickListener {
                listener.onTopicListClicked(item)
            }
        }

        fun bind(item: TopicList) {
            this.item = item
            binding.topicList.text = item.getTitle(binding.context)
            binding.badge.isVisible = when (val session = listener.getSession()) {
                is Session -> when {
                    item is FollowedTopics && session.followedTopics > 0 -> {
                        binding.badge.text = session.followedTopics.wrap100()
                        true
                    }
                    item is FollowedBoards && session.followedBoards > 0 -> {
                        binding.badge.text = session.followedBoards.wrap100()
                        true
                    }
                    else -> {
                        false
                    }
                }
                else -> false
            }
        }
    }


    class TitleVH(binding: ItemExploreBoardTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.title.setText(R.string.multi_boards)
        }
    }

    interface Listener {
        fun onTopicListClicked(topicList: TopicList)
        fun getSession(): Session?
    }

    companion object {
        private const val TOPIC_LIST = 0
        private const val TITLE = 1
    }
}
