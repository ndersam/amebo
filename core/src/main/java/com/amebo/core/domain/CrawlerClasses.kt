package com.amebo.core.domain

class LikeShareUrlParseResult(val postId: String, val redirect: String, val session: String)
class FollowTopicUrlParseResult(val topic: String, val redirect: String, val session: String)
class FollowBoardUrlParseResult(val board: Int, val redirect: String, val session: String)
internal class FollowedBoardPageUrlParseResult(val sort: String, val page: Int)
internal class BoardUrlParseResult(val board: Board, val page: Int)
