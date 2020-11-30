package com.amebo.amebo.common.popup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemPopupItemBinding

class ItemAdapter(private val fonts: List<PopupItem>, val onItemClick: (PopupItem) -> Unit) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    override fun getItemCount(): Int = fonts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPopupItemBinding.inflate(inflater, parent, false)
        return if (viewType == COLOR) {
            ColorViewHolder(binding)
        } else {
            ViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int) = when (fonts[position]) {
        is PopupItem.Color -> COLOR
        is PopupItem.Font -> FONT
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = fonts[position]
        when (item) {
            is PopupItem.Color -> {
                holder.binding.textView.setText(item.name)
                holder.binding.textView.setTextColor(item.value)
            }
            is PopupItem.Font -> {
                holder.binding.textView.setText(item.titleRes)
                holder.binding.textView.typeface = item.typeFace
            }
        }
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    open class ViewHolder(val binding: ItemPopupItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ColorViewHolder(binding: ItemPopupItemBinding) : ViewHolder(binding)

    companion object {
        private const val COLOR = 0
        private const val FONT = 1
    }
}