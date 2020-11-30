package com.amebo.amebo.common.routing

import com.amebo.amebo.R

sealed class TabItem(val id: Long, val titleRes: Int, val drawableRes: Int) {

    object RecentPosts : TabItem(
        1L,
        R.string.recent_posts,
        R.drawable.ic_new_releases_24dp
    )

    object Mentions : TabItem(
        2L,
        R.string.mentions,
        R.drawable.ic_megaphone
    )

    object LikesAndShares : TabItem(
        3L,
        R.string.likes_and_shares,
        R.drawable.ic_favorite_24dp
    )

    object Profile : TabItem(
        4L,
        R.string.profile,
        R.drawable.ic_person_24dp
    )

    object Topics : TabItem(
        5L,
        R.string.topics,
        R.drawable.ic_home_24dp
    )

    object SharedWithMe : TabItem(
        6L,
        R.string.shared_with_me,
        R.drawable.ic_whatshot_24dp
    )

    object Following : TabItem(
        7L,
        R.string.following,
        R.drawable.ic_following
    )

    object MyLikes: TabItem(
        8L,
        R.string.my_likes,
        R.drawable.ic_heart_outline
    )

    object MyShares: TabItem(
        9L,
        R.string.my_shared_posts,
        R.drawable.ic_share_outline
    )

    object MyFollowers: TabItem(
        10L,
        R.string.followers,
        R.drawable.ic_account_group
    )

    object Inbox: TabItem(
        12L,
        R.string.inbox,
        R.drawable.ic_email_24dp
    )
}