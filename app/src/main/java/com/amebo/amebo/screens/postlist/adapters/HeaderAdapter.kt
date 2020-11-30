package com.amebo.amebo.screens.postlist.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.databinding.ItemLikesSharesHeaderBinding
import com.amebo.amebo.databinding.ItemPostListHeaderBinding
import com.amebo.core.domain.*

class HeaderAdapter(private val postList: PostList, private val listener: Listener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataPage: PostListDataPage? = null

    fun setData(page: PostListDataPage) {
        this.dataPage = page
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_likes_shares_header -> LikesAndSharesHeaderItemVH(
                ItemLikesSharesHeaderBinding.inflate(inflater, parent, false)
            )
            R.layout.item_post_list_header -> TopicViewHolder(
                ItemPostListHeaderBinding.inflate(inflater, parent, false),
                postList,
                listener
            )
            else -> throw IllegalStateException("Unknown viewType '$viewType'")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TopicViewHolder -> {
                holder.bind(dataPage ?: return)
            }
            is LikesAndSharesHeaderItemVH -> {
                holder.bind(dataPage as? LikedOrSharedPostListDataPage ?: return)
            }
        }
    }

    override fun getItemCount(): Int = if (postList is Topic || postList is LikesAndShares) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return when (postList) {
            is Topic -> R.layout.item_post_list_header
            is LikesAndShares -> R.layout.item_likes_shares_header
            else -> throw IllegalStateException("Unsupported postList type ${postList::class.java.name}")
        }
    }

    private class TopicViewHolder(
        private val binding: ItemPostListHeaderBinding,
        postList: PostList,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var board: Board

        init {
            binding.title.text = (postList as Topic).title
            binding.topicList.setOnClickListener { listener.onBoardClicked(board) }
            binding.topicList.isInvisible = true
            binding.pageNumberInfo.setOnClickListener { listener.onPageMetaClicked() }
            binding.pageNumberInfo.isInvisible = true
        }

        fun bind(dataPage: PostListDataPage) {
            val topicList = dataPage as TopicPostListDataPage
            if (topicList.isHiddenFromUser) {
                binding.title.setText(R.string.topic_hidden)
                return
            }
            board = topicList.topic.mainBoard ?: return
            binding.topicList.isVisible = true
            binding.pageNumberInfo.isVisible = true
            binding.title.text = topicList.topic.title
            binding.topicList.text = topicList.topic.mainBoard!!.name
            val ctx = itemView.context
            binding.postViews.text =
                ctx.getString(R.string.view_count, AppUtil.largeNumberFormatter(topicList.views))
            binding.pageNumberInfo.text = ctx.getString(
                R.string.page_x_of_x,
                (dataPage.page + 1).toString(),
                (dataPage.last + 1).toString()
            )
        }
    }

    private class LikesAndSharesHeaderItemVH(private val binding: ItemLikesSharesHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(postList: LikedOrSharedPostListDataPage) {
            binding.text.text = itemView.context.getString(
                R.string.you_have_likes_and_shares,
                postList.numLikes,
                postList.numShares
            )
        }
    }

    interface Listener {
        fun onBoardClicked(board: Board)
        fun onPageMetaClicked()
    }


}