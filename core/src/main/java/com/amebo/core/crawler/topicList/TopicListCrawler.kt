package com.amebo.core.crawler.topicList

import com.amebo.core.CoreUtils
import com.amebo.core.Values
import com.amebo.core.crawler.*
import com.amebo.core.domain.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.regex.Matcher
import java.util.regex.Pattern


data class TopicUrlParseResult(
    val topicId: Int,
    val slug: String,
    val page: Int,
    val refPost: String?,
    val isOldUrl: Boolean = false
)

private class TopicStats(
    val postCount: Int, val viewCount: Int, val author: User,
    val timestamp: Long
)

/*
    Patterns
 */
private val TOPIC_RE = Pattern.compile("/(\\d+)/([^/#]+)(?:/(\\d+))?(?:#(\\d+))?")
private val OLD_TOPIC_RE =
    Pattern.compile("/(topic|board)-(\\d+)\\.\\d+\\.html(#msg\\d+)?$")  // e.g. https://www.nairaland.com/nigeria/topic-4113.0.html
private val TOPIC_FULL_RE = Pattern.compile("${Values.URL}/(\\d+)/([^/#]+)(?:/(\\d+))?(?:#(\\d+))?")
private val OLD_TOPIC_FULL_RE =
    Pattern.compile("${Values.URL}/(topic|board)-(\\d+)\\.\\d+\\.html(#msg\\d+)?$")  // e.g. https://www.nairaland.com/nigeria/topic-4113.0.html
private val TOPIC_NO_TITLE_RE = Pattern.compile("/(\\d+)([^/]*)")
private val TOPIC_NO_TITLE_FULL_RE = Pattern.compile("${Values.URL}/(\\d+)([^/]*)")
private val BY_AUTHOR_RE = Pattern.compile("by\\s+(\\w+)\\.")
private val FEATURED_ITEM_TOPIC = Pattern.compile("(<a.+?)<br>")
private val FOLLOW_URL = Pattern.compile("board=(\\d+)")
private val REGULAR_ITEM_BOARD_VIEWERS =
    Pattern.compile("Viewing this board:\\s+(.*)and\\s+(\\d+)\\s+guest\\(s\\)")
private val NUMBER_REGEX = Pattern.compile("\\d+")
// =======================================================================================


internal fun parseFeaturedTopics(doc: Document, pageNum: Int): TopicListDataPage {
    val topics = mutableListOf<Topic>()

    val table = doc.selectFirst("table[summary=\"links\"]")
    val pageInfo = table.previousElementSibling().previousElementSibling()
    val lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1

    val data = table.selectFirst("tbody tr td")
    val matcher = FEATURED_ITEM_TOPIC.matcher(data.html())
    while (matcher.find()) {
        val row = Jsoup.parse(matcher.group(1))
        val link = row.selectFirst("a")
        val boldTags = row.select("body > b")

        val timestamp = if (boldTags.isNotEmpty()) {
            val time: String = boldTags[0].text()
            var mthDay: String
            var year: String
            when (boldTags.size) {
                3 -> {
                    mthDay = boldTags[1].text()
                    year = boldTags[2].text()
                }
                2 -> {
                    mthDay = boldTags[1].text()
                    year = CoreUtils.currentYear.toString()
                }
                else -> {
                    mthDay = CoreUtils.currentDate
                    year = CoreUtils.currentYear.toString()
                }
            }
            CoreUtils.toTimeStamp(time.trim(), mthDay, year)
        } else -1L
        // very early nairaland pages are empty
        val res = parseTopicUrl(link.attr("href")) ?: continue // FIXME
        val topic = Topic(
            link.text(),
            res.topicId,
            res.slug,
            author = null,
            timestamp = timestamp,
            isOldUrl = res.isOldUrl,
            linkedPage = res.page,
            refPost = res.refPost
        )
        topics.add(topic)
    }
    return TopicListDataPage(topics, pageNum, lastPage)
}

internal fun parseTrendingTopics(doc: Document, pageNum: Int): TopicListDataPage {
    val tables: Elements = doc.select("html body div.body table")
    val topicsElem = tables[1]
    if (topicsElem.classNames().isNotEmpty())
        throw ParseException("Topics Table has at least one class class Names. \"${topicsElem.html()}\"")

    val pageInfo =
        topicsElem.previousElementSibling().previousElementSibling()
    val lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1

    val tableRows = topicsElem.select("tbody > tr > td")
    val topics = parseOtherTopics(tableRows)
    return TopicListDataPage(topics, pageNum, lastPage)
}

internal fun parseNewTopics(doc: Document, pageNum: Int) = parseTrendingTopics(doc, pageNum)

internal fun parseBoardTopics(soup: Document, pageNum: Int, board: Board): BoardsDataPage {
    val tables = soup.select("html body div.body table")
    var relatedElem = tables[1] // related-boards element

    val topicListElem: Element?
    if (relatedElem!!.hasAttr("summary")) {
        topicListElem = tables[2]
    } else {
        if (tables.size == 2) { // i.e. header and footer only
            throw UnauthorizedAccessException()
        }
        topicListElem = relatedElem
        relatedElem = null
    }

    /*
     Related-board information
     */
    val relatedBoards = if (relatedElem != null) {
        val tRows = relatedElem.select("tbody tr td")
        val builder = StringBuilder("<p>")
        for (elem in tRows) {
            builder.append(elem.html()).append("<br><br>")
        }
        builder.append("</p>")
        builder.toString()
    } else ""

    // IS_FOLLOWING?
    val pTagAboveTopicList =
        topicListElem.previousElementSibling().previousElementSibling()
    if (pTagAboveTopicList.tagName() != "p") throw ParseException("Unable to find page number container")

    val aTags = pTagAboveTopicList.select("*")
    var boardNumber: Int? = null
    var following: Boolean? = null
    var followOrUnFollowUrl: String? = null
    var mailModsUrl: String? = null
    for (idx in aTags.size - 1 downTo -1 + 1) {
        val aa = aTags[idx]
        if (aa.text().equals("follow", ignoreCase = true) ||
            aa.text().equals("un-follow", ignoreCase = true)
        ) {
            followOrUnFollowUrl = aa.attr("href")
            following = aa.text().equals("un-follow", ignoreCase = true)
            val matcher = FOLLOW_URL.matcher(aa.attr("href"))
            if (!matcher.find()) throw ParseException("Unable to parse url for follow/un-follow href")
            boardNumber = matcher.group(1)!!.toInt()
        } else if (aa.text().equals("mail mods", ignoreCase = true)) {
            mailModsUrl = aa.attr("href")
        }
    }

    /*
        Board description, Moderators
        .body > p:nth-child(7)
        .body > p:nth-child(7)
        .body > p:nth-child(7) > a:nth-child(1)
         */
    val pTags = soup.select("body > div.body > p")
    var boardInfo = ""
    val mods = arrayListOf<User>()
    val users = arrayListOf<User>()
    var usersViewing = 0
    var guestsViewing = 0
    for (pTag in pTags) {
        if (pTag.classNames().isEmpty() && pTag.text().trim().isNotEmpty()) { // Board description
            if (pTag.children().size == 4 && pTag.child(0).isTag("a") &&
                pTag.child(0).attr("href").contains(board.url)
            ) {
                val boardText = pTag.textNodes()[0].text().trim { it <= ' ' }
                if (boardText[0] == ':') {
                    boardInfo = boardText.substring(1).trim()
                }
            } else if (pTag.text().contains("(Moderators")) {
                for (elem in pTag.select("a")) {
                    mods.add(User(elem.text()))
                }
            }
            // "CreateNewTopic", "Follow", "MailMods"
//                else if (pTag.nextElementSibling().equals(topicListElem)) {
//
//                }
        } else if (pTag.hasClass("nocopy")) {

            // IF LOGGED IN
            val elems = pTag.select("a")
            elems.forEach {
                users.add(User(it.text()))
            }
            usersViewing = users.size
            val textNodes = pTag.textNodes()
            if (textNodes != null && textNodes.size > 0) {
                val m =
                    NUMBER_REGEX.matcher(textNodes[textNodes.size - 1].text())
                if (m.find()) {
                    guestsViewing = Integer.valueOf(m.group())
                }
            }

            // IF NOT LOGGED IN
            if (elems.isEmpty()) {
                val pattern = Pattern.compile(",|and") // TODO .. remove this from here
                val txt = pTag.text().substringAfter("Viewing this board: ")
                val result = pattern.split(txt)
                usersViewing = result.size - 1
                guestsViewing = result.last().trim().split(" ").first().toInt() // e.g. 45 guest(s)
            }

        }
    }

    /*
        Topics
         */
    val tData = topicListElem.select("tbody > tr > td")
    val topics = mutableListOf<Topic>()
    for (row in tData) {
        val boldTags = row.select("> b")
        val topicElem = boldTags.first().selectFirst("a")
        val hasNewPosts = row.selectFirst("img[src=\"/icons/new.gif\"]") != null
        val anchors = row.select("> a")
        var extraElem: Element? = null
        if (anchors.size > 1) extraElem = anchors.last() // href to last page of posts on Topic ..
        val result = parseTopicUrlOrThrow(
            if (extraElem == null)
                topicElem.attr("href")
            else
                extraElem.attr("href")
        )

        // Parse Span...
        val stats = parseTopicStats(row.selectFirst("td > span.s"))
        val topic = Topic(
            topicElem.text(),
            result.topicId,
            result.slug,
            timestamp = stats.timestamp,
            postCount = stats.postCount,
            viewCount = stats.viewCount,
            author = stats.author,
            linkedPage = result.page,
            refPost = result.refPost,
            mainBoard = board,
            hasNewPosts = hasNewPosts,
            isOldUrl = result.isOldUrl
        )
        topics.add(topic)
    }

    val pageInfo =
        topicListElem.previousElementSibling().previousElementSibling()
    val lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1
    return BoardsDataPage(
        data = topics,
        page = pageNum,
        last = lastPage,
        usersViewing = users,
        relatedBoards = relatedBoards,
        numGuestsViewing = guestsViewing,
        numUsersViewing = usersViewing,
        isFollowing = following == true,
        moderators = mods,
        boardInfo = boardInfo,
        followOrUnFollowUrl = followOrUnFollowUrl,
        boardId = boardNumber
    )
}

internal fun parseBoardTopics(soup: Document, url: String): BoardsDataPage {
    val result = parseBoardUrl(url)
    return parseBoardTopics(soup, result.page, result.board)
}

internal fun parseFollowedBoards(soup: Document, url: String): FollowedBoardsDataPage {
    val result = parseFollowedBoardPageUrl(url)
    return parseFollowedBoards(soup, result.page)
}


internal fun parseUserTopics(soup: Document, page: Int): TopicListDataPage {
    var topics = emptyList<Topic>()
    val tables = soup.select("html > body > div > table")
    val tdTopics = tables[1].select("tbody tr td")
    var lastPage = 0
    if (!tdTopics.isEmpty()) { // => there are user topics
        topics = parseOtherTopics(tdTopics)
        val pageInfo =
            tables[1].previousElementSibling().previousElementSibling()
        lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1
    }

    return TopicListDataPage(topics, page, lastPage)
}

internal fun parseFollowedTopics(soup: Document, page: Int): TopicListDataPage {
    var topics = emptyList<Topic>()
    val tables = soup.select("html > body > div > table")
    val tdTopics = tables[1].select("tbody tr td")
    var lastPage = 0
    if (!tdTopics.isEmpty()) { // => there are followed topics
        topics = parseOtherTopics(tdTopics)
        val pageInfo =
            tables[1].previousElementSibling().previousElementSibling()
        lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1
    }
    return TopicListDataPage(topics, page, lastPage)
}


internal fun parseFollowedBoards(soup: Document, pageNumber: Int): FollowedBoardsDataPage {
    val boards = mutableListOf<Pair<Board, String>>()
    var topics = emptyList<Topic>()
    val tables = soup.select("html body div.body table")
    val tBoardsRows = tables[1].select("tbody tr")
    val tdTopics = tables[2].select("tbody tr td")
    val pageInfo = tables[2].previousElementSibling()
    val lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1
    if (tBoardsRows.size == 2) { // => there are followed boards
        val aTags = tBoardsRows[0].select("td a")
        var i = 0
        val len = aTags.size
        while (i < len) {
            val a = aTags[i]
            val boardUrl = a.attr("href").substringAfter("/")
            val followUnFollowUrl = a.nextElementSibling().attr("href")
            val b = Board(a.text(), boardUrl)
            boards.add(b to followUnFollowUrl)
            i += 2
        }
        topics = parseOtherTopics(tdTopics)
    }
    return FollowedBoardsDataPage(topics, pageNumber, lastPage, boards)
}
// ======================================================================================


// NonRegular and NonFeatured
internal fun parseOtherTopics(tableRows: Elements): List<Topic> {
    return tableRows.map {
        val boldTags: Elements = it.select("> b")
        val boardElem: Element = boldTags.first().selectFirst("a")
        val topicElem: Element = boldTags[1].selectFirst("a")

        val result = parseTopicUrlOrThrow(topicElem.attr("href"))
        var href = boardElem.attr("href")
        if (href.startsWith('/')) {
            href = href.substringAfter('/')
        }
        val mainBoard = Board(boardElem.text(), href)
        val stats = parseTopicStats(it.selectFirst("> span.s"))
        val topic =
            Topic(
                topicElem.text(),
                result.topicId,
                result.slug,
                hasNewPosts = hasNewPosts(it),
                mainBoard = mainBoard,
                timestamp = stats.timestamp,
                postCount = stats.postCount,
                viewCount = stats.viewCount,
                linkedPage = result.page,
                refPost = result.refPost,
                author = stats.author,
                isOldUrl = result.isOldUrl
            )
        topic
    }
}

private fun hasNewPosts(row: Element): Boolean {
    return row.selectFirst("img[src=\"/icons/new.gif\"]") != null
}

private fun parseTopicStats(span: Element): TopicStats {
    val boldTags = span.select("> b") // size of: 5 - 7
    val userElem = boldTags.first().selectFirst("a")
    val isUserNull = userElem == null
    val author: User = if (isUserNull) { // this is true for nairaland.com/nobody
        val matcher = BY_AUTHOR_RE.matcher(span.html())
        if (!matcher.find()) {
            throw ParseException("Unable to find topic author")
        }
        User(matcher.group(1)!!)
    } else {
        User(userElem!!.text())
    }

    val offset = if (isUserNull) -1 else 0
    val postCount = boldTags[1 + offset].text().toInt()
    val viewCount = boldTags[2 + offset].text().toInt()

    // Time information (Time of most recent comment)
    // Time information (Time of most recent comment)
    val time = boldTags[3 + offset].text()
    val mthDay: String
    val year: String
    when (boldTags.size) {
        7 + offset -> {
            mthDay = boldTags[4 + offset].text()
            year = boldTags[5 + offset].text()
        }
        6 + offset -> {
            mthDay = boldTags[4 + offset].text()
            year = java.lang.String.valueOf(CoreUtils.currentYear)
        }
        else -> {
            mthDay = CoreUtils.currentDate
            year = java.lang.String.valueOf(CoreUtils.currentYear)
        }
    }
    val timestamp = CoreUtils.toTimeStamp(time.trim(), mthDay, year)
    return TopicStats(postCount, viewCount, author, timestamp)
}

internal fun parseTopicUrlOrThrow(url: String): TopicUrlParseResult {
    return parseTopicUrl(url) ?: throw ParseException("Unable to parse topic url '$url'")
}

/**
 * @param find If false, perform exact match else find pattern in substrings
 */
internal fun parseTopicUrl(url: String, find: Boolean = true): TopicUrlParseResult? {
    val m: Matcher
    val result: Boolean
    if (find) {
        m = TOPIC_RE.matcher(url)
        result = m.find()
    } else {
        m = TOPIC_FULL_RE.matcher(url)
        result = m.matches()
    }
    if (result) {
        val id = m.group(1)!!.toInt()
        val slug = m.group(2)!!
        val page = m.group(3)?.toInt() ?: 0
        val refPost = m.group(4)
        return TopicUrlParseResult(id, slug, page, refPost)
    }

    var res = parseOldTopicUrl(url, find)
    if (res != null) {
        return res
    }

    res = noTitle(url, find)
    if (res != null) {
        return res
    }

    return null
}

fun parseOldTopicUrl(url: String, find: Boolean = true): TopicUrlParseResult? {
    val m: Matcher
    val result: Boolean
    if (find) {
        m = OLD_TOPIC_RE.matcher(url)
        result = m.find()
    } else {
        m = OLD_TOPIC_FULL_RE.matcher(url)
        result = m.matches()
    }
    if (result) {
        val id = m.group(2)!!.toInt()
        val slug = m.group().substringAfter("/")
        val refPost = m.group(3)
        return TopicUrlParseResult(id, slug, 0, refPost, isOldUrl = true)
    }
    return null
}

private fun noTitle(url: String, find: Boolean = true): TopicUrlParseResult? {
    val m: Matcher
    val result: Boolean
    if (find) {
        m = TOPIC_NO_TITLE_RE.matcher(url)
        result = m.find()
    } else {
        m = TOPIC_NO_TITLE_FULL_RE.matcher(url)
        result = m.matches()
    }
    if (result) {
        val id = m.group(1)!!.toInt()
        val slug = m.group(2) ?: ""
        return TopicUrlParseResult(id, slug, 0, null)
    }
    return null
}

