package com.amebo.core.crawler.topicList

import com.amebo.core.crawler.TestCase
import com.amebo.core.domain.Board
import org.junit.Test

class TopicListCrawlerTest : TestCase() {

    @Test
    fun `board-works-just-fine`() {
        val document = fetchDocument("/topiclist/board-politics.html")
        val board = Board("Politics", "politics")
        val page = document.parseBoardTopics(0, board)
    }
}