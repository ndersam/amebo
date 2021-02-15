package com.amebo.amebo.screens.postlist.adapters.image

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.databinding.ItemImageBinding
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ImageAdapter(
    private val images: List<String>,
    private val requestManager: RequestManager,
    private val listener: Listener
) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(inflater, parent, false)
        return ViewHolder(
            binding,
            requestManager,
            listener
        )
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    class ViewHolder(
        private val binding: ItemImageBinding,
        private val requestManager: RequestManager,
        private val listener: Listener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private var positionInAdapter: Int = -1
        private lateinit var url: String
        private val progressDrawable = AppUtil.progressDrawable(itemView.context)
        private val glideListener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                binding.image.isVisible = false
                binding.btnRetry.isVisible = true
                listener.onLoadCompleted(binding.image, positionInAdapter)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                // reset original scale type
                binding.image.scaleType = ImageView.ScaleType.FIT_CENTER
                listener.onLoadCompleted(binding.image, positionInAdapter)
                return false
            }
        }

        init {
            binding.btnRetry.setOnClickListener {
                load()
            }
            progressDrawable.setStyle(CircularProgressDrawable.LARGE)
            binding.imageFrame.setOnClickListener { listener.onClick(it, positionInAdapter) }
        }

        fun bind(url: String, position: Int) {
            this.url = url
            this.positionInAdapter = position
            load()
        }

        private fun load() {
            binding.image.scaleType =
                ImageView.ScaleType.CENTER // for placeholder image alone to work
            binding.image.transitionName = ""
            binding.image.isVisible = true
            binding.btnRetry.isVisible = false

            progressDrawable.start()

            requestManager
                .load(url)
                .listener(glideListener)
                .placeholder(progressDrawable)
//                .dontAnimate()
                .into(binding.image)
                .waitForLayout()
        }
    }

    interface Listener {
        fun onLoadCompleted(view: ImageView, imagePosition: Int)
        fun onClick(view: View, position: Int)
        fun onClick(view: View, url: String)
    }


}