package com.amebo.core.domain

object TopicListSorts {
    // Board
    @JvmField
    val UPDATED = Sort("Updated")
    @JvmField
    val POSTS = Sort("Posts")
    @JvmField
    val VIEWS = Sort("Views")
    @JvmField
    val NEW = Sort("New")

    // Followed boards
    @JvmField
    val CREATION = Sort("Creation Time", "creationtime")
    @JvmField
    val UPDATE = Sort("Update Time", "updatetime")

    val BoardSorts = arrayOf(UPDATED, NEW, POSTS, VIEWS)

    val FollowedBoardsSorts = arrayOf(CREATION, UPDATE)
}