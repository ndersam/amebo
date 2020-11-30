package com.amebo.core.data.datasources.impls

import androidx.annotation.RestrictTo
import com.amebo.core.Database
import com.amebo.core.apis.TopicListApi
import com.amebo.core.crawler.ParseException
import com.amebo.core.crawler.UnauthorizedAccessException
import com.amebo.core.crawler.topicList.*
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.TopicListDataSource
import com.amebo.core.domain.*
import com.amebo.core.extensions.map
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class TopicListDataSourceImpl @Inject constructor(
    private val user: User?,
    private val api: TopicListApi,
    private val database: Database,
    private val context: CoroutineContextProvider
) : TopicListDataSource {

    override suspend fun initialize() = withContext(context.IO) {
        database.transaction {
            TopicListSorts.BoardSorts.forEach {
                database.topicListPagesQueries.insertSort(it.value)
            }
            TopicListSorts.FollowedBoardsSorts.forEach {
                database.topicListPagesQueries.insertSort(it.value)
            }
        }
    }

    override suspend fun fetch(
        topicList: TopicList,
        page: Int,
        sort: Sort?
    ): ResultWrapper<BaseTopicListDataPage, ErrorResponse> = withContext(context.IO) {
        val response = when (topicList) {
            is Featured -> api.fetchFeaturedSoup(page)
            is Board -> api.fetchBoardSoup(topicList.url, sort!!.value, page)
            is FollowedBoards -> api.fetchFollowedBoardTopics(sort!!.value, page)
            is FollowedTopics -> api.fetchFollowedTopics(page)
            is Trending -> api.fetchTrendingSoup(page)
            is UserTopics -> api.fetchUserTopics(topicList.user.slug, page)
            is NewTopics -> api.fetchNewSoup(page)
        }

        try {
            response.map({
                val document = it.body
                val data = parseResponseBody(topicList, document, page)
                // saving body here, after parsing is successful (no exceptions thrown)
                saveTopicListDataPage(topicList, sort, page, it.body.outerHtml())
                ResultWrapper.Success(data)
            },
                {
                    ResultWrapper.Failure(ErrorResponse.Network)
                })
        } catch (e: ParseException) {
            ResultWrapper.failure(ErrorResponse.Parse)
        } catch (e: UnauthorizedAccessException) {
            ResultWrapper.failure(ErrorResponse.UnAuthorized)
        }
    }

    private fun parseResponseBody(topicList: TopicList, document: Document, page: Int) =
        when (topicList) {
            is Featured -> parseFeaturedTopics(document, page)
            is Board -> parseBoardTopics(document, page, topicList)
            is FollowedBoards -> parseFollowedBoards(document, page)
            is FollowedTopics -> parseFollowedTopics(document, page)
            is Trending -> parseTrendingTopics(document, page)
            is UserTopics -> parseUserTopics(document, page)
            is NewTopics -> parseNewTopics(document, page)
        }


    override suspend fun fetchCached(
        topicList: TopicList,
        page: Int,
        sort: Sort?
    ): BaseTopicListDataPage? = withContext(context.IO) {
        val topicListType = topicList.topicListType()
        val boardId = when (topicList) {
            is Board -> database.boardQueries.select(topicList.url).executeAsOne().id.toInt()
            else -> null
        }
        val sortId = when (sort) {
            is Sort -> database.topicListPagesQueries.findSortByValue(sort.value)
                .executeAsOneOrNull()?.id
            else -> null
        }

        when (val data = database.topicListPagesQueries.findtopicListPageData(
            type_id = topicListType,
            page = page,
            board_id = boardId,
            sort_id = sortId,
            user_slug = user?.slug
        ).executeAsOneOrNull()) {
            is String -> parseResponseBody(topicList, Jsoup.parse(data), page)
            else -> null
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal fun saveTopicListResponse(topicList: TopicList, sort: Sort?, page: Int, data: String) =
        saveTopicListDataPage(topicList, sort, page, data)

    private fun saveTopicListDataPage(topicList: TopicList, sort: Sort?, page: Int, data: String) {
        database.transaction {
            val topicListType = topicList.topicListType()
            val boardId = when (topicList) {
                is Board -> database.boardQueries.select(topicList.url).executeAsOne().id.toInt()
                else -> null
            }
            val sortId = findSortId(sort)

            // if data exits, update fields and return
            when (val dataPage = database.topicListPagesQueries.findtopicListPage(
                type_id = topicListType,
                sort_id = sortId,
                board_id = boardId,
                page = page,
                user_slug = user?.slug
            ).executeAsOneOrNull()) {
                null -> {
                    // continue
                }
                else -> {
                    database.topicListPagesQueries.updateTopicListPage(
                        timestamp = System.currentTimeMillis(),
                        data = data,
                        id = dataPage.id
                    )
                    return@transaction
                }
            }

            // Find topicListId.
            // If not exists, create new entry
            val topicListId = when (val topicListId = database.topicListQueries.findTopicListIdBy(
                type_id = topicListType,
                board_id = boardId
            ).executeAsOneOrNull()) {
                null -> {
                    database.topicListQueries.insertTopicListId(
                        type_id = topicListType,
                        board_id = boardId
                    )
                    database.topicListQueries.findTopicListIdBy(
                        type_id = topicListType,
                        board_id = boardId
                    ).executeAsOne()
                }
                else -> topicListId
            }


            // Finally, save data
            database.topicListPagesQueries.insertTopicListPage(
                topiclist_id = topicListId,
                page = page,
                data = data,
                timestamp = System.currentTimeMillis(),
                sort = sortId,
                user_slug = user?.slug
            )
        }

    }

    private fun findSortId(sort: Sort?) = when (sort) {
        // is NonNull, check if exists in database
        is Sort -> database.topicListPagesQueries.findSortByValue(sort.value).executeAsOne().id
        else -> null
    }

    companion object {

        private fun TopicList.topicListType(): Int = when (this) {
            is Featured -> 0
            is Trending -> 1
            is Board -> 2
            is NewTopics -> 3
            is UserTopics -> 4
            is FollowedTopics -> 5
            is FollowedBoards -> 6
        }


    }
}