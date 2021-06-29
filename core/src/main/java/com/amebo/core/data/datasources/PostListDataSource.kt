package com.amebo.core.data.datasources

import com.amebo.core.domain.*
import com.github.michaelbull.result.Result

interface PostListDataSource {
    suspend fun fetchCached(postList: PostList, page: Int): TopicPostListDataPage?
    suspend fun fetch(postList: PostList, page: Int): Result<PostListDataPage, ErrorResponse>
    suspend fun allViewedTopicIds(): MutableSet<Int>
    suspend fun allViewedTopics(): List<Topic>
    suspend fun addViewedTopic(topic: Topic)
    suspend fun updateViewedTopic(topic: Topic)
    suspend fun recentTopics(count: Int): List<Topic>
    suspend fun removeVisitedTopic(topic: Topic)
    suspend fun removeAllTopics()
    suspend fun fetchPageWithPost(postId: String): Result<TopicPostListDataPage, ErrorResponse>
}