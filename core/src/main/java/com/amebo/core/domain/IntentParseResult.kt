package com.amebo.core.domain

/**
 * Models the parsed result of a 'known' nairaland url
 */
sealed class IntentParseResult {
    class TopicListResult(val topicList: TopicList, val page: Int) : IntentParseResult()
    class PostListResult(val postList: PostList, val page: Int) : IntentParseResult()
    class UserResult(val user: User) : IntentParseResult()
}