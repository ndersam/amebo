package com.amebo.core.crawler

import com.amebo.core.Values
import com.amebo.core.crawler.topicList.parseTopicUrl
import com.amebo.core.domain.Topic
import com.amebo.core.domain.TopicFeed
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.util.*

internal fun parseFeedCrawler(text: String): List<TopicFeed> {
    val doc = Jsoup.parse(text, Values.URL + "/feed", Parser.xmlParser())
    val feed = doc.selectFirst("feed")
    return feed.select("entry").map(::parseEntry)
}

private fun parseEntry(element: Element): TopicFeed {
    val title = element.selectFirst("title").text()
    val summary = element.selectFirst("summary").text()
    val urlRaw = element.select("id").text()
    val url = urlRaw.substringAfter("http://www.nairaland.com").apply {
        this.substringAfter("https://www.nairaland.com")
    }
    val result = parseTopicUrl(url, find = true)!!

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val timeUpdated = sdf.parse(element.selectFirst("updated").text())!!
    val timePublished = sdf.parse(element.selectFirst("published").text())!!
    return TopicFeed(
        topic = Topic(
            id = result.topicId,
            slug = result.slug,
            linkedPage = result.page,
            refPost = result.refPost,
            isOldUrl = result.isOldUrl,
            title = title,
            timestamp = timePublished.time
        ),
        timestampUpdated = timeUpdated.time,
        summary = summary
    )
}