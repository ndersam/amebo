package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class TopicList : Parcelable {
    val hasSort: Boolean get() = this is Board || this is FollowedBoards

    val sorts
        get() = when (this) {
            is Board -> TopicListSorts.BoardSorts
            is FollowedBoards -> TopicListSorts.FollowedBoardsSorts
            else -> null
        }
}

@Parcelize
object Featured : TopicList()

@Parcelize
object Trending : TopicList()

@Parcelize
class UserTopics(val user: User) : TopicList()

@Parcelize
object FollowedBoards : TopicList()

@Parcelize
object NewTopics : TopicList()

@Parcelize
object FollowedTopics : TopicList()

@Parcelize
class Board(val name: String, val url: String, val id: Int = -1) : TopicList()

/*
    Information crawled from topic list pages
 */
sealed class BaseTopicListDataPage(
        open val data: List<Topic>,
        open val page: Int,
        open val last: Int
) : Parcelable {
    val hasNextPage get() = page < last
    val hasPrevPage get() = page > 0
}

@Parcelize
open class TopicListDataPage(
        override val data: List<Topic>,
        override val page: Int,
        override val last: Int
) : BaseTopicListDataPage(data, page, last)

@Parcelize
class FollowedBoardsDataPage(
    override val data: List<Topic>,
    override val page: Int,
    override val last: Int,
    val boards: MutableList<Pair<Board, String>>
) : BaseTopicListDataPage(data, page, last)

@Parcelize
class BoardsDataPage(
    override val data: List<Topic>,
    override val page: Int,
    override val last: Int,
    val usersViewing: List<User>,
    val relatedBoards: String,
    val numGuestsViewing: Int,
    val numUsersViewing: Int,
    val isFollowing: Boolean,
    val moderators: List<User>,
    val boardInfo: String,
    val followOrUnFollowUrl: String? = null,
    val boardId: Int? = null
) : TopicListDataPage(data, page, last)

