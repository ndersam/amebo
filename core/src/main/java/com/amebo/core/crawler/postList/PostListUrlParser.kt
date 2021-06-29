package com.amebo.core.crawler.postList

import com.amebo.core.common.Values
import java.util.regex.Pattern

//private val RECENT_URL = Pattern.compile("${Values.URL}/recent(?:/(\\d+))?(?:#(\\d+))?")
private val LIKES_AND_SHARES_URL =
    Pattern.compile("${Values.URL}/likesandshares(?:/(\\d+))?(?:#(\\d+))?")
//private val USER_POST_URL = Pattern.compile("${Values.URL}/[^/]+/posts(?:/(\\d+))?(?:#(\\d+))?")
private val SHARED_URL = Pattern.compile("${Values.URL}/shared(?:/(\\d+))?(?:#(\\d+))?")
private val POST_LIST_URL = Pattern.compile("(?:/(\\d+))?(?:#(\\d+))?")

internal class SimplePostListResult(val page: Int, val refPost: String?)

//internal fun parseRecentUrl(url: String) = parse(RECENT_URL, url)

//internal fun parseUsersPostUrl(url: String) = parse(USER_POST_URL, url)

internal fun parseLikesAndSharesUrl(url: String) = parse(LIKES_AND_SHARES_URL, url)

internal fun parseSharedPostUrl(url: String) = parse(SHARED_URL, url)

internal fun parsePostListPattern(url: String): SimplePostListResult? {
    val m = POST_LIST_URL.matcher(url)
    if (m.find()) {
        val page = m.group(1)?.toInt() ?: 0
        val refPost = m.group(2)
        return SimplePostListResult(page, refPost)
    }
    return null
}

private fun parse(pattern: Pattern, url: String): SimplePostListResult? {
    val m = pattern.matcher(url)
    if (m.find()) {
        val page = m.group(1)?.toInt() ?: 0
        val refPost = m.group(2)
        return SimplePostListResult(page, refPost)
    }
    return null
}