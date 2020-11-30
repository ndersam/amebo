package com.amebo.core.data.datasources

import com.amebo.core.domain.*

interface TopicListDataSource {
    suspend fun fetch(topicList: TopicList, page: Int, sort: Sort?): ResultWrapper<BaseTopicListDataPage, ErrorResponse>
    suspend fun fetchCached(topicList: TopicList, page: Int, sort: Sort?): BaseTopicListDataPage?

    /**
     * populate sort table
     */
    suspend fun initialize()
}