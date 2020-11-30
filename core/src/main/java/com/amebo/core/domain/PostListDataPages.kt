package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class PostListDataPage(
    open val data: List<Post>,
    open val page: Int,
    open val last: Int,
    open var postToScrollTo: String?
) : Parcelable


@Parcelize
class TimelinePostsListDataPage(
    override val data: List<Post>,
    override val page: Int,
    override val last: Int,
    override var postToScrollTo: String? = null
) : PostListDataPage(data, page, last, postToScrollTo), Parcelable

@Parcelize
class SharedPostsListDataPage(
    override val data: List<Post>,
    override val page: Int,
    override val last: Int,
    override var postToScrollTo: String? = null
) : PostListDataPage(data, page, last, postToScrollTo)

@Parcelize
class TopicPostListDataPage(
    val topic: Topic,
    override val data: List<Post>,
    override val page: Int,
    override val last: Int,
    val usersViewing: List<User>,
    val views: Int,
    val isHiddenFromUser: Boolean,
    val isFollowingTopic: Boolean,
    val isClosed: Boolean,
    val followOrUnFollowTopicUrl: String? = null,
    override var postToScrollTo: String? = null,
    val relatedTopics: List<Topic> = emptyList()
) : PostListDataPage(data, page, last, postToScrollTo)

@Parcelize
class LikedOrSharedPostListDataPage(
    override val data: List<Post>,
    override val page: Int,
    override val last: Int,
    val numLikes: Int,
    val numShares: Int,
    override var postToScrollTo: String? = null
) : PostListDataPage(data, page, last, postToScrollTo)