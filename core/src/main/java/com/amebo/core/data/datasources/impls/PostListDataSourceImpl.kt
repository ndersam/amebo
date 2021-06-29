package com.amebo.core.data.datasources.impls

import com.amebo.core.Database
import com.amebo.core.apis.PostListApi
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.common.extensions.awaitResultResponse
import com.amebo.core.crawler.postList.parseLikesAndShares
import com.amebo.core.crawler.postList.parseSharedPosts
import com.amebo.core.crawler.postList.parseTimelinePosts
import com.amebo.core.crawler.postList.parseTopicPosts
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.PostListDataSource
import com.amebo.core.domain.*
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

internal class PostListDataSourceImpl @Inject constructor(
    private val user: User?,
    private val api: PostListApi,
    private val context: CoroutineContextProvider,
    private val database: Database
) : PostListDataSource {

    override suspend fun fetch(
        postList: PostList,
        page: Int
    ): Result<PostListDataPage, ErrorResponse> = withContext(context.IO) {
        when (postList) {
            is Topic -> fetchTopicPosts(postList, page)
            is UserPosts -> fetchUserPosts(postList, page)
            is RecentPosts -> fetchRecentPosts(page)
            is SearchQuery -> search(postList, page)
            is Mentions -> fetchMentions(postList, page)
            is PostsByPeopleYouAreFollowing -> fetchPostsByPeopleYouAreFollowing(page)
            is SharedPosts -> fetchPostsSharedWithMe(page)
            is LikesAndShares -> fetchLikesAndShares(page)
            is MyLikes -> fetchMyLikes(page)
            is MySharedPosts -> fetchMyShares(page)
        }
    }

    override suspend fun fetchCached(postList: PostList, page: Int): TopicPostListDataPage? =
        withContext(context.IO) {
            if (postList !is Topic) {
                return@withContext null
            }
            when (val data =
                database.postListPagesQueries.findData(
                    topic_id = postList.id.toString(),
                    page = page,
                    user_slug = user?.slug
                )
                    .executeAsOneOrNull()) {
                is String -> Jsoup.parse(data).parseTopicPosts(postList, page).component1()
                else -> null
            }
        }

    private suspend fun fetchTopicPosts(
        topic: Topic,
        page: Int
    ): Result<PostListDataPage, ErrorResponse> =
        api.fetchTopicPosts(topic.id.toString(), topic.slug, page).awaitResult { soup ->
            soup.parseTopicPosts(topic, page).onSuccess {
                it.apply {
                    postToScrollTo = topic.refPost
                }
                // save to db after parsing is successful
                saveTopicPostListData(topic, soup.outerHtml(), page)
            }
        }

    private suspend fun fetchUserPosts(
        userPosts: UserPosts,
        page: Int
    ): Result<PostListDataPage, ErrorResponse> =
        api.fetchUserPosts(userPosts.user.slug, page)
            .awaitResult {
                it.parseTimelinePosts("/${userPosts.user.slug}/posts/$page", page)
            }

    private suspend fun fetchRecentPosts(
        page: Int
    ): Result<PostListDataPage, ErrorResponse> = api.fetchRecent(page)
        .awaitResult { it.parseTimelinePosts("/recent/$page", page) }

    private suspend fun fetchPostsByPeopleYouAreFollowing(
        page: Int
    ): Result<PostListDataPage, ErrorResponse> = api.fetchFollowing(page)
        .awaitResult { it.parseTimelinePosts("/following/$page", page) }

    private suspend fun fetchMyLikes(
        page: Int
    ): Result<PostListDataPage, ErrorResponse> = api.fetchMyLikes(page)
        .awaitResult { it.parseTimelinePosts("/mylikes/$page", page) }

    private suspend fun fetchMyShares(
        page: Int
    ): Result<PostListDataPage, ErrorResponse> = api.fetchMyShares(page)
        .awaitResult { it.parseTimelinePosts("/myshares/$page", page) }

    private suspend fun fetchPostsSharedWithMe(page: Int): Result<PostListDataPage, ErrorResponse> =
        api.fetchPostsSharedWithMe(page)
            .awaitResult { it.parseSharedPosts(page) }

    private suspend fun fetchLikesAndShares(page: Int): Result<PostListDataPage, ErrorResponse> =
        api.fetchLikesAndShares(page)
            .awaitResult { it.parseLikesAndShares(page) }

    private suspend fun fetchMentions(
        mentions: Mentions,
        page: Int
    ): Result<PostListDataPage, ErrorResponse> = when (page) {
        0 -> {
            api.fetchMentionsPageOne()
        }
        else -> {
            api.fetchMentions(mentions.user.slug, page)
        }
    }.awaitResult {
        it.parseTimelinePosts(
            when (page) {
                0 -> "/mentions"
                else -> "/search/${mentions.user.slug}/0/0/0/$page"
            }, page
        )
    }

    private suspend fun search(
        query: SearchQuery,
        page: Int
    ): Result<PostListDataPage, ErrorResponse> {
        val topicsOnly = if (query.onlyShowTopicPosts) 1 else 0
        val imagesOnly = if (query.onlyShowImages) 1 else 0
        return api.fetchSearchResults(
            query.query,
            query.board?.id ?: 0,
            topicsOnly,
            imagesOnly,
            page
        ).awaitResult {
            it.parseTimelinePosts(
                "/search/${query.query}/$topicsOnly/${query.board?.id ?: 0}/$imagesOnly/$page",
                page
            )
        }
    }

    override suspend fun allViewedTopicIds(): MutableSet<Int> = withContext(context.IO) {
        database.topicQueries.selectAllIds().executeAsList().map { it.toInt() }.toMutableSet()
    }

    override suspend fun allViewedTopics(): List<Topic> = withContext(context.IO) {
        database.topicHistoryQueries.selectRecent { topic_id, title, slug, is_old_url, timestamp ->
            Topic(
                title = title,
                id = topic_id.toInt(),
                slug = slug,
                isOldUrl = is_old_url == 1L,
                timestamp = timestamp?.toLong()
            )
        }.executeAsList()
    }

    override suspend fun updateViewedTopic(topic: Topic) {
        database.topicQueries.updateTitle(
            title = topic.title,
            topic_id = topic.id.toString()
        )
    }

    override suspend fun addViewedTopic(topic: Topic) = withContext(context.IO) {
        database.transaction {
            database.topicQueries.insert(
                topic.id.toString(),
                topic.title,
                topic.slug,
                if (topic.isOldUrl) 1L else 0L,
                topic.timestamp?.toDouble()
            )
            database.topicHistoryQueries.insert(
                topic.id.toString(),
                System.currentTimeMillis().toDouble()
            )
        }
    }

    override suspend fun recentTopics(count: Int): List<Topic> = withContext(context.IO) {
        database.topicHistoryQueries.selectNRecent(count.toLong()) { topic_id, title, slug, is_old_url, timestamp ->
            Topic(
                title = title,
                id = topic_id.toInt(),
                slug = slug,
                isOldUrl = is_old_url == 1L,
                timestamp = timestamp?.toLong()
            )
        }.executeAsList()
    }

    override suspend fun removeVisitedTopic(topic: Topic) = withContext(context.IO) {
        database.transaction {
            database.topicHistoryQueries.deleteThis(topic.id.toString())
            database.topicQueries.deleteThis(topic.id.toString())
        }
    }

    override suspend fun removeAllTopics(): Unit = withContext(context.IO) {
        database.transaction {
            database.topicHistoryQueries.deleteAll()
            database.topicQueries.deleteAll()
        }
    }

    override suspend fun fetchPageWithPost(postId: String): Result<TopicPostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            api.fetchPageWithPost(postId)
                .awaitResultResponse { resp, result ->
                    parseTopicPosts(result, resp.request.url.toString())
                }
        }

    private fun saveTopicPostListData(topic: Topic, data: String, page: Int) {
        database.transaction {

            // update if exists, and return
            when (val dataId = database.postListPagesQueries.findId(
                topic_id = topic.id.toString(),
                page = page,
                user_slug = user?.slug
            ).executeAsOneOrNull()) {
                is Long -> {
                    database.postListPagesQueries.update(
                        data_ = data,
                        timestamp = System.currentTimeMillis(),
                        id = dataId
                    )
                    return@transaction
                }
            }

            // insert topic if not exists
            if (database.topicQueries.selectById(topic.id.toString())
                    .executeAsOneOrNull() == null
            ) {
                database.topicQueries.insert(
                    topic_id = topic.id.toString(),
                    timestamp = topic.timestamp?.toDouble(),
                    title = topic.title,
                    slug = topic.slug,
                    is_old_url = if (topic.isOldUrl) 1L else 0L
                )
            }

            database.postListPagesQueries.insert(
                topic_id = topic.id.toString(),
                timestamp = System.currentTimeMillis(),
                page = page,
                data_ = data,
                user_slug = user?.slug
            )
        }
    }
}