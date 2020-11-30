package com.amebo.amebo.screens.postlist.adapters.posts

import com.amebo.core.domain.*

sealed class PostItem {
    class SimplePostItem(
        val post: SimplePost,
        var collapsed: Boolean = false,
        val isByOriginalPoster: Boolean = false,
        var highlight: Boolean = false
    ) : PostItem()

    class MainPostItem(val post: SimplePost, var highlight: Boolean = false) : PostItem()
    class DeletedPostItem(val deletedPost: DeletedPost, var highlight: Boolean = false) : PostItem()
    class SharedPostItem(val sharedPost: SharedPost) : PostItem()
    class LikesOrSharedPostItem(val likedOrSharedPost: LikedOrSharedPost) : PostItem()
    class TimelinePostItem(val timelinePost: TimelinePost, var collapsed: Boolean = false) :
        PostItem()

    object FooterItem : PostItem()
}