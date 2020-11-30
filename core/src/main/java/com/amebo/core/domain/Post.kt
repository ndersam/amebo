package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class Post : Parcelable

@Parcelize
class SimplePost(
    val author: User,
    val topic: Topic,
    val id: String,
    val url: String,
    val text: String,
    val timestamp: Long,
    var likes: Int,
    var shares: Int,
    var isShared: Boolean = false,
    var isLiked: Boolean = false,
    val likeUrl: String? = null,
    val shareUrl: String? = null,
    val editUrl: String? = null,
    val reportUrl: String? = null,
    /**
     * Urls of mentioned posts. Each url is in the regex form '/post/\d+'
     */
    val parentQuotes: List<String> = emptyList(),
    val images: List<String> = emptyList()
) : Post()

@Parcelize
class TimelinePost(val isMainPost: Boolean, val post: SimplePost) : Post()

@Parcelize
class DeletedPost(val name: String) : Post()

@Parcelize
class SharedPost(
    val sharer: User,
    val extra: String,
    val shareTime: Long,
    val isMainPost: Boolean,
    val post: SimplePost
) : Post()

@Parcelize
class LikedOrSharedPost(
    val timestamp: Long,
    val kind: Kind,
    val isMainPost: Boolean,
    val post: SimplePost
) : Post() {
    sealed class Kind: Parcelable {
        @Parcelize
        object Liked: Kind()
        @Parcelize
        class  Shared(val isYou: Boolean, val sharer: User? ): Kind()
    }
}