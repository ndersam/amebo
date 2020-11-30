package com.amebo.core.data.datasources

import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.IntentParseResult
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.TopicFeed

interface MiscDataSource {
    suspend fun searchHistory(limit: Int = 5): List<String>
    suspend fun saveSearch(query: String)
    suspend fun removeSearch(term: String)
    suspend fun parseIntent(url: String): IntentParseResult?
    suspend fun removeAllSearchHistory()
    suspend fun feed(): ResultWrapper<List<TopicFeed>, ErrorResponse>
}