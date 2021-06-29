package com.amebo.core.crawler.user

import com.amebo.core.common.CoreUtils.currentDate
import com.amebo.core.common.CoreUtils.currentYear
import com.amebo.core.common.CoreUtils.timeRegisteredToStamp
import com.amebo.core.common.CoreUtils.toTimeStamp
import com.amebo.core.crawler.DateTimeParser
import com.amebo.core.crawler.ParseException
import com.amebo.core.crawler.isTag
import com.amebo.core.crawler.topicList.parseOtherTopics
import com.amebo.core.domain.Board
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.Gender
import com.amebo.core.domain.User
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern

private val TIME_REGISTERED =
    Pattern.compile("(\\d+)\\s+([a-zA-Z]+)")
private val PROFILE_LIKES =
    Pattern.compile("(\\d+)\\s+like(s)?")
private val USER_ID = Pattern.compile("member=(\\d+)")
private val SPACES = Pattern.compile("\\s+")
private val STH_ON_STH = Pattern.compile("\\S*On\\S*")

// TODO: Catch parsing exceptions on this page
/**
 * Returns a new User object with same name and url as {@param user0} with updated statistics
 *
 * @param soup  Optional HTML containing user statistics.
 * @return new User object with updated statistics.
 */
@Throws(ParseException::class)
fun fetchUserData(
    soup: Document
): Result<User.Data, ErrorResponse> {

    val userData = User.Data()
    val imageBuilder = UserImageBuilder()
    soup.select("table").forEach { table ->
        if (!table.hasAttr("id") && table.hasAttr("summary")) {
            if (table.attr("summary") == "friends" && table.select("tbody tr").size == 2) {
                table.select("tbody tr td a").forEach { anchor ->
                    userData.following.add(User(anchor.text()))
                }
            }
        } else {
            parseUserData(userData, table, imageBuilder)
        }
    }
    userData.image = imageBuilder.build()
    return Ok(userData)

}

@Throws(ParseException::class)
private fun parseUserData(
    data: User.Data,
    table: Element,
    imageBuilder: UserImageBuilder
) : Result<Unit, ErrorResponse> {

    val tRows = table.select("tbody tr td")
    // User Profile Information
    if (tRows.size == 1) {
        tRows[0].children().forEach { element ->
            when {
                element.isTag("p") -> {
                    parsePTags(data, element, imageBuilder)
                }
                element.isTag("a") -> {
                    parseATags(data, element)
                }
                else -> {
                    Timber.d("Not a P: %s", element.toString())
                }
            }
        }
    } else {
        val header = table.selectFirst("tbody tr th a")
        data.topicCount = header.text().split(SPACES)
            .toTypedArray()[2]
            .toInt() // text is of form "View All 1054 Topics"

        data.latestTopics.addAll(
            when(val result = parseOtherTopics(tRows)) {
                is Ok -> result.value
                is Err -> return result
            }
        )
    }
    return Ok(Unit)
}

@Throws(ParseException::class)
private fun parseATags(
    data: User.Data,
    element: Element
) {
    val href = element.attr("href")
    var isSet = false
    if (href.contains("do_followmember")) {
        data.isFollowing = false
        isSet = true
    } else if (href.contains("do_unfollowmember")) {
        data.isFollowing = true
        isSet = true
    }
    if (isSet) {
        data.followUserUrl = href
    }
    if (isSet) {
        val m = USER_ID.matcher(href)
        if (!m.find()) {
            throw ParseException("Unable to find User Id in \"$href\"")
        }
        data.userID = m.group(1)!!
    }
}

@Throws(ParseException::class)
private fun parsePTags(
    data: User.Data,
    pTag: Element,
    imageBuilder: UserImageBuilder
) {
    val hasBoldTitle =
        !pTag.children().isEmpty() && pTag.child(0) != null && pTag.child(0).tagName() == "b"

    if (hasBoldTitle) {
        val name = pTag.child(0).text().trim()
        var value = ""
        try {
            for (idx in 1 until pTag.childNodes().size) {
                val sibling = pTag.childNodes()[idx]
                value = if (sibling is TextNode) {
                    val text = sibling.text()
                    if (idx == 1 && text.startsWith(":")) { // remove the starting colon
                        value + text.substring(1).trim { it <= ' ' }
                    } else {
                        value + text
                    }
                } else {
                    value + (sibling as Element).html()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (textEquals(name, "personal text")) {
            data.personalText = value
        } else if (textEquals(name, "gender")) {
            data.gender = if (value == "m") {
                Gender.Male
            } else {
                Gender.Female
            }
        } else if (textEquals(name, "location")) {
            data.location = value
        } else if (textEquals(name, "time registered")) {
            data.timeRegistered = timeRegisteredToStamp(value)
        } else if (textEquals(name, "last seen")) {
            val res = value.split(STH_ON_STH).toTypedArray()
            if (res.size == 1) {
                data.lastSeen = try {
                    toTimeStamp(
                        value,
                        currentDate,
                        currentYear.toString() + ""
                    )
                } catch (e: Exception) {
                    -1
                }
            } else { // Usually in the form like ... 3:30pm On Oct 04
                var date = res[1].trim { it <= ' ' }
                val year: String
                val comma = date.indexOf(',')
                if (comma == -1) {
                    year = currentYear.toString() + ""
                } else {
                    year = date.substring(comma + 1).trim()
                    date = date.substring(0, comma)
                }
                data.lastSeen = toTimeStamp(res[0].trim(), date, year)
            }
        } else if (textEquals(name, "time spent online")) {
            val m = TIME_REGISTERED.matcher(value)
            val sb = StringBuilder()
            var item: String
            while (m.find()) {
                item = when {
                    m.group(2)!!.startsWith("year") -> {
                        "y "
                    }
                    m.group(2)!!.startsWith("month") -> {
                        "m "
                    }
                    m.group(2)!!.startsWith("day") -> {
                        "d "
                    }
                    m.group(2)!!.startsWith("hour") -> {
                        "h "
                    }
                    m.group(2)!!.startsWith("minute") -> {
                        "min "
                    }
                    m.group(2)!!.startsWith("second") -> {
                        "s "
                    }
                    else -> {
                        throw ParseException(
                            "Encountered illegal character while parsing date \"" + m.group(
                                2
                            ) + "\""
                        )
                    }
                }
                sb.append(m.group(1)).append(item)
            }
            data.timeSpentOnline = sb.toString()
        } else if (textEquals(name, "sections most active in:")) {
            val anchors = pTag.select("a")
            for (anchor in anchors) { // TODO -> Find board number
                data.boardsMostActiveIn.add(
                    Board(
                        anchor.text(),
                        anchor.attr("href"),
                        -1
                    )
                )
            }
        } else if (textEquals(name, "moderates:")) {
            val anchors = pTag.select("a")
            for (anchor in anchors) { // TODO -> Find board number
                data.boardsModeratesIn.add(
                    Board(
                        anchor.text(),
                        anchor.attr("href"),
                        -1
                    )
                )
            }
        } else if (textEquals(name, "Signature")) {
            data.signature = value
        } else if (textEquals(name, "twitter")) {
            data.twitter = value
        } else if (textEquals(name, "yim")) {
            data.yim = value
        } else if (textEquals(name, "time uploaded:")) {
            val tags = pTag.select("b")
            imageBuilder.timestamp = DateTimeParser.parse(tags.subList(1, tags.size))

            if (pTag.nextElementSibling()?.hasClass("s") == true && pTag.nextElementSibling()
                    ?.tagName() == "p"
            ) {
                val imageLikesElemPTag = pTag.nextElementSibling()

                // e.g. "<b>3 likes</b> <b>23 old likes</b>
                imageLikesElemPTag.selectFirst("b")?.let { likesElem ->
                    imageBuilder.likes = likesElem.text().split(SPACES).firstOrNull()?.toIntOrNull()
                        ?: imageBuilder.likes
                }

                imageLikesElemPTag.select("b").getOrNull(1)?.let { oldLikeElem ->
                    imageBuilder.oldLikes =
                        oldLikeElem.text().split(Regex("\\s+")).firstOrNull()?.toIntOrNull()
                            ?: imageBuilder.oldLikes
                }

                imageLikesElemPTag.selectFirst("a")?.let { likeATag ->
                    imageBuilder.isLiked =
                        likeATag.text().trim().equals("unlike", ignoreCase = true)
                    imageBuilder.likeUrl = likeATag.attr("href")
                }

            }

        }
    } else {
        if (pTag.childNodeSize() == 1 && pTag.child(0).tagName() == "img") {
            imageBuilder.url = pTag.child(0).attr("src")
        } else if (!pTag.children().isEmpty()) {
            val aTags = pTag.select("> a")
            if (aTags != null) {
                for (aTag in aTags) { // POST_COUNT
                    if (aTag.attr("href").endsWith("/posts")) {
                        val postCountText =
                            aTag.text().trim { it <= ' ' }.split(SPACES)
                                .toTypedArray()[3] // text is of form "View Racoon's posts (14)"
                        data.postCount =
                            postCountText.substring(1, postCountText.length - 1).toInt()
                    } else if (aTag.attr("href").endsWith("/topics")) {
                        val topicCountText =
                            aTag.text().trim { it <= ' ' }.split(SPACES)
                                .toTypedArray()[3] // text is of form "View Racoon's topics (114)"
                        data.topicCount =
                            topicCountText.substring(1, topicCountText.length - 1).toInt()
                    }
                }
            }
        }
    }
}

private fun textEquals(lhs: String, rhs: String): Boolean {
    return lhs.equals(rhs, ignoreCase = true)
}

fun fetchFollowers(soup: Document): List<User> {
    val userList: MutableList<User> =
        ArrayList()
    val rows =
        soup.select("html body div.body table:nth-of-type(2) tbody tr td")
    if (rows != null) {
        for (elem in rows) {
            val userElem = elem.selectFirst("b a")
            if (userElem != null) userList.add(
                User(userElem.text())
            )
        }
    }
    return userList
}

private class UserImageBuilder {
    var url: String? = null
    var oldLikes = 0
    var timestamp = -1L
    var likes = 0
    var isLiked = false
    var likeUrl: String? = null

    fun build(): User.Image? {
        if (url == null) return null
        return User.Image(
            url = url!!,
            likes = likes,
            oldLikes = oldLikes,
            isLiked = isLiked,
            timestamp = timestamp,
            likeUrl = likeUrl
        )
    }
}