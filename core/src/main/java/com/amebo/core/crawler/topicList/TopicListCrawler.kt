package com.amebo.core.crawler.topicList

import com.amebo.core.common.CoreUtils
import com.amebo.core.common.Values
import com.amebo.core.crawler.*
import com.amebo.core.domain.*
import com.github.michaelbull.result.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URI
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
private val FEATURED_ITEM_TOPIC = Pattern.compile("(<a.+?)\\s+<hr>")
private val FOLLOW_URL = Pattern.compile("board=(\\d+)")
private val REGULAR_ITEM_BOARD_VIEWERS =
    Pattern.compile("Viewing this board:\\s+(.*)and\\s+(\\d+)\\s+guest\\(s\\)")
private val NUMBER_REGEX = Pattern.compile("\\d+")
// =======================================================================================


internal fun Document.parseFeaturedTopics(pageNum: Int): Result<TopicListDataPage, ErrorResponse> {
    return runCatching {
        val topics = mutableListOf<Topic>()

        val table = selectFirst("table[summary=\"links\"]")
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
        TopicListDataPage(topics, pageNum, lastPage)
    }.mapError { errorResponse("/news/$pageNum", it) }
}

internal fun Document.parseTrendingTopics(
    pageNum: Int
): Result<TopicListDataPage, ErrorResponse> {
    return runCatching {
        val tables: Elements = select("html body div.body table")
        val topicsElem = tables[1]
        if (topicsElem.classNames().isNotEmpty())
            return Err(ErrorResponse.Unknown(exception = ParseException("Topics Table has at least one class class Names. \"${topicsElem.html()}\"")))

        val pageInfo =
            topicsElem.previousElementSibling().previousElementSibling()
        val lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1

        val tableRows = topicsElem.select("tbody > tr > td")
        val topics = when (val result = parseOtherTopics(tableRows)) {
            is Ok -> result.value
            is Err -> return result
        }
        TopicListDataPage(topics, pageNum, lastPage)
    }.mapError { errorResponse("/trending/$pageNum", it) }
}

internal fun Document.parseNewTopics(pageNum: Int) = parseTrendingTopics(pageNum)

private data class PageNavigationHeader(
    val boardNumber: Int?,
    val following: Boolean?,
    val mailModsUrl: String?,
    val followOrUnfollowUrl: String?,
    val lastPage: Int,
)

private data class BoardInformation(
    val boardInfo: String,
    val mods: List<User>,
    val users: List<User>,
    val usersViewing: Int,
    val guestsViewing: Int
)

internal fun Document.parseBoardTopics(
    pageNum: Int,
    board: Board
): Result<BoardsDataPage, ErrorResponse> = validatePage()
    .flatMap {
        runCatching {
            val allTables = select("html body div.body table")
            val tables = allTables.subList(1, allTables.size - 1)

            if (tables.isEmpty()) {
                return@flatMap Err(ErrorResponse.UnAuthorized)
            }

            val relatedBoards = if (tables.first().hasAttr("summary")) {
                tables.first().parseRelatedBoardsElem()
            } else {
                ""
            }

            val topicListElem = if (tables.first().hasAttr("summary")) {
                tables.getOrNull(1)
                    ?: return@flatMap Err(ErrorResponse.Unknown(exception = ParseException("Unable")))
            } else {
                tables.first()
            }

            val pTagAboveTopicList = topicListElem.previousElementSibling().previousElementSibling()
            if (pTagAboveTopicList.tagName() != "p") {
                return@flatMap Err(ErrorResponse.Unknown(exception = ParseException("Unable to find page number container")))
            }
            val pageHeader = when (val result = pTagAboveTopicList.parsePageNavigationHeader()) {
                is Ok -> result.value
                is Err -> return@flatMap result.mapError { ErrorResponse.Unknown(exception = it) }
            }

            val info = it.parseBoardInformation(board.url)
            val topics = when (val result = topicListElem.parseBoardTopics(board)) {
                is Ok -> result.value
                is Err -> return@flatMap result.mapError { ErrorResponse.Unknown(exception = it) }
            }

            BoardsDataPage(
                data = topics,
                page = pageNum,
                last = pageHeader.lastPage,
                usersViewing = info.users,
                relatedBoards = relatedBoards,
                numGuestsViewing = info.guestsViewing,
                numUsersViewing = info.usersViewing,
                isFollowing = pageHeader.following == true,
                moderators = info.mods,
                boardInfo = info.boardInfo,
                followOrUnFollowUrl = pageHeader.followOrUnfollowUrl,
                boardId = pageHeader.boardNumber
            )
        }.mapError { errorResponse("/${board.url}/$pageNum", it) }
    }


internal fun Document.parseBoardTopics(url: String): Result<BoardsDataPage, ErrorResponse> {
    val result = parseBoardUrl(url)
    return parseBoardTopics(result.page, result.board)
}

internal fun Document.parseFollowedBoards(url: String): Result<FollowedBoardsDataPage, ErrorResponse> {
    val result = parseFollowedBoardPageUrl(url)
    return parseFollowedBoards(result.page)
}


internal fun Document.parseUserTopics(page: Int): Result<TopicListDataPage, ErrorResponse> {
    return runCatching {
        var topics = emptyList<Topic>()
        val tables = select("html > body > div > table")
        val tdTopics = tables[1].select("tbody tr td")
        var lastPage = 0
        if (!tdTopics.isEmpty()) { // => there are user topics
            topics = when (val result = parseOtherTopics(tdTopics)) {
                is Ok -> result.value
                is Err -> return result
            }
            val pageInfo =
                tables[1].previousElementSibling().previousElementSibling()
            lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1
        }

        TopicListDataPage(topics, page, lastPage)
    }.mapError { errorResponse("/{user}/$page", it) }
}

internal fun Document.parseFollowedTopics(page: Int? = null): Result<TopicListDataPage, ErrorResponse> {
    return runCatching {
        var topics = emptyList<Topic>()
        val tables = select("html > body > div > table")
        val tdTopics = tables[1].select("tbody tr td")
        var lastPage = 0
        val currentPage: Int
        if (!tdTopics.isEmpty()) { // => there are followed topics
            topics = when (val result = parseOtherTopics(tdTopics)) {
                is Ok -> result.value
                is Err -> return result
            }
            val pageInfo =
                tables[1].previousElementSibling().previousElementSibling()
            lastPage = pageInfo.selectFirst("b:last-of-type").text().toInt() - 1
            val currentPageInfo = pageInfo.selectFirst("b:first-of-type").text()
            currentPage = currentPageInfo.substring(1, currentPageInfo.length - 1)
                .toInt() - 1 // remove parentheses
        } else {
            currentPage = 0
        }
        TopicListDataPage(topics, currentPage, lastPage)
    }.mapError { errorResponse("/followed/$page", it) }
}


internal fun Document.parseFollowedBoards(
    pageNumber: Int
): Result<FollowedBoardsDataPage, ErrorResponse> {
    return runCatching {
        val boards = mutableListOf<Pair<Board, String>>()
        var topics = emptyList<Topic>()
        val tables = select("html body div.body table")
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
            topics = when (val result = parseOtherTopics(tdTopics)) {
                is Ok -> result.value
                is Err -> return result
            }
        }
        FollowedBoardsDataPage(topics, pageNumber, lastPage, boards)
    }.mapError { ErrorResponse.Unknown(exception = Exception(it)) }
}
// ======================================================================================


// NonRegular and NonFeatured
internal fun parseOtherTopics(tableRows: Elements): Result<List<Topic>, ErrorResponse> {
    val topics = tableRows.map {
        val boldTags: Elements = it.select("> b")
        val boardElem: Element = boldTags.first().selectFirst("a")
        val topicElem: Element = boldTags[1].selectFirst("a")

        var href = boardElem.attr("href")
        if (href.startsWith('/')) {
            href = href.substringAfter('/')
        }
        val mainBoard = Board(boardElem.text(), href)
        val stats = parseTopicStats(it.selectFirst("> span.s"))
        // For FollowedTopicsList
        // TODO: The redirect param in url is wrong...
        // The page number is not present
        val unFollowLink = it.selectFirst("img[src=\"/static/delete.png\"]")?.parent()?.attr("href")
        val result = parseTopicUrlOrThrow(topicElem.attr("href"))
            .map { result ->
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
                    isOldUrl = result.isOldUrl,
                    followOrUnFollowLink = unFollowLink
                )
            }
        when (result) {
            is Ok -> result.value
            is Err -> return Err(result.error)
        }
    }
    return Ok(topics)
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

internal fun parseTopicUrlOrThrow(url: String): Result<TopicUrlParseResult, ErrorResponse> {
    return when (val result = parseTopicUrl(url)) {
        is TopicUrlParseResult -> Ok(result)
        else -> Err(ErrorResponse.Unknown(exception = ParseException("Unable to parse topic url '$url'")))
    }
}

/**
 * @param find If false, perform exact match else find pattern in substrings
 */
internal fun parseTopicUrl(url: String, find: Boolean = true): TopicUrlParseResult? {
    if (!url.startsWith('/')) {
        val uri = URI(url)
        val domain = if (uri.host.startsWith("www.")) uri.host.substring(4) else uri.host
        if (!domain.equals("nairaland.com", true)) {
            return null
        }
    }
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


/**
 * Related-board information
 */
private fun Element.parseRelatedBoardsElem(): String {
    val tRows = this.select("tbody tr td")
    val builder = StringBuilder("<p>")
    for (elem in tRows) {
        builder.append(elem.html()).append("<br><br>")
    }
    builder.append("</p>")
    return builder.toString()
}

/**
 * Page navigation header from outermost <p> tag
 */
private fun Element.parsePageNavigationHeader(): Result<PageNavigationHeader, ParseException> {
    var boardNumber: Int? = null
    var following: Boolean? = null
    var followOrUnFollowUrl: String? = null
    var mailModsUrl: String? = null
    val aTags = select("*")
    var exception: ParseException? = null
    val lastPage = selectFirst("b:last-of-type").text().toInt() - 1

    aTags.reversed()
        .forEach {
            if (it.text().equals("follow", ignoreCase = true) ||
                it.text().equals("un-follow", ignoreCase = true)
            ) {
                followOrUnFollowUrl = it.attr("href")
                following = it.text().equals("un-follow", ignoreCase = true)
                val matcher = FOLLOW_URL.matcher(it.attr("href"))
                if (!matcher.find()) {
                    exception = ParseException("Unable to parse url for follow/un-follow href")
                    return@forEach
                }
                boardNumber = matcher.group(1)!!.toInt()
            } else if (it.text().equals("mail mods", ignoreCase = true)) {
                mailModsUrl = it.attr("href")
            }
        }

    return if (exception == null) {
        Ok(
            PageNavigationHeader(
                boardNumber,
                following,
                followOrUnFollowUrl,
                mailModsUrl,
                lastPage
            )
        )
    } else {
        Err(exception!!)
    }
}


/**
 * Obtain board info text, mods, users viewing and guest viewing counts
 */
private fun Document.parseBoardInformation(boardUrl: String): BoardInformation {
    var boardInfo = ""
    val mods = arrayListOf<User>()
    val users = arrayListOf<User>()
    var usersViewing = 0
    var guestsViewing = 0

    val pTags = select("body > div.body > p")
    for (pTag in pTags) {
        if (pTag.classNames().isEmpty() && pTag.text().trim()
                .isNotEmpty()
        ) { // Board description
            if (pTag.children().size == 4 && pTag.child(0).isTag("a") &&
                pTag.child(0).attr("href").contains(boardUrl)
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
                guestsViewing =
                    result.last().trim().split(" ").first().toInt() // e.g. 45 guest(s)
            }
        }
    }
    return BoardInformation(boardInfo, mods, users, usersViewing, guestsViewing)
}

/**
 * Get topics from table element
 */
private fun Element.parseBoardTopics(board: Board): Result<List<Topic>, ParseException> {
    val tData = select("tbody > tr > td")
    val topics = mutableListOf<Topic>()
    for (row in tData) {
        val boldTags = row.select("> b")
        val topicElem = boldTags.first().selectFirst("a")
        val hasNewPosts = row.selectFirst("img[src=\"/icons/new.gif\"]") != null
        val anchors = row.select("> a")
        var extraElem: Element? = null
        if (anchors.size > 1) extraElem =
            anchors.last() // href to last page of posts on Topic ..
        val result = parseTopicUrl(
            if (extraElem == null)
                topicElem.attr("href")
            else
                extraElem.attr("href")
        ) ?: return Err(ParseException("Unable to parse topic url"))

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
    return Ok(topics)
}