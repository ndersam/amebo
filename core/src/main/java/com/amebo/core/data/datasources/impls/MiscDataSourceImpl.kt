package com.amebo.core.data.datasources.impls

import android.net.Uri
import com.amebo.core.Database
import com.amebo.core.apis.MiscApi
import com.amebo.core.common.CoreUtils
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.crawler.parseFeedCrawler
import com.amebo.core.crawler.topicList.parseTopicUrl
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.MiscDataSource
import com.amebo.core.domain.*
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class MiscDataSourceImpl @Inject internal constructor(
    private val user: User?,
    private val database: Database,
    private val api: MiscApi,
    private val context: CoroutineContextProvider
) :
    MiscDataSource {
    override suspend fun searchHistory(limit: Int): List<String> = withContext(context.IO) {
        database.searchHistoryQueries.selectNRecent(limit.toLong()).executeAsList()
    }

    override suspend fun saveSearch(query: String): Unit = withContext(context.IO) {
        database.searchHistoryQueries.insert(query, System.currentTimeMillis().toDouble())
    }

    override suspend fun removeSearch(term: String): Unit = withContext(context.IO) {
        database.searchHistoryQueries.deleteThis(term)
    }

    override suspend fun removeAllSearchHistory(): Unit = withContext(context.IO) {
        database.searchHistoryQueries.deleteAll()
    }

    override suspend fun parseIntent(url: String): IntentParseResult? {

        val uri = Uri.parse(url)
        val first = uri.pathSegments.firstOrNull()?.toLowerCase(Locale.ROOT)
        val second = uri.pathSegments.getOrNull(1)

        // all lead or redirect to homePage
        if (first == null || (first == "nigeria" && second == null)) return null

        // FEATURED
        if (first == "links" || first == "news") {
            return IntentParseResult.TopicListResult(
                Featured,
                second?.toInt() ?: 0
            )
        }

        // IGNORE THESE
        if (CoreUtils.isIgnoredPathSegment(first)) {
            return null
        }

        // Ignore campaign
        if (first == "campaign" || first == "adrates") {
            return null
        }

        // TOPIC
        if (isInteger(first) || (first == "nigeria")) {
            val result = parseTopicUrl(url) ?: return null
            return IntentParseResult.PostListResult(
                Topic(
                    title = result.slug,
                    id = result.topicId,
                    isOldUrl = result.isOldUrl,
                    refPost = result.refPost,
                    linkedPage = result.page,
                    slug = result.slug
                ),
                result.page
            )
        }


        // BOARD
        val board = database.boardQueries.select(first) { name, slug, id ->
            Board(
                name,
                slug,
                id = id.toInt()
            )
        }.executeAsOneOrNull()
        if (board != null) {
            return IntentParseResult.TopicListResult(
                board,
                second?.toInt() ?: 0
            )
        }

        // OTHERS
        if (first == "trending") {
            return IntentParseResult.TopicListResult(
                Trending,
                second?.toInt() ?: 0
            )
        }
        if (first == "topics") {
            return IntentParseResult.TopicListResult(
                NewTopics,
                second?.toInt() ?: 0
            )
        }
        if (first == "followedboards") {
            return IntentParseResult.TopicListResult(
                FollowedBoards,
                second?.toInt() ?: 0
            )
        }
        if (first == "followed") {
            return IntentParseResult.TopicListResult(
                FollowedTopics,
                second?.toInt() ?: 0
            )
        }
        if (first == "mentions") {
            return IntentParseResult.PostListResult(
                Mentions(user!!),
                second?.toInt() ?: 0
            )
        }
        if (first == "search") {
            // e.g. /search/{query}/{topics_only}/{board}/{images_only}/{page}
            val query: String
            val onlyTopics: Boolean
            val onlyImages: Boolean
            val board: Board?
            if (uri.pathSegments.size > 1) {
                query = uri.pathSegments[1]
                onlyTopics = uri.pathSegments[2].toInt() == 1
                onlyImages = uri.pathSegments[4].toInt() == 1
                board = selectBoardById(uri.pathSegments[3].toInt())
            } else {
                // e.g. /search?q={query}
                query = uri.getQueryParameter("q") ?: "" // empty query open search window
                onlyTopics = uri.getQueryParameter("topicsonly")?.toIntOrNull() == 1
                onlyImages = uri.getQueryParameter("imagesonly")?.toIntOrNull() == 1
                board = selectBoardById(uri.getQueryParameter("board")?.toIntOrNull() ?: 0)
            }
            val searchQuery = SearchQuery(
                query,
                onlyShowTopicPosts = onlyTopics,
                onlyShowImages = onlyImages,
                board = board
            )
            return IntentParseResult.PostListResult(
                searchQuery, second?.toInt() ?: 0
            )
        }

        if (first == "shared") {
            return IntentParseResult.PostListResult(
                SharedPosts,
                second?.toInt() ?: 0
            )
        }

        if (first == "following") {
            return IntentParseResult.PostListResult(
                PostsByPeopleYouAreFollowing,
                second?.toInt() ?: 0
            )
        }
        if (first == "likesandshares") {
            val pageNum = if (uri.pathSegments.size == 3)
                uri.lastPathSegment!!.toInt()
            else 0
            return IntentParseResult.PostListResult(LikesAndShares, pageNum)
        }

        if (first == "recent") {
            return IntentParseResult.PostListResult(
                RecentPosts,
                second?.toInt() ?: 0
            )
        }
        if (first == "mylikes") {
            return IntentParseResult.PostListResult(MyLikes, second?.toInt() ?: 0)
        }

        // FIXME
        // Assume it's a user
        return IntentParseResult.UserResult(User(first))
    }

    override suspend fun feed(): Result<List<TopicFeed>, ErrorResponse> =
        withContext(context.IO) {
            api.fetchFeed()
                .awaitResult { Ok(parseFeedCrawler(it)) }
        }

    private fun selectBoardById(boardId: Int): Board? {
        return database.boardQueries.selectById(boardId.toLong()) { name, slug, id ->
            Board(
                name,
                slug,
                id.toInt()
            )
        }.executeAsOneOrNull()
    }

    private fun isInteger(string: String) = string.toIntOrNull() != null

    private fun isBoard(string: String) =
        database.boardQueries.select(string).executeAsOneOrNull() != null


    companion object {
        fun String.e(another: String) = this.equals(another, ignoreCase = true)
    }

}