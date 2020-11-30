package com.amebo.core.crawler

import android.net.Uri
import com.amebo.core.domain.Board
import com.amebo.core.domain.BoardUrlParseResult
import com.amebo.core.domain.FollowedBoardPageUrlParseResult
import retrofit2.Response

internal fun parseBoardUrl(url: String): BoardUrlParseResult {
    val uri = Uri.parse(url)
    val boardSlug = uri.pathSegments.first()
    val page = uri.lastPathSegment?.toIntOrNull() ?: 0
    return BoardUrlParseResult(Board(boardSlug, boardSlug), page)
}

internal fun parseFollowedBoardPageUrl(url: String): FollowedBoardPageUrlParseResult {
    val uri = Uri.parse(url)
    if (uri.pathSegments.size == 1) return FollowedBoardPageUrlParseResult("creationtime", 0)
    val page = uri.lastPathSegment?.toIntOrNull() ?: 0
    return FollowedBoardPageUrlParseResult(uri.pathSegments[1], page)
}

internal fun isHomeUrl(response: Response<*>): Boolean {
    val uri = Uri.parse(response.raw().request.url.toString())
    uri.getQueryParameter("board")
    return uri.pathSegments.size == 0
}

