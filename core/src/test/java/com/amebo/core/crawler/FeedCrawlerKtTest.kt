package com.amebo.core.crawler

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FeedCrawlerKtTest: TestCase(){

    @Test
    fun testFeedParsingWorksCorrectly(){
        val data = parseFeedCrawler(readFile("/feed/feed1.xml"))
        assertThat(data).hasSize(12)
    }
}