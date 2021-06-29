package com.amebo.core.data.datasources

import com.amebo.core.domain.BaseTopicListDataPage
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.Sort
import com.amebo.core.domain.TopicList
import com.github.michaelbull.result.Result

interface TopicListDataSource {
    suspend fun fetch(topicList: TopicList, page: Int, sort: Sort?): Result<BaseTopicListDataPage, ErrorResponse>
    suspend fun fetchCached(topicList: TopicList, page: Int, sort: Sort?): BaseTopicListDataPage?

    /**
     * populate sort table
     */
    suspend fun initialize()
}