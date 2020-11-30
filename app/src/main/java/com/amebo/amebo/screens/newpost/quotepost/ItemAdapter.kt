package com.amebo.amebo.screens.newpost.quotepost

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.SpanHandler
import com.amebo.amebo.databinding.ItemQuotablePostBinding
import com.amebo.core.domain.QuotablePost
import com.amebo.core.domain.Topic

class ItemAdapter(private val items: List<QuotablePost>, private val listener: Listener) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemQuotablePostBinding.inflate(inflater, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemQuotablePostBinding, private val listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var post: QuotablePost

        private val handler = object : SpanHandler {
            override fun onImageClick(view: TextView, imagePosition: Int) {}

            override fun onImageClick(view: TextView, url: String) {}


            override fun onTopicLinkClick(view: TextView, topic: Topic) {}

            override fun onYoutubeLinkClick(view: TextView, url: String) {}

            override fun onReferencedPostClick(
                view: TextView,
                postID: String,
                author: String
            ) {
            }

            override fun onPostLinkClick(view: TextView, postID: String) {}
            override fun onUnknownLinkClick(url: String) {}
        }

        init {
            binding.overlay.setOnClickListener {
                listener.onPostSelected(post)
            }
            binding.root.setOnClickListener {
                listener.onPostSelected(post)
            }
        }

        fun bind(post: QuotablePost) {
            this.post = post
            binding.postAuthor.text = post.author.name
            binding.postIndex.text = post.number.toString()
            binding.text.setData(
                listener.listenerLifecycle,
                post.post,
                listener.useDeviceEmojis,
                handler
            )
        }

    }

    interface Listener {
        val listenerLifecycle: Lifecycle
        val useDeviceEmojis: Boolean
        fun onPostSelected(quotablePost: QuotablePost)
    }
}