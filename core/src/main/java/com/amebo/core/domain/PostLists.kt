package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class PostList

/**
 * Models a Nairaland Topic.
 * The actual post data is contained in series of [TopicListDataPage] objects.
 */
@Parcelize
data class Topic(
    val title: String,
    val id: Int,
    val slug: String,
    val author: User? = null,
    val timestamp: Long? = null,
    val hasNewPosts: Boolean = false,
    var mainBoard: Board? = null,
    val postCount: Int? = null,
    val viewCount: Int? = null,
    val linkedPage: Int = 0,
    val refPost: String? = null,
    val isOldUrl: Boolean = false
) : PostList(), Parcelable

/**
 * This class models lists, whose pages comprise [TimelinePost] posts from various Nairaland topics and Nairaland boards.
 * Actual post data is contained in [TimelinePostsListDataPage]
 */
sealed class Timeline : PostList()

object RecentPosts : Timeline()

class Mentions(val user: User) : Timeline()

object PostsByPeopleYouAreFollowing : Timeline()

object SharedPosts : Timeline()

object LikesAndShares : Timeline()

class UserPosts(val user: User) : Timeline()

object MyLikes: Timeline()

object MySharedPosts: Timeline()

@Parcelize
class SearchQuery(
    val query: String,
    val onlyShowTopicPosts: Boolean,
    val onlyShowImages: Boolean,
    val board: Board?
) : Timeline(), Parcelable