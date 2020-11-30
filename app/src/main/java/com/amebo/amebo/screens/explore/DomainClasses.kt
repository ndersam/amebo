package com.amebo.amebo.screens.explore

import com.amebo.core.domain.Board

data class ExploreData(val topicListData: TopicListData, val recentSearch: MutableList<String>)
data class TopicListData(val recent: List<Board>, val followed: List<Board>, val allBoards: List<Board>)