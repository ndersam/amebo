package com.amebo.amebo.screens.imagepicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.GlideApp
import com.amebo.amebo.databinding.ItemImagePickerItemBinding
import com.bumptech.glide.RequestManager

class GalleryAdapter(private val listener: Listener, fragment: Fragment) :
    RecyclerView.Adapter<GalleryAdapter.ImageItemViewHolder>() {
    private val requestManager = GlideApp.with(fragment)
    private var items: MutableList<ImageItem> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: ImageItemViewHolder, position: Int) {
        h.bind(position, items[position], listener)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageItemViewHolder(
            ItemImagePickerItemBinding.inflate(inflater, parent, false),
            requestManager
        )
    }

    fun addImage(item: ImageItem, position: Int = -1) {
        if (position == -1) {
            items.add(item)
        } else {
            items.add(position, item)
        }
        notifyDataSetChanged()
    }


    fun removeImage(position: Int) {
        items.removeAt(position)
        notifyDataSetChanged()
    }


    class ImageItemViewHolder(
        private val binding: ItemImagePickerItemBinding,
        private val requestManager: RequestManager
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, imageItem: ImageItem, listener: Listener) {
            when (imageItem) {
                is ImageItem.Existing -> {
                    requestManager
                        .load(imageItem.url)
                        .into(binding.image)
                    binding.txtInfo.text = imageItem.name
                }
                is ImageItem.New -> {
                    requestManager
                        .load(imageItem.downscaledBitmap)
                        .into(binding.image)
                    binding.txtInfo.text = imageItem.name
                }
            }
            binding.btnRemove.setOnClickListener { listener.removeImage(imageItem, position) }
        }
    }

    interface Listener {
        fun removeImage(item: ImageItem, position: Int)
    }
}