package com.amebo.amebo.common.extensions

import android.content.Context
import com.amebo.amebo.R
import com.amebo.core.domain.*

fun TopicList.getTitle(context: Context) = when (this) {
    is Board -> name
    is Featured -> context.getString(R.string.featured)
    is Trending -> context.getString(R.string.trending)
    is NewTopics -> context.getString(R.string.new_topics)
    is UserTopics -> context.getString(R.string.users_topics, user.name)
    is FollowedBoards -> context.getString(R.string.followed_boards)
    is FollowedTopics -> context.getString(R.string.followed_topics)
}