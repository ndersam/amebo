package com.amebo.amebo.screens.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.fragments.PaddedBottomSheetDialog
import com.amebo.amebo.databinding.ItemBottomSheetPreferenceBinding
import com.amebo.amebo.databinding.LayoutBottomPreferenceDialogBinding
import com.amebo.amebo.di.Injectable
import com.amebo.amebo.common.Pref

class PreferenceBottomSheetDialog<T : Any>(
    context: Context,
    container: ViewGroup,
    private val pref: Pref,
    private val data: BottomPreferenceData<T>
) : PaddedBottomSheetDialog(context, container), ItemAdapter.Listener, Injectable {


    private var selectedPosition: Int = data.defaultValuePosition
    private val binding by lazy {
        LayoutBottomPreferenceDialogBinding.inflate(
            layoutInflater,
            container,
            false
        )
    }

    private val adapter = ItemAdapter(data.entries, data.entryValues, selectedPosition, this)

    init {
        this.title = data.title
        binding.recyclerView.adapter = adapter
        root.addView(binding.root)
    }

    override fun onItemClicked(position: Int) {
        pref[data.key] = data.entryValues[position]
        dismiss()
    }
}

private class ItemAdapter<T : Any>(
    private val entries: List<String>,
    private val entryValues: List<T>,
    private var selectedPosition: Int,
    private val listener: Listener
) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>(), ItemBinder {

    init {
        require(entries.size == entryValues.size)
    }

    override fun getItemCount(): Int = entryValues.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemBottomSheetPreferenceBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, entries[position], this)
    }

    class ViewHolder(private val binding: ItemBottomSheetPreferenceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.checkbox.isClickable = false
        }

        fun bind(position: Int, title: String, binder: ItemBinder) {
            binding.checkbox.text = title
            binding.checkbox.isChecked = binder.isSelected(position)
            binding.root.setOnClickListener { binder.onItemClicked(binding.checkbox, position) }
        }
    }

    override fun onItemClicked(box: CheckBox, position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        box.isChecked = true
        listener.onItemClicked(position)
    }

    override fun isSelected(position: Int) = position == selectedPosition

    interface Listener {
        fun onItemClicked(position: Int)
    }
}

interface ItemBinder {
    fun onItemClicked(box: CheckBox, position: Int)
    fun isSelected(position: Int): Boolean
}