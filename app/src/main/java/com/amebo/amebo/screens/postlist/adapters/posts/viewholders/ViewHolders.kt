package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.databinding.ItemCollapsedPostBinding
import com.amebo.amebo.databinding.ItemDeletedPostBinding
import com.amebo.amebo.databinding.ItemFooterBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.PostItemVHListener
import com.amebo.core.CoreUtils
import com.amebo.core.domain.SimplePost


class CollapsedItemVH(private val binding: ItemCollapsedPostBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val theme = itemView.context.asTheme()
    fun bind(
        post: SimplePost,
        postByOp: Boolean,
        isTimelinePost: Boolean,
        position: Int,
        postBinder: PostItemVHListener,
        listener: PostListAdapterListener
    ) {
        binding.root.setOnClickListener { postBinder.expandPost(position) }
        binding.author.text = post.author.name
        binding.author.setOnClickListener { listener.onUserClicked(post.author) }
        binding.postTime.text = CoreUtils.howLongAgo(post.timestamp)
        binding.text.text = CoreUtils.textOnly(post.text)
        BasePostVH.styleAuthorTextView(
            binding.author,
            theme,
            postByOp && !isTimelinePost
        )
    }
}

class DeletedPostVH(binding: ItemDeletedPostBinding) : RecyclerView.ViewHolder(binding.root) {
    private val theme = itemView.context.asTheme()
    fun bind(highlight: Boolean) {
        BasePostVH.highlight(itemView, theme, highlight)
    }
}

class FooterVH(
    private val binding: ItemFooterBinding,
    private val listener: PostListAdapterListener
) : RecyclerView.ViewHolder(
    binding.root
) {
    init {
        binding.button.setOnClickListener { listener.nextPage() }
    }

    fun bind() {
        binding.button.isEnabled = if (listener.hasNextPage()) {
            binding.button.setText(R.string.next_page)
            true
        } else {
            binding.button.setText(R.string.end_of_posts)
            false
        }
    }
}



