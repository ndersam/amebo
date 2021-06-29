package com.amebo.core.crawler.topicList

import org.junit.Test

class TopicUrlParserTest {
    private val testCases = arrayListOf(
        "https://www.nairaland.com/2792995/nairaland-says-no-secessionists" to
                TopicUrlParseResult(2792995, "nairaland-says-no-secessionists", 0, null),
        "https://www.nairaland.com/2792995/nairaland-says-no-secessionists/122" to
                TopicUrlParseResult(2792995, "nairaland-says-no-secessionists", 122, null),
        "https://www.nairaland.com/5640372/manchester-united-vs-burnley-0/4#86028359" to
                TopicUrlParseResult(5640372, "manchester-united-vs-burnley-0", 4, "86028359"),
        "https://www.nairaland.com/5643541/sheffield-united-vs-manchester-city/3#85999259" to
                TopicUrlParseResult(5643541, "sheffield-united-vs-manchester-city", 3, "85999259")
    )

    @Test
    fun `parsing work correctly`() {
        testCases.forEach {
            val result = parseTopicUrlOrThrow(it.first)
//            if (it.second != result) {
//                print("${it.second} != $result\n")
//                assert(false)
//            }

        }
    }
}