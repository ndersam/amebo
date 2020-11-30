package com.amebo.amebo.common.extensions

import android.content.Context
import com.amebo.amebo.R
import com.amebo.core.domain.*

fun PostList.getPostListTitle(context: Context) = when (this) {
    is Topic -> title
    is UserPosts -> context.getString(R.string.users_posts, user.name)
    is SearchQuery -> context.getString(R.string.search_results_for, query)
    is Mentions -> context.getString(R.string.mentions)
    is RecentPosts -> context.getString(R.string.recent_posts)
    is SharedPosts -> context.getString(R.string.shared_with_me)
    is PostsByPeopleYouAreFollowing -> context.getString(R.string.posts_by_people_you_are_following)
    is LikesAndShares -> context.getString(R.string.likes_and_shares)
    is MyLikes -> context.getString(R.string.my_likes)
    is MySharedPosts -> context.getString(R.string.my_shared_posts)
}