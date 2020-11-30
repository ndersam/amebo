package com.amebo.amebo.screens.postlist.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemRecentTopicBinding
import com.amebo.core.domain.Topic

/**
 * Used to display list of related topics in [TopicInfoFragment] or viewed topics in [RecentTopicsFragment]
 */
class SimpleTopicListAdapter(
    private val listener: Listener,
    private val showDeleteButton: Boolean = true
) :
    ListAdapter<Topic, SimpleTopicListAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var _items = mutableListOf<Topic>()

    fun setItem(topics: List<Topic>) {
        _items = ArrayList(topics.toMutableList())
        submitList(_items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemRecentTopicBinding.inflate(inflater, parent, false),
            showDeleteButton = showDeleteButton,
            listener = listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun removeItemAt(position: Int) {
        _items.removeAt(position)
        submitList(ArrayList(_items))
    }

    class ViewHolder(
        private val binding: ItemRecentTopicBinding,
        listener: Listener,
        showDeleteButton: Boolean
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var topic: Topic

        init {
            binding.root.setOnClickListener {
                listener.onTopicClicked(topic)
            }
            if (showDeleteButton) {
                binding.btnRemove.isVisible = true
                binding.btnRemove.setOnClickListener {
                    listener.removeTopic(topic, layoutPosition)
                }
            } else {
                binding.btnRemove.isVisible = false
            }
        }

        fun bind(topic: Topic) {
            this.topic = topic
            binding.textView.text = topic.title
        }
    }

    interface Listener {
        fun onTopicClicked(topic: Topic)
        fun removeTopic(topic: Topic, position: Int) {

        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Topic>() {
            override fun areItemsTheSame(oldItem: Topic, newItem: Topic): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Topic, newItem: Topic): Boolean {
                return oldItem.id == newItem.id && oldItem.title == newItem.title
            }
        }
    }
}