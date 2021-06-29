package com.amebo.core.crawler.postlist

import com.amebo.core.crawler.TestCase
import com.amebo.core.crawler.postList.parseTimelinePosts
import org.junit.Test

class PostListCrawlerTest: TestCase() {

    @Test
    fun `my shares`() {
        val document = fetchDocument("/sample-html/my-shared-posts.html")
        val page = document.parseTimelinePosts("", 0)
    }
}