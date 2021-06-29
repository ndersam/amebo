package com.amebo.core.crawler.postList

import android.os.Build
import com.amebo.core.common.CoreUtils
import com.amebo.core.common.Values
import com.amebo.core.crawler.DateTimeParser
import com.amebo.core.crawler.ParseException
import com.amebo.core.crawler.errorResponse
import com.amebo.core.crawler.isTag
import com.amebo.core.crawler.topicList.parseTopicUrl
import com.amebo.core.crawler.topicList.parseTopicUrlOrThrow
import com.amebo.core.domain.*
import com.github.michaelbull.result.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import timber.log.Timber
import java.util.regex.Pattern
import kotlin.properties.Delegates

private val MULTI_LINE_REGEX = Pattern.compile("(<br>\n*){3,}")
private val NUMBER_REGEX = Pattern.compile("\\d+")
private val NUMBER_IN_PAREN_REGEX = Regex("\\(\\d+\\)")
private val VIEW_COUNT_REGEX = Pattern.compile("(\\d+)\\s+Views", Pattern.CASE_INSENSITIVE)
private const val FORUMS_SELECTOR = "html body div.body p.bold"
private const val LIKES_SELECTOR = "lpt"
private const val SHARES_SELECTOR = "shb"
private fun likesHTMLId(postId: String) = LIKES_SELECTOR + postId
private fun sharesHTMLId(postId: String) = SHARES_SELECTOR + postId

private const val DEFAULT_HIGHLIGHT_COLOR = "#f9a825"


internal fun Document.parseUnknownPostList(
    url: String
): Result<PostListDataPage, ErrorResponse> {
    val topic = parseTopicUrl(url, find = false)
    if (topic != null) {
        return parseTopicPosts(this, url).onSuccess {
            it.apply { postToScrollTo = topic.refPost }
        }
    }

    val likesAndShares = parseLikesAndSharesUrl(url)
    if (likesAndShares != null) {
        return parseLikesAndShares(likesAndShares.page).onSuccess {
            it.apply {
                postToScrollTo = likesAndShares.refPost
            }
        }
    }

    val shared = parseSharedPostUrl(url)
    if (shared != null) {
        return parseSharedPosts(shared.page).onSuccess {
            it.apply { postToScrollTo = shared.refPost }
        }
    }

    // assumed others must be a simple timeline post
    // e.g. search result, mentions or recent or user posts ... etc
    val postListResult = parsePostListPattern(url)
    if (postListResult != null) {
        return parseTimelinePosts(url, postListResult.page).onSuccess {
            it.apply {
                postToScrollTo = postListResult.refPost
            }
        }
    }
    return Err(errorResponse(url, Exception("Unknown post list with url=$url")))
}

internal fun parseTopicPosts(
    soup: Document,
    topicUrl: String
): Result<TopicPostListDataPage, ErrorResponse> {
    return parseTopicUrlOrThrow(topicUrl)
        .map { result ->
            result to Topic(
                title = result.slug,
                id = result.topicId,
                isOldUrl = result.isOldUrl,
                linkedPage = result.page,
                slug = result.slug,
                refPost = result.refPost
            )
        }
        .flatMap {
            soup.parseTopicPosts(it.second, it.first.page)
        }
}

internal fun Document.parseTopicPosts(
    originalTopic: Topic,
    pageNo: Int
): Result<TopicPostListDataPage, ErrorResponse> {
    return runCatching {
        var topic = run {
            val titleElem: Element = this.selectFirst("html body div.body h2")
            var title: String = titleElem.text()
            var dash = title.lastIndexOf("-") - 1
            if (dash > -1) {
                dash = title.lastIndexOf("-", startIndex = dash)
                title = title.substring(0, dash).trim()
            }
            originalTopic.copy(title = title)
        }

        // FETCH posts
        val table = this.selectFirst("table[summary=\"posts\"]")
            ?:
            // Hidden topic e.g. https://www.nairaland.com/4928836/madagascar-pochard-worlds-rarest-bird
            // ... when viewed without logging in
            return@runCatching TopicPostListDataPage(
                topic = topic,
                views = 0,
                isHiddenFromUser = true,
                last = 0,
                page = 0,
                data = emptyList(),
                usersViewing = emptyList(),
                isFollowingTopic = false,
                followOrUnFollowTopicUrl = null,
                isClosed = false
            )

        val postList = fetchPostsOnTopic(table, topic)
        var viewCount: Int? = null
        val boards = mutableListOf<Board>()
        var isFollowingTopic = false
        var followOrUnFollowTopicUrl: String? = null

        // Find forums that topic is posted in
        val nodes: List<Node> = this.selectFirst(FORUMS_SELECTOR).childNodes()
        val anchors: MutableList<Element> = ArrayList()
        for (node in nodes) {
            if (node is TextNode) { // VIEW_COUNT
                val matcher = VIEW_COUNT_REGEX.matcher(node.wholeText)
                if (matcher.find()) {
                    viewCount = matcher.group(1)!!.toInt()
                }
            } else if (node is Element) {
                anchors.add(node)
            }
        }
        /*
        Related topics
         */
        val relatedTopicsElem = this.selectFirst("body > div > p.bold").nextElementSibling()
        val relatedTopics = mutableListOf<Topic>()
        var index = 0
        while (index < relatedTopicsElem.childNodeSize() && relatedTopicsElem.childNode(index) !is Element) {
            index++
        }
        while (index < relatedTopicsElem.childNodeSize() && relatedTopicsElem.isTag("p")) {
            var node = relatedTopicsElem.childNode(index)
            if (node is Element) {
                // e.g. (2), (33)
                val isPageNum = node.text().matches(NUMBER_IN_PAREN_REGEX)
                if (node.tagName() == "a" && !isPageNum) {
                    val result = parseTopicUrl(node.attr("href"))
                    if (result != null) {
                        relatedTopics.add(
                            Topic(
                                title = node.text(),
                                slug = result.slug,
                                id = result.topicId,
                                linkedPage = result.page,
                                isOldUrl = result.isOldUrl,
                                refPost = result.refPost
                            )
                        )
                        // note..
                        index++
                    }
                }
            }

            node = relatedTopicsElem.childNode(index)
            if (!(node is TextNode && node.text().trim() == "/")) {
                break
            }
            index++
        }
        // Related topics beneath list of posts
        this.select("div.nocopy > p:nth-of-type(2) > a").forEach {
            val result = parseTopicUrl(it.attr("href"))
            if (result != null) {
                relatedTopics.add(
                    Topic(
                        title = it.text(),
                        slug = result.slug,
                        id = result.topicId,
                        linkedPage = result.page,
                        isOldUrl = result.isOldUrl,
                        refPost = result.refPost
                    )
                )
            }
        }


        /*
          Boards
         */
        val start = if (anchors.size > 2) 1 else 0
        val end = anchors.size - 1
        for (i in end - 1 downTo start) {
            val elem: Element = anchors[i]
            // workaround for old topics like
            // https://www.nairaland.com/1049481/how-place-targeted-ads-nairaland
            // ... that have page number anchors as sibling elements to board names
            val url = elem.attr("href").substringAfter("/")
            if (elem.text().trim().matches(NUMBER_IN_PAREN_REGEX).not() && url.contains('/')
                    .not()
            ) {
                boards.add(Board(elem.text(), url))
            }
        }
        if (boards.isNotEmpty()) {
            topic.mainBoard = boards[0]
        }

        /*
          Page information
        */
        var lastPage = 0
        val pageInfo =
            table.previousElementSibling().previousElementSibling()  // True if an add is present
                ?: table.parent().previousElementSibling()
        val children = pageInfo.children()
        for (idx in children.indices.reversed()) {
            val elem: Element = children[idx]
            // page number tags
            // e.g. <a>(2)</a> .... <b>(1)</b>
            if (elem.isTag("b") || elem.isTag("a") &&
                elem.attr("href").startsWith("/" + topic.id)
            ) {
                lastPage = elem.text().substring(1, elem.text().length - 1)
                    .toInt() - 1 // -1 bcos pages are zero-index
                break
            } else if (elem.isTag("a") && elem.attr("href").contains("followtopic?")) {
                isFollowingTopic = !elem.text().trim().equals("follow", true)
                followOrUnFollowTopicUrl = elem.attr("href")
            }
        }
        // IsPageClosed?
        val closedElemParent = table.nextElementSibling() ?: table.parent().nextElementSibling()
        val closedElem = closedElemParent.selectFirst("p img[src=\"/icons/closed.gif\"]")

        if (pageNo == 0 && postList.isNotEmpty()) {
            val firstPost = (postList.first() as SimplePost)
            topic = topic.copy(timestamp = firstPost.timestamp, author = firstPost.author)
        }

        TopicPostListDataPage(
            topic = topic,
            views = viewCount!!,
            isHiddenFromUser = false,
            last = lastPage,
            page = pageNo,
            data = postList,
            isFollowingTopic = isFollowingTopic,
            followOrUnFollowTopicUrl = followOrUnFollowTopicUrl,
            usersViewing = emptyList(),
            isClosed = closedElem != null,
            relatedTopics = relatedTopics
        )
    }.mapError { errorResponse("/${originalTopic.id}/${originalTopic.slug}/${pageNo}", it) }
}

internal fun Document.parseTimelinePosts(
    url: String,
    pageNumber: Int
): Result<TimelinePostsListDataPage, ErrorResponse> {
    return runCatching {
        val posts = mutableListOf<Post>()

        val table = selectFirst("body > div > table:nth-of-type(2)")
        val tRows = table.select("tr td")
            ?: // No posts
            return@runCatching TimelinePostsListDataPage(emptyList(), pageNumber, pageNumber)

        /*
            Page information
             */
        var pageElem = table.previousElementSibling().previousElementSibling()
        if (!pageElem.isTag("p"))
            pageElem = pageElem.nextElementSibling()
        val lastPageElem = pageElem.selectFirst("b:last-of-type")
            ?: return@runCatching TimelinePostsListDataPage(
                posts,
                pageNumber,
                0
            )
        val lastPage = (lastPageElem.text().toInt() - 1).coerceAtLeast(0)


        var idx = 0
        while (idx < tRows.size) {
            val td = tRows[idx++]
            when {
                isStartOfNewPost(td) -> {
                    val header = when (val headerResult = parseTimelineItemHeader(td)) {
                        is Ok -> headerResult.value
                        is Err -> return Err(headerResult.error)
                    }
                    val postBody = parsePostBody(
                        tRows[idx++].selectFirst("td")
                    )
                    val post = SimplePost(
                        author = header.author,
                        topic = header.topic,
                        isLiked = postBody.isLiked,
                        isShared = postBody.isShared,
                        likeUrl = postBody.likeUrl,
                        shareUrl = postBody.shareUrl,
                        timestamp = header.timestamp,
                        images = postBody.images,
                        text = postBody.text,
                        id = postBody.id,
                        likes = postBody.likes,
                        shares = postBody.shares,
                        parentQuotes = postBody.parentQuotes,
                        editUrl = postBody.modifyPostURL,
                        reportUrl = postBody.reportPostURL,
                        url = header.linkedUrl
                    )
                    val timelinePost = TimelinePost(header.isMainPost, post)
                    posts.add(timelinePost)
                }
                isBlankPost(td) -> {
                    posts.add(DeletedPost(td.selectFirst("a").attr("name")))
                }
                else -> {
                    Timber.e("[ERROR] This should never happen")
                }
            }
        }
        TimelinePostsListDataPage(
            posts,
            pageNumber,
            lastPage
        )
    }
        .mapError { errorResponse(url, it) }
}

internal fun Document.parseSharedPosts(
    pageNumber: Int
): Result<SharedPostsListDataPage, ErrorResponse> {
    return runCatching {
        val posts = mutableListOf<Post>()
        val table = selectFirst("body > div > table:nth-of-type(2)")
        val tRows = table.select("tr td")
            ?: // No posts
            return@runCatching SharedPostsListDataPage(emptyList(), pageNumber, pageNumber)

        /*
            Page information
             */
        var pageElem = table.previousElementSibling().previousElementSibling()
        if (!pageElem.isTag("p"))
            pageElem = pageElem.nextElementSibling()
        val lastPage = (pageElem.selectFirst("b:last-of-type").text().toInt() - 1).coerceAtLeast(0)


        var idx = 0
        while (idx < tRows.size) {
            val td = tRows[idx++]
            when {
                isStartOfNewPost(td) -> {
                    val header = when (val result = parseSharedPostHeader(td)) {
                        is Ok -> result.value
                        is Err -> return result
                    }
                    val postBody = parsePostBody(
                        tRows[idx++].selectFirst("td")
                    )
                    val post = SimplePost(
                        author = header.author,
                        topic = header.topic,
                        isLiked = postBody.isLiked,
                        isShared = postBody.isShared,
                        likeUrl = postBody.likeUrl,
                        shareUrl = postBody.shareUrl,
                        timestamp = header.timestamp,
                        images = postBody.images,
                        text = postBody.text,
                        id = postBody.id,
                        likes = postBody.likes,
                        shares = postBody.shares,
                        parentQuotes = postBody.parentQuotes,
                        editUrl = postBody.modifyPostURL,
                        reportUrl = postBody.reportPostURL,
                        url = header.linkedUrl
                    )
                    val sharedPost = SharedPost(
                        header.sharer,
                        header.sharerInfo,
                        header.timestampShared,
                        header.isMainPost,
                        post
                    )
                    posts.add(sharedPost)
                }
                isBlankPost(td) -> {
                    posts.add(DeletedPost(td.selectFirst("a").attr("name")))
                }
                else -> {
                    Timber.e("[ERROR] This should never happen")
                }
            }
        }
        SharedPostsListDataPage(
            posts,
            pageNumber,
            lastPage
        )
    }
        .mapError { errorResponse("/shared/$pageNumber", it) }
}

internal fun Document.parseLikesAndShares(
    pageNumber: Int
): Result<LikedOrSharedPostListDataPage, ErrorResponse> {
    return runCatching {
        val posts = mutableListOf<Post>()
        var numShares = 0
        var numLikes = 0

        // Num likes? shares?
        this.select("body > div > table:nth-of-type(2) b").forEach {
            val text = it.text().trim()
            // e.g. 283 total shares
            if (text.endsWith("total shares", ignoreCase = true)) {
                numShares = text.split(" ").first().trim().toInt()
            }
            // e.g. 283 total likes
            else if (text.endsWith("total likes", ignoreCase = true)) {
                numLikes = text.split(" ").first().trim().toInt()
            }
        }

        val table = this.selectFirst("body > div > table:nth-of-type(3)")
        // 'table of posts' has no id, so if this has an Id, it's definitely not the right table
        if (table.id().isNotBlank()) {
            return@runCatching LikedOrSharedPostListDataPage(
                emptyList(),
                pageNumber,
                pageNumber,
                numLikes,
                numShares
            )
        }
        val tRows = table.select("tr td")
            ?: // No posts
            return@runCatching LikedOrSharedPostListDataPage(
                emptyList(),
                pageNumber,
                pageNumber,
                numLikes,
                numShares
            )

        /*
            Page information
             */
        var pageElem = table.previousElementSibling().previousElementSibling()
        if (!pageElem.isTag("p"))
            pageElem = pageElem.nextElementSibling()
        val lastPage = pageElem.selectFirst("b:last-of-type").text().toInt() - 1


        var idx = 0
        while (idx < tRows.size) {
            val td = tRows[idx++]
            when {
                isStartOfNewPost(td) -> {
                    val header = when (val result = parseLikesAndSharesHeader(td)) {
                        is Ok -> result.value
                        is Err -> return result
                    }
                    val postBody = parsePostBody(
                        tRows[idx++].selectFirst("td")
                    )
                    val post = SimplePost(
                        author = header.author,
                        topic = header.topic,
                        isLiked = postBody.isLiked,
                        isShared = postBody.isShared,
                        likeUrl = postBody.likeUrl,
                        shareUrl = postBody.shareUrl,
                        timestamp = header.timestamp,
                        images = postBody.images,
                        text = postBody.text,
                        id = postBody.id,
                        likes = postBody.likes,
                        shares = postBody.shares,
                        parentQuotes = postBody.parentQuotes,
                        editUrl = postBody.modifyPostURL,
                        reportUrl = postBody.reportPostURL,
                        url = header.linkedUrl
                    )
                    val sharedPost = LikedOrSharedPost(
                        post = post,
                        timestamp = header.timestampLikedOrShared,
                        isMainPost = header.isMainPost,
                        kind = if (header.info.equals("shared", true)) {
                            val sharerName = header.nameOfSharer!!
                            val sharer =
                                if (sharerName.equals("you", true)) null else User(sharerName)
                            LikedOrSharedPost.Kind.Shared(isYou = sharer == null, sharer = sharer)
                        } else {
                            LikedOrSharedPost.Kind.Liked
                        }
                    )
                    posts.add(sharedPost)
                }
                isBlankPost(td) -> {
                    posts.add(DeletedPost(td.selectFirst("a").attr("name")))
                }
                else -> {
                    Timber.e("[ERROR] This should never happen")
                }
            }
        }


        LikedOrSharedPostListDataPage(
            posts,
            pageNumber,
            lastPage,
            numLikes,
            numShares
        )
    }
        .mapError { errorResponse("/likesandshares/$pageNumber", it) }
}

private fun parseTimelineItemHeader(td: Element): Result<TimelineHeader, ErrorResponse> {
    var postAuthor: User? = null

    /*
        TOPIC, AUTHOR
     */
    val authorElem = td.selectFirst("a.user")
    val boardElem = td.selectFirst("a:nth-of-type(4)")
    val topicElem = td.selectFirst("a:nth-of-type(5)")
    val postLink = topicElem.attr("href") // postLink is of form ".../<TOPIC_ID>/<TOPIC>#<POST_ID>"


    if (authorElem != null) {
        postAuthor = User(authorElem.text())
    } else {
        // In some special cases, for special users e.g. https://www.nairaland.com/nobody
        for (node in td.childNodes()) {
            if (node is TextNode && node.text().trim { it <= ' ' }.startsWith("by")) {
                // assumes text is in the form: "by XXXXXX:" ... hence start at index 3 and ignore the last char
                val text = node.text().trim { it <= ' ' }
                val author = text.substring(3, text.length - 1)
                postAuthor = User(author)
                break
            }
        }
    }

    val isSimpleComment = topicElem.text().startsWith("Re:")
    return parseTopicUrlOrThrow(topicElem.attr("href"))
        .map { result ->
            TimelineHeader().apply {
                author = postAuthor ?: throw ParseException("Unable to fix author")
                val boardUrl = if (boardElem.attr("href").startsWith("/"))
                    boardElem.attr("href").substringAfter("/")
                else boardElem.attr("href")
                topic = Topic(
                    title = if (isSimpleComment) topicElem.text().substring(3)
                        .trim() else topicElem.text(),
                    id = result.topicId,
                    slug = result.slug,
                    linkedPage = result.page,
                    refPost = result.refPost,
                    mainBoard = Board(boardElem.text(), boardUrl),
                    isOldUrl = result.isOldUrl
                )
                timestamp = DateTimeParser.parse(td.selectFirst("td"))
                isMainPost = !isSimpleComment
                linkedUrl = postLink
            }
        }

}

private fun fetchPostsOnTopic(table: Element, topic: Topic): List<Post> {
    val tableRows = table.select("tr")!!
    val posts: MutableList<Post> = ArrayList()
    val rowCount: Int = tableRows.size
    var idx = 0
    while (idx < rowCount) { // Expecting only a single <td> in <tr> tag (Sanity check)
        var datum: Element = tableRows[idx++].select("td").first()
        if (isStartOfNewPost(datum)) { // PARSE COMMENT HEADING
            val commentId: String = datum.selectFirst("a").attr("name")
            var authorName: String? = null
//            var authorHref: String? = null
            var isMale: Boolean? = null
            val postHref = datum.select("a")[3].attr("href") // post href is 4th <a> tag
            try {
                val gender = datum.selectFirst("a.user").nextElementSibling()
                when {
                    gender == null -> {

                    }
                    gender.hasClass("m") -> {
                        isMale = true
                    }
                    gender.hasClass("f") -> {
                        isMale = false
                    }
                }
                authorName = datum.selectFirst("a.user").text()
//                authorHref = datum.selectFirst("a.user").attr("href").substringAfter("/")
            } catch (e: Exception) {
                // In some special cases, for deleted users e.g. https://www.nairaland.com/nobody
                for (node in datum.childNodes()) {
                    if (node is TextNode && node.text().trim()
                            .startsWith("by")
                    ) { // assumes text is in the form: "by XXXXXX:" ... hence start at index 3 and ignore the last char
                        val text: String = node.text().trim()
                        authorName = text.substring(3, text.length - 1)
//                        authorHref = authorName.toLowerCase(Locale.ENGLISH)
                        break
                    }
                }
            }
            val dateTime = datum.select("span.s b")
            val time: String = dateTime[0].text()
            val timestamp: Long = when (dateTime.size) {
                3 -> { // if dateTime in form, 9:30pm Dec 07 2016
                    CoreUtils.toTimeStamp(time, dateTime[1].text(), dateTime[2].text())
                }
                2 -> {
                    CoreUtils.toTimeStamp(
                        time,
                        dateTime[1].text(),
                        CoreUtils.currentYear.toString()
                    )
                }
                else -> {
                    CoreUtils.toTimeStamp(
                        time,
                        CoreUtils.currentDate,
                        CoreUtils.currentYear.toString()
                    )
                }
            }

            // PARSE COMMENT BODY
            datum = tableRows[idx++].select("td").first()
            val gender = when (isMale) {
                null -> Gender.Unknown
                true -> Gender.Male
                false -> Gender.Female
            }
            val user = User(authorName!!, _gender = gender)
            val postBody = parsePostBody(datum)
            if (!commentId.equals(postBody.id, ignoreCase = true)) {
                throw ParseException("Post id not equal to PostBody id")
            }
            val post = SimplePost(
                author = user,
                topic = topic,
                id = commentId,
                timestamp = timestamp,
                images = postBody.images,
                isLiked = postBody.isLiked,
                isShared = postBody.isShared,
                parentQuotes = postBody.parentQuotes,
                text = postBody.text,
                editUrl = postBody.modifyPostURL,
                likeUrl = postBody.likeUrl,
                shareUrl = postBody.shareUrl,
                reportUrl = postBody.reportPostURL,
                url = postHref,
                likes = postBody.likes,
                shares = postBody.shares
            )
            posts.add(post)
        } else if (isBlankPost(datum)) {
            posts.add(DeletedPost(datum.selectFirst("a").attr("name")))
            Timber.w("[INFO] Shit like this happens.")
        } else {
            Timber.e("[ERROR] This should never happen")
        }
    }
    return posts
}

private fun isStartOfNewPost(element: Element): Boolean {
    val classes = arrayOf("bold", "l", "pu")
    for (className in classes) {
        if (!element.hasClass(className)) return false
    }
    return true
}

private fun isBlankPost(element: Element): Boolean {
    val classes = arrayOf("pd", "l", "pu")
    for (className in classes) {
        if (!element.hasClass(className)) return false
    }
    return true
}

private fun parsePostBody(tableData: Element): PostBody {
    val postBody = PostBody(tableData.id().substring(2))


    tableData.select("img").forEach {
        val src = it.attr("src")
        if (src.startsWith("/")) {
            it.attr("src", Values.URL + src)
        }
    }

    for (element in tableData.children()) { // TEXT
        when {
            element.tagName().compareTo("div", ignoreCase = true) == 0 -> {
                element.select("img").forEach {
                    if (EmojiParser.isImageEmoji(it.attr("src"))) {
                        var src = it.attr("src")
                        if (src.startsWith("/")) src = Values.URL + src
                        it.attr("src", src)
                        //postBody.addImage(src)
                        //it.remove()
                    }
                }

                // find blockQuotes that "actually" quote a nairaland post
                val blockQuotes = element.select("blockquote")
                for (blockQuote in blockQuotes) {
                    // check if blockQuote element has an anchor tag as its first element
                    // this is usually the link to the quoted text
                    val children = blockQuote.children()
                    if (children.isNotEmpty()) {
                        val elem = children.first()
                        if (elem.tagName() == "a" &&
                            elem.hasAttr("href") &&
                            elem.attr("href").startsWith("/post")
                        ) {
                            // If next node text is a colon, remove
                            val node = elem.nextSibling()
                            if (node != null && node.outerHtml().trim() == ":") {
                                node.remove()
                            }
                            postBody.addQuote(elem.attr("href"))
                        }
                    }
                }

                // Modify highlights so as to be correctly parsed by [Html.fromText]
                element.select("span.highlight")
                    .forEach {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            it.attr("style", "background-color:$DEFAULT_HIGHLIGHT_COLOR;")
                        } else {
                            val font = Element(Tag.valueOf("font"), "").apply {
                                attr("color", DEFAULT_HIGHLIGHT_COLOR)
                            }
                            it.replaceWith(font)
                        }
                    }

                var textHTML = element.html()
                val m = MULTI_LINE_REGEX.matcher(textHTML)
                textHTML = m.replaceAll("<br>\n<br>\n")
                postBody.text = textHTML
            }
            element.hasClass("s") -> {
                // HowManyLikes?
                val likeCountElem = element.selectFirst("#" + likesHTMLId(postBody.id))
                if (likeCountElem != null) {
                    val matcher = NUMBER_REGEX.matcher(likeCountElem.text())
                    if (matcher.find()) {
                        postBody.likes = matcher.group().toInt()
                    }
                }
                // HowManyShares?
                val sharesCountElem = element.selectFirst("#" + sharesHTMLId(postBody.id))
                if (sharesCountElem != null) {
                    val matcher = NUMBER_REGEX.matcher(sharesCountElem.text())
                    if (matcher.find()) {
                        postBody.shares = matcher.group().toInt()
                    }
                }
                // URLS
                val aTags = element.select("> a")
                for (aTag in aTags) {
                    val text = aTag.text().trim()
                    if (text.equals("Share", ignoreCase = true) ||
                        text.equals("Un-Share", ignoreCase = true)
                    ) {
                        postBody.shareUrl = aTag.attr("href")
                        postBody.isShared = text.equals("Un-Share", ignoreCase = true)
                    } else if (text.equals("Like", ignoreCase = true) ||
                        text.equals("Unlike", ignoreCase = true)
                    ) {
                        postBody.likeUrl = aTag.attr("href")
                        postBody.isLiked = text.equals("Unlike", ignoreCase = true)
                    } else if (text.equals("modify", ignoreCase = true)) {
                        postBody.modifyPostURL = aTag.attr("href")
                    } else if (text.equals("Report", ignoreCase = true)) {
                        postBody.reportPostURL = aTag.attr("href")
                    }
                }
            }
            else -> {
                postBody.images.addAll(
                    element.select("img").map {
                        it.attr("src")
                    }
                )

            }
        }
    }
    return postBody
}

@Throws(ParseException::class)
private fun parseSharedPostHeader(td: Element): Result<SharedPostHeader, ErrorResponse> {
    val topic: Topic
    var postAuthor: User? = null
    val postTime: Long
    var sharer: User? = null
    val shareMeta: String

    /*
       TOPIC, AUTHOR
      */
    val authorElem = td.selectFirst("a.user")
    val boardElem = td.selectFirst("a:nth-of-type(4)")
    val topicElem = td.selectFirst("a:nth-of-type(5)")
    val postLink =
        topicElem.attr("href") // post href is 5th <a> tag ... postLink is of form ".../<TOPIC_ID>/<TOPIC>#<POST_ID>"

    // true if sharedPost was just a simple post on a Topic, i.e, not the "MainPost"
    val isSimpleComment = topicElem.text().startsWith("Re:")
    topic = when (val result = parseTopicUrlOrThrow(topicElem.attr("href"))
        .map {
            Topic(
                title = if (isSimpleComment) topicElem.text().substring(4) else topicElem.text(),
                id = it.topicId,
                slug = it.slug,
                isOldUrl = it.isOldUrl,
                refPost = it.refPost,
                linkedPage = it.page
            )
        }
    ) {
        is Ok -> result.value
        is Err -> return result
    }

    val boardUrl = if (boardElem.attr("href").startsWith("/"))
        boardElem.attr("href").substringAfter("/")
    else boardElem.attr("href")
    topic.mainBoard = Board(boardElem.text(), boardUrl)
    if (authorElem != null) {
        postAuthor = User(authorElem.text())
    } else { // In some special cases, for special users e.g. https://www.nairaland.com/nobody
        for (node in td.childNodes()) {
            if (node is TextNode && node.text().trim()
                    .startsWith("by")
            ) { // assumes text is in the form: "by XXXXXX:" ... hence start at index 3 and ignore the last char
                val text =
                    node.text().trim()
                val author = text.substring(3, text.length - 1)
                postAuthor = User(author)
                break
            }
        }
    }
    if (postAuthor == null) {
        throw ParseException("Unable to find author in \"" + td.html() + "\"")
    }
    /*
      Who shared the post? and when? ...
     */
    val sharerInfo = td.selectFirst("span.s")
    val boldTgs: Elements = sharerInfo.select("b")
    shareMeta = boldTgs.first().text()
    try {
        val authorSharing = sharerInfo.selectFirst("a")
        sharer = User(authorSharing.text())
    } catch (e: java.lang.Exception) { // In some special cases, for special users e.g. https://www.nairaland.com/nobody
        for (node in td.childNodes()) {
            if (node is TextNode && node.text().trim()
                    .startsWith("by")
            ) { // assumes text is in the form: "by XXXXXX" ... hence start at index 3
                val text =
                    node.text().trim()
                val name = text.substring(3)
                sharer = User(name)
                break
            }
        }
    }
    if (sharer == null) {
        throw ParseException("Unable to find sharer in \"" + td.html() + "\"")
    }
    val timeShared = DateTimeParser.parseShareTime(sharerInfo)
    val dateTime: Elements = td.select("span.s")[1].select("b") // 2nd "span.s"
    postTime = DateTimeParser.parse(dateTime)

    // if boldTags in form, {"<some text>", "9:30pm",  "Dec 07", "2016"}
//    val timeShared: String = boldTgs.get(1).text()
//    shareTime = if (boldTgs.size() === 4) {
//        Utils.toTimeStamp(timeShared, boldTgs.get(2).text(), boldTgs.get(3).text())
//    } else if (boldTgs.size() === 3) {
//        Utils.toTimeStamp(
//            timeShared,
//            boldTgs.get(2).text(),
//            Utils.currentYear().toString() + ""
//        )
//    } else {
//        Utils.toTimeStamp(timeShared, Utils.currentDate(), Utils.currentYear().toString() + "")
//    }
    /*
        Post time
         */

//    val time: String = dateTime.get(0).text()
    // if dateTime in form, 9:30pm Dec 07 2016
//    postTime = if (dateTime.size() === 3) {
//        Utils.toTimeStamp(time, dateTime.get(1).text(), dateTime.get(2).text())
//    } else if (dateTime.size() === 2) {
//        Utils.toTimeStamp(time, dateTime.get(1).text(), Utils.currentYear().toString() + "")
//    } else {
//        Utils.toTimeStamp(time, Utils.currentDate(), Utils.currentYear().toString() + "")
//    }

    return Ok(
        SharedPostHeader().apply {
            author = postAuthor
            this.topic = topic
            timestamp = postTime
            this.sharer = sharer
            this.sharerInfo = shareMeta
            this.timestampShared = timeShared
            linkedUrl = postLink
            isMainPost = !isSimpleComment
        }
    )
}


@Throws(ParseException::class)
private fun parseLikesAndSharesHeader(td: Element): Result<LikesAndSharesHeader, ErrorResponse> {
    val topic: Topic
    var postAuthor: User? = null
    val postTime: Long
    val info: String

    /*
       TOPIC, AUTHOR
      */
    val authorElem = td.selectFirst("a.user")
    val boardElem = td.selectFirst("a:nth-of-type(4)")
    val topicElem = td.selectFirst("a:nth-of-type(5)")
    val postLink =
        topicElem.attr("href") // post href is 5th <a> tag ... postLink is of form ".../<TOPIC_ID>/<TOPIC>#<POST_ID>"

    // true if sharedPost was just a simple post on a Topic, i.e, not the "MainPost"
    val isSimpleComment = topicElem.text().startsWith("Re:")
    val parseResult = when (val result = parseTopicUrlOrThrow(topicElem.attr("href"))) {
        is Ok -> result.value
        is Err -> return result
    }
    topic = Topic(
        title = if (isSimpleComment) topicElem.text().substring(4) else topicElem.text(),
        id = parseResult.topicId,
        slug = parseResult.slug,
        isOldUrl = parseResult.isOldUrl,
        refPost = parseResult.refPost,
        linkedPage = parseResult.page
    )
    val boardUrl = if (boardElem.attr("href").startsWith("/"))
        boardElem.attr("href").substringAfter("/")
    else boardElem.attr("href")
    topic.mainBoard = Board(boardElem.text(), boardUrl)
    if (authorElem != null) {
        postAuthor = User(authorElem.text())
    } else { // In some special cases, for special users e.g. https://www.nairaland.com/nobody
        for (node in td.childNodes()) {
            if (node is TextNode && node.text().trim()
                    .startsWith("by")
            ) { // assumes text is in the form: "by XXXXXX:" ... hence start at index 3 and ignore the last char
                val text =
                    node.text().trim()
                val author = text.substring(3, text.length - 1)
                postAuthor = User(author)
                break
            }
        }
    }
    if (postAuthor == null) {
        throw ParseException("Unable to find author in \"" + td.html() + "\"")
    }
    /*
      Who shared the post? and when? ...
     */
    val infoElem = td.selectFirst("span.s")
    val boldTgs: Elements = infoElem.select("b")
    info = boldTgs.first().text()
    var nameOfSharer: String? = null
    //e.g.. <b>Shared</b> by <b>Seun</b> at <b>4:30pm</b> on ..
    if (info.equals("shared", true)) {
        // next elem sibling might be an anchor tag
        nameOfSharer = boldTgs.first().nextElementSibling().text()
    }

    val timestampLikedOrShared = DateTimeParser.parseShareTime(infoElem)
    val dateTime: Elements = td.select("span.s")[1].select("b") // 2nd "span.s"
    postTime = DateTimeParser.parse(dateTime)


    return Ok(
        LikesAndSharesHeader().apply {
            author = postAuthor
            this.topic = topic
            timestamp = postTime
            this.info = info
            this.timestampLikedOrShared = timestampLikedOrShared
            linkedUrl = postLink
            isMainPost = !isSimpleComment
            this.nameOfSharer = nameOfSharer
        }
    )
}


/**
 * Helper classes
 */
private class PostBody(val id: String) {
    var likes = 0
    var shares = 0
    var text: String = ""
    var likeUrl: String? = null
    var shareUrl: String? = null
    var isLiked = false
    var isShared = false
    var modifyPostURL: String? = null
    var reportPostURL: String? = null
    var parentQuotes: MutableList<String> = mutableListOf()
    var images: ArrayList<String> = ArrayList()

    fun addQuote(href: String) {
        parentQuotes.add(href)
    }
}

private class TimelineHeader {
    lateinit var author: User
    lateinit var topic: Topic
    var timestamp by Delegates.notNull<Long>()
    lateinit var linkedUrl: String
    var isMainPost by Delegates.notNull<Boolean>()
}

private class SharedPostHeader {
    lateinit var author: User
    lateinit var sharer: User
    lateinit var sharerInfo: String
    lateinit var topic: Topic
    var timestamp by Delegates.notNull<Long>()
    var timestampShared by Delegates.notNull<Long>()
    lateinit var linkedUrl: String
    var isMainPost by Delegates.notNull<Boolean>()
}

private class LikesAndSharesHeader {
    lateinit var author: User
    lateinit var info: String
    lateinit var topic: Topic
    var timestamp by Delegates.notNull<Long>()
    var timestampLikedOrShared by Delegates.notNull<Long>()
    lateinit var linkedUrl: String
    var isMainPost by Delegates.notNull<Boolean>()
    var nameOfSharer: String? = null
}