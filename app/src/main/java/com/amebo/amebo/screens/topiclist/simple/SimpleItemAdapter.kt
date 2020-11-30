package com.amebo.amebo.screens.topiclist.simple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemSpecialTopicBinding
import com.amebo.amebo.screens.topiclist.adapters.ItemAdapter
import com.amebo.amebo.screens.topiclist.adapters.TopicListAdapterListener
import com.amebo.core.domain.Topic

class SimpleItemAdapter(
    private val topics: List<Topic>,
    private val listener: TopicListAdapterListener
) :
    RecyclerView.Adapter<ItemAdapter.OtherTopicItemVH>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemAdapter.OtherTopicItemVH {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAdapter.OtherTopicItemVH(
            ItemSpecialTopicBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = topics.size

    override fun onBindViewHolder(
            holder: ItemAdapter.OtherTopicItemVH,
            position: Int
    ) {
        holder.bind(topics[position], listener)
    }
}