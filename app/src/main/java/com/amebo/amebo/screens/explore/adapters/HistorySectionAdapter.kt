package com.amebo.amebo.screens.explore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.databinding.ItemExploreBoardBinding
import com.amebo.amebo.databinding.ItemExploreBoardTitleBinding


class HistorySectionAdapter(private val listener: Listener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: Array<Item> = arrayOf(Item.TopicHistory)

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            Item.TopicHistory.id ->
                ItemVH(
                    ItemExploreBoardBinding.inflate(layoutInflater, parent, false),
                    listener
                )
            else -> throw IllegalArgumentException("Unknown viewType `${viewType}`")
        }
    }


    override fun getItemViewType(position: Int): Int = items[position].id

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
        private lateinit var item: Item

        init {
            binding.root.setOnClickListener {
                when (item) {
                    is Item.TopicHistory -> {
                        listener.onRecentTopicsClicked()
                    }
                }
            }

        }

        fun bind(item: Item) {
            this.item = item
            binding.topicList.setText(
                when (item) {
                    is Item.TopicHistory -> R.string.topic_history
                }
            )
        }
    }


    class TitleVH(binding: ItemExploreBoardTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.title.setText(R.string.history)
        }
    }

    interface Listener {
        fun onRecentTopicsClicked() {}
    }

}

private sealed class Item(val id: Int) {
    object TopicHistory : Item(0)
}