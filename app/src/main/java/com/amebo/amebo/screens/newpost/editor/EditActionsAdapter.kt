package com.amebo.amebo.screens.newpost.editor

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.extensions.context
import com.amebo.amebo.databinding.ItemEditMenuActionBinding
import java.util.*

class EditActionsAdapter(
    actions: List<EditAction>,
    private var badgeTextList: MutableMap<EditAction, Int> = mutableMapOf(),
    private var listener: Listener
) : ListAdapter<EditAction, EditActionsAdapter.ViewHolder>(DIFF_CALLBACK) {

    init {
        submitList(actions)
    }

    fun setActions(actions: List<EditAction>) {
        submitList(actions)
        badgeTextList.clear()
        notifyDataSetChanged()
    }

    fun setBadgeCount(count: Int, action: EditAction) {
        badgeTextList[action] = count
        notifyItemChanged(currentList.indexOf(action))
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        return ViewHolder(
            ItemEditMenuActionBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = getItem(position)
        val count = badgeTextList[action] ?: 0
        holder.bind(action, count, listener)
    }

    interface Listener {
        fun onItemClicked(view: View, type: EditAction)
    }

    class ViewHolder constructor(private val binding: ItemEditMenuActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(action: EditAction, badgeCount: Int, listener: Listener) {
            binding.imageView.setImageResource(action.drawableRes)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.root.tooltipText = action.getName(binding.context)
            } else {
                binding.root.contentDescription = action.getName(binding.context)
            }
            binding.root.setOnClickListener { listener.onItemClicked(it, action) }
            binding.count.text = badgeCount.toString()
            binding.count.isVisible = badgeCount > 0
        }
    }

    companion object {
        private fun emptyBadgeTextList(len: Int): MutableList<Int> {
            val badgeTextList: MutableList<Int> = ArrayList()
            for (i in 0 until len) badgeTextList.add(0)
            return badgeTextList
        }

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EditAction>() {
            override fun areItemsTheSame(oldItem: EditAction, newItem: EditAction) =
                oldItem.identifier == newItem.identifier

            override fun areContentsTheSame(oldItem: EditAction, newItem: EditAction) =
                oldItem.identifier == newItem.identifier

        }
    }
}