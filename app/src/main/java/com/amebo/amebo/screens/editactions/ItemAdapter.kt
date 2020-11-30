package com.amebo.amebo.screens.editactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemEditActionBinding
import com.amebo.amebo.screens.newpost.editor.EditActionSetting
import java.util.*

class ItemAdapter(private val items: MutableList<EditActionSetting>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>(), Draggable {
    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEditActionBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun move(fromPosition: Int, toPosition: Int) {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    class ViewHolder(private val binding: ItemEditActionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EditActionSetting) {
            binding.checkbox.text = item.editAction.getName(binding.checkbox.context)
            binding.checkbox.setOnCheckedChangeListener(null)
            binding.checkbox.isChecked = item.isVisible
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                item.isVisible = isChecked
            }
        }
    }

}