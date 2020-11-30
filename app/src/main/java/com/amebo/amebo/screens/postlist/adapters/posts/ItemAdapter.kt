package com.amebo.amebo.screens.postlist.adapters.posts

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.GlideApp
import com.amebo.amebo.common.Pref
import com.amebo.amebo.databinding.*
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.viewholders.*
import com.amebo.core.domain.*
import com.bumptech.glide.RequestManager

class ItemAdapter(
    fragment: Fragment,
    private val pref: Pref,
    private val listener: PostListAdapterListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    PostItemVHListener {
    private var requestManager: RequestManager = GlideApp.with(fragment)
    private var items: List<PostItem> = emptyList()
    private val handler = Handler(Looper.getMainLooper())

    var postListDataPage: PostListDataPage? = null
        set(value) {
            val list = mutableListOf<PostItem>()
            field = value
            val page = field ?: return
            var op: User? = null
            page.data.forEachIndexed { index, post ->
                val item = when (post) {
                    is SimplePost -> when {
                        // topic containing post
                        index == 0 && page.page == 0 && page is TopicPostListDataPage -> {
                            op = post.author
                            PostItem.MainPostItem(
                                post
                            )
                        }
                        // post by original poster
                        post.author == op -> PostItem.SimplePostItem(
                            post,
                            isByOriginalPoster = true
                        )
                        // regular comment
                        else -> PostItem.SimplePostItem(
                            post
                        )
                    }
                    is DeletedPost -> PostItem.DeletedPostItem(
                        post
                    )
                    is SharedPost -> PostItem.SharedPostItem(
                        post
                    )
                    is TimelinePost -> PostItem.TimelinePostItem(
                        post
                    )
                    is LikedOrSharedPost -> PostItem.LikesOrSharedPostItem(
                        post
                    )
                }
                list.add(item)
            }
            if (list.isNotEmpty()) {
                list.add(PostItem.FooterItem)
            }
            items = list
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_SIMPLE_POST -> PostItemVH(
                pref,
                ItemSimplePostBinding.inflate(inflater, parent, false),
                requestManager,
                listener
            )
            ITEM_COLLAPSED_POST -> CollapsedItemVH(
                ItemCollapsedPostBinding.inflate(inflater, parent, false)
            )
            ITEM_DELETED_POST -> DeletedPostVH(
                ItemDeletedPostBinding.inflate(inflater, parent, false)
            )
            ITEM_FOOTER -> FooterVH(
                ItemFooterBinding.inflate(inflater, parent, false),
                listener
            )
            ITEM_TIMELINE -> TimelinePostVH(
                pref,
                ItemTimelinePostBinding.inflate(inflater, parent, false),
                requestManager,
                listener
            )
            ITEM_MAIN_POST -> MainPostItemVH(
                pref,
                ItemMainPostBinding.inflate(inflater, parent, false),
                requestManager,
                listener
            )
            ITEM_SHARED_POST -> SharedPostVH(
                pref,
                ItemSharedPostBinding.inflate(inflater, parent, false),
                requestManager,
                listener
            )
            ITEM_LIKES_AND_SHARES -> LikesAndSharesPostVH(
                pref,
                ItemSharedPostBinding.inflate(inflater, parent, false),
                requestManager,
                listener
            )
            else -> throw IllegalArgumentException("Unknown view type '$viewType'")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is PostItem.SimplePostItem -> when (isCollapsed(position)) {
                true -> {
                    val vh = holder as CollapsedItemVH
                    vh.bind(item.post, item.isByOriginalPoster, false, position, this, listener)
                }
                false -> {
                    val vh = holder as PostItemVH
                    vh.bind(item.post, item.isByOriginalPoster, position, item.highlight)
                }
            }
            is PostItem.TimelinePostItem -> when (isCollapsed(position)) {
                true -> {
                    val vh = holder as CollapsedItemVH
                    vh.bind(
                        item.timelinePost.post,
                        item.timelinePost.isMainPost,
                        true,
                        position,
                        this,
                        listener
                    )
                }
                false -> {
                    val vh = holder as TimelinePostVH
                    vh.bind(item, position)
                }
            }
            is PostItem.SharedPostItem -> when (isCollapsed(position)) {
                true -> {
                    val vh = holder as CollapsedItemVH
                    vh.bind(
                        item.sharedPost.post,
                        item.sharedPost.isMainPost,
                        true,
                        position,
                        this,
                        listener
                    )
                }
                false -> {
                    val vh = holder as SharedPostVH
                    vh.bind(item, position)
                }
            }
            is PostItem.MainPostItem -> {
                val vh = holder as MainPostItemVH
                vh.bind(item, position)
            }
            is PostItem.LikesOrSharedPostItem -> when (isCollapsed(position)) {
                true -> {
                    val vh = holder as CollapsedItemVH
                    vh.bind(
                        item.likedOrSharedPost.post,
                        item.likedOrSharedPost.isMainPost,
                        true,
                        position,
                        this,
                        listener
                    )
                }
                false -> {
                    val vh = holder as LikesAndSharesPostVH
                    vh.bind(item, position)
                }
            }
            is PostItem.FooterItem -> {
                val vh = holder as FooterVH
                vh.bind()
            }
            is PostItem.DeletedPostItem -> {
                val vh = holder as DeletedPostVH
                vh.bind(item.highlight)
            }
        }
    }

    private fun isCollapsed(position: Int) = listener.isItemCollapsed(position)


    fun clear() {
        items = emptyList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is PostItem.SimplePostItem ->
            if (isCollapsed(position))
                ITEM_COLLAPSED_POST
            else
                ITEM_SIMPLE_POST
        is PostItem.MainPostItem -> ITEM_MAIN_POST
        is PostItem.DeletedPostItem -> ITEM_DELETED_POST
        is PostItem.SharedPostItem -> ITEM_SHARED_POST
        is PostItem.FooterItem -> ITEM_FOOTER
        is PostItem.TimelinePostItem -> if (isCollapsed(position))
            ITEM_COLLAPSED_POST
        else
            ITEM_TIMELINE
        is PostItem.LikesOrSharedPostItem -> ITEM_LIKES_AND_SHARES
    }

    override fun expandPost(position: Int): Boolean {
        setItemCollapsed(position, false)
        return true
    }

    override fun collapsePost(position: Int): Boolean {
        setItemCollapsed(position, true)
        return true
    }

    fun collapseAllPosts() {
        items.forEachIndexed { index, it ->
            if (it is PostItem.SimplePostItem) {
                it.collapsed = true
                listener.onItemCollapsed(index)
            }
        }
        notifyDataSetChanged()
    }

    fun expandAllPosts() {
        items.forEachIndexed { index, it ->
            if (it is PostItem.SimplePostItem) {
                it.collapsed = false
                listener.onItemExpanded(index)
            }
        }
        notifyDataSetChanged()
    }


    /**
     * @param postPosition - position of postView in the adapter. If the adapter is in another adapter ([androidx.recyclerview.widget.ConcatAdapter]),
     * this position must be same as the position in the parent adapter.
     * @param imagePosition - position of image in image recyclerView
     */
    fun scrollToImageAt(recyclerView: RecyclerView, postPosition: Int, imagePosition: Int) {
        recyclerView.post {
            when (val vh = recyclerView.findViewHolderForAdapterPosition(postPosition)) {
                is PostItemVH -> {
                    vh.imageRecyclerView.scrollToPosition(imagePosition)
                }
                is MainPostItemVH -> {
                    vh.imageRecyclerView.scrollToPosition(imagePosition)
                }
            }
        }
    }

    fun findPostPosition(postID: String): Int {
        return items.indexOfFirst {
            if (it is PostItem.MainPostItem && it.post.id == postID) {
                true
            } else if (it is PostItem.DeletedPostItem && it.deletedPost.name == postID) {
                true
            } else it is PostItem.SimplePostItem && it.post.id == postID
        }
    }

    fun highlightItem(index: Int) {
        val postId = findPostIdAt(index) ?: return
        changeHighlight(index, true)
        handler.postDelayed({
            if (index < itemCount && postId == findPostIdAt(index)) {
                changeHighlight(index, false)
            }
        }, 2_000)
    }

    private fun findPostIdAt(index: Int): String? = when (val item = items[index]) {
        is PostItem.MainPostItem -> item.post.id
        is PostItem.SimplePostItem -> item.post.id
        is PostItem.DeletedPostItem -> item.deletedPost.name
        else -> null
    }

    private fun changeHighlight(index: Int, highlight: Boolean) {
        when (val item = items[index]) {
            is PostItem.MainPostItem -> {
                item.highlight = highlight
            }
            is PostItem.SimplePostItem -> {
                item.highlight = highlight
            }
            is PostItem.DeletedPostItem -> {
                item.highlight = highlight
            }
            else -> return
        }
        notifyItemChanged(index)
    }

    private fun setItemCollapsed(position: Int, collapsed: Boolean) {
        when (val item = items[position]) {
            is PostItem.SimplePostItem -> {
                item.collapsed = collapsed
            }
            is PostItem.TimelinePostItem -> {
                item.collapsed = collapsed
            }
            else -> return
        }

        notifyItemChanged(position)
        if (collapsed) {
            listener.onItemCollapsed(position)
        } else {
            listener.onItemExpanded(position)
        }
    }


    companion object {
        const val ITEM_SIMPLE_POST = 0
        const val ITEM_COLLAPSED_POST = 1
        const val ITEM_DELETED_POST = 2
        const val ITEM_SHARED_POST = 3
        const val ITEM_FOOTER = 4
        const val ITEM_TIMELINE = 5
        const val ITEM_MAIN_POST = 6
        const val ITEM_LIKES_AND_SHARES = 8
    }

}


