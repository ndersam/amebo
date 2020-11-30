package com.amebo.amebo.screens.topiclist.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemViewedTopicBinding
import com.amebo.core.domain.Topic

class ItemAdapter(private val listener: Listener) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private var _items = mutableListOf<Topic>()

    override fun getItemCount(): Int = _items.size

    fun setItem(topics: List<Topic>) {
        _items = topics.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemViewedTopicBinding.inflate(inflater, parent, false),
            listener = listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(_items[position])


    fun removeItemAt(position: Int) {
        _items.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(
        private val binding: ItemViewedTopicBinding,
        listener: Listener,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var topic: Topic

        init {
            binding.root.setOnClickListener {
                listener.onTopicClicked(topic)
            }
            binding.btnRemove.isVisible = true
            binding.btnRemove.setOnClickListener {
                listener.removeTopic(topic, bindingAdapterPosition)
            }
        }

        fun bind(topic: Topic) {
            this.topic = topic
            binding.textView.text = topic.title
        }
    }

    interface Listener {
        fun onTopicClicked(topic: Topic)
        fun removeTopic(topic: Topic, position: Int)
    }

}