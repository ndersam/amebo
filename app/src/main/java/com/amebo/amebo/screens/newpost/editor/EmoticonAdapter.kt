package com.amebo.amebo.screens.newpost.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.EmojiGetter
import com.amebo.amebo.databinding.ItemEmoticonBinding

class EmoticonAdapter(
    private val emoticonList: List<EmojiGetter.Emoticon>,
    private var emoticonListener: EmoticonListener
) :
    RecyclerView.Adapter<EmoticonAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return emoticonList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemEmoticonBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val emoticon =
            emoticonList[position]
        holder.bind(emoticon, emoticonListener)
    }

    interface EmoticonListener {
        fun onEmoticonClicked(emoticon: EmojiGetter.Emoticon)
    }

    class ViewHolder(private val binding: ItemEmoticonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(emoticon: EmojiGetter.Emoticon, listener: EmoticonListener) {
            binding.button.setImageResource(emoticon.drawableRes)
            binding.button.setOnClickListener { listener.onEmoticonClicked(emoticon) }
        }
    }

}