package com.amebo.amebo.screens.postlist.adapters.posts

interface PostItemVHListener {
    fun expandPost(position: Int): Boolean
    fun collapsePost(position: Int): Boolean
}