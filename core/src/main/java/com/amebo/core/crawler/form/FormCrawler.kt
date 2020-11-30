package com.amebo.core.crawler.form

import android.net.Uri
import com.amebo.core.crawler.ParseException
import com.amebo.core.crawler.TopicLockedException
import com.amebo.core.domain.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.net.URLDecoder

internal fun parsePostForm(soup: Document, url: String): Form? {
    val uri = Uri.parse(url)!!
    if (uri.pathSegments.size == 0){
        return null
    }
    return when(val segment = uri.pathSegments.first()){
        "newpost" -> parseNewPost(soup).asSuccess.data
        "newtopic" -> parseNewTopic(soup).asSuccess.data
        "modifypost" -> parseModifyPost(soup).asSuccess.data
        else -> throw ParseException("Could not understand uri $segment")
    }
}

internal fun parseNewPost(soup: Document): ResultWrapper<NewPostForm, AreYouMuslimDeclarationForm> {
    throwIfTopicLocked(soup)
    val muslim = parseMuslimDeclarationIfExists(soup)
    if (muslim != null) {
        return ResultWrapper.failure(muslim)
    }
    val body = soup.selectFirst("#body").text()
    val maxPost = soup.selectFirst("input[name=\"max_post\"]").attr("value").toInt()
    val topic = soup.selectFirst("input[name=\"topic\"]").attr("value")!!
    val title = soup.selectFirst("input[name=\"title\"]").attr("value")!!
    val session = soup.selectFirst("input[name=\"session\"]").attr("value")!!
    val tBody: Element? = soup.selectFirst("table[summary=\"posts\"] tbody")
    return ResultWrapper.success(
        NewPostForm(
            body = body,
            maxPost = maxPost,
            topic = topic,
            title = title,
            session = session,
            quotablePosts = if (tBody == null) emptyList() else fetchQuotablePosts(tBody)
        )
    )
}

internal fun fetchQuotablePosts(tBody: Element): List<QuotablePost> {
    return tBody.select("td").map {
        val aTags = it.select("a")
        val gender = run {
            val m = it.select("span.m")
            if (m != null) {
                m.remove()
                return@run Gender.Male
            }
            val f = it.select("span.f")
            if (f != null) {
                return@run Gender.Female
            }
            Gender.Unknown
        }
        val id = aTags[0].attr("name")!!
        val url = aTags[1].attr("href")!!
        val number = aTags[1].text().toInt()
        val quoteTagIdx: Int
        val user = if (aTags[2].text() == "Quote Post") {
            quoteTagIdx = 2
            User("Nobody")
        } else {
            quoteTagIdx = 3
            User(aTags[2].text(), _gender = gender)
        }

        val quotePostTag = aTags[quoteTagIdx]
        val session = run {
            val text = quotePostTag.attr("onclick")
            val last = text.lastIndexOf('\'')
            val first = text.lastIndexOf('\'', startIndex = last - 1)
            text.substring(first + 1 until last)
        }

        val toRemove = mutableListOf<Node>()
        for (node in it.childNodes()) {
            toRemove.add(node)
            // Break at the
            if (node == quotePostTag) {
                break
            }
        }
        toRemove.forEach { n -> n.remove() }
        val content = it.html()


        QuotablePost(
            id = id,
            number = number,
            author = user,
            text = content,
            url = url,
            session = session
        )
    }
}

internal fun parseNewTopic(soup: Document): ResultWrapper<NewTopicForm, AreYouMuslimDeclarationForm> {
    val muslim = parseMuslimDeclarationIfExists(soup)
    if (muslim != null) {
        return ResultWrapper.failure(muslim)
    }
    val body = soup.selectFirst("#body").text()
    val postFormTitle = soup.selectFirst("#postformtitle").attr("value")
    val board = soup.selectFirst("input[name=\"board\"]").attr("value").toInt()
    val session = soup.selectFirst("input[name=\"session\"]").attr("value")!!
    return ResultWrapper.success(
        NewTopicForm(
            body = body,
            board = board,
            title = postFormTitle,
            session = session
        )
    )
}

internal fun parseEditProfile(soup: Document): EditProfileForm {
    fun nthRow(row: Int) =
        "form[action=\"/do_editprofile\"] > table > tbody > tr:nth-child(${row}) > td"

    val email =
        with(
            soup.selectFirst(nthRow(1))
                .childNode(1) as TextNode
        ) {
            text().substringAfter(':').trim()
        }

//    fun birthDateElem(row: Int) = soup.select("${nthRow(2)} > select:nth-of-type(${row}) > option")


    val birthDay =
        with(soup.select("${nthRow(2)} > select:nth-of-type(1) > option")
            .first { it.hasAttr("selected") }) {
            val day = attr("value").toIntOrNull()
            if (day == null) null else Day(day)
        }

    val birthMonth =
        with(soup.select("${nthRow(2)} > select:nth-of-type(2) > option")
            .first { it.hasAttr("selected") }) {
            val month = attr("value").toIntOrNull()
            if (month == null) null else Month(month, text())
        }

    val birthYear =
        with(soup.select("${nthRow(2)} > select:nth-of-type(3) > option")
            .first { it.hasAttr("selected") }) {
            val year = attr("value").toIntOrNull()
            if (year == null) null else Year(year)
        }

    val earliestYear =
        with(soup.select("${nthRow(2)} > select:nth-of-type(3) > option")[2]) {
            Year(attr("value").toInt())
        }

    val birthDate = if (birthDay != null && birthMonth != null && birthYear != null)
        BirthDate(
            day = birthDay,
            month = birthMonth,
            year = birthYear
        )
    else null

    val gender = with(soup.select("${nthRow(3)} select option").first { it.hasAttr("selected") }) {
        when (attr("value")) {
            "m" -> Gender.Male
            "f" -> Gender.Female
            else -> Gender.Unknown
        }
    }

    // maxLength = 255 xters
    val personalText =
        soup.selectFirst("${nthRow(4)} input[name=\"personaltext\"]").attr("value") ?: ""
    val signature = soup.selectFirst("${nthRow(5)} input[name=\"signature\"]").attr("value") ?: ""
    val location = soup.selectFirst("${nthRow(7)} input[name=\"location\"]").attr("value") ?: ""
    val yim = soup.selectFirst(("${nthRow(8)} input[name=\"yim\"]")).attr("value") ?: ""
    val twitter = soup.selectFirst("${nthRow(9)} input[name=\"twitter\"]").attr("value") ?: ""
    val session = soup.selectFirst("input[name=\"session\"]").attr("value")

    return EditProfileForm(
        email = email,
        birthDate = birthDate,
        gender = gender,
        location = location,
        signature = signature,
        personalText = personalText,
        yim = yim,
        twitter = twitter,
        session = session,
        photo = null,
        removeThisImage = false,
        earliestYear = earliestYear
    )
}


internal fun parseMailToUserForm(soup: Document): ResultWrapper<MailUserForm, ErrorResponse> {


    val sessionElem: Element? = soup.selectFirst("#postform > input[name=session]")
    if (sessionElem == null) {
        val title = soup.selectFirst("body > div > h2").text()
        if (title.equals("You have sent too many anonymous mails. Please wait.", true)) {
            return ResultWrapper.failure(ErrorResponse.TooManyAnonymousMails)
        }
        if (title.equals("Insufficent Permissions For Mailing 1", true)) {
            return ResultWrapper.failure(ErrorResponse.InsufficientMailingPermissions)
        }
        return ResultWrapper.failure(ErrorResponse.Unknown(title))
    }
    val session = sessionElem.attr("value")!!
    val recipientName = soup.selectFirst("#postform > input[name=recipient_name]").attr("value")!!
    var subject: String? = null
    var canSendEmail = true
    val body = soup.selectFirst("#body").text() ?: ""

    soup.selectFirst("#postform input[name=subject][disabled]")?.let { element ->
        subject = element.attr("value")!!
        canSendEmail = false
    }

    return ResultWrapper.success(
        MailUserForm(
            session = session,
            recipientName = recipientName,
            body = body,
            canSendMail = canSendEmail,
            subject = subject ?: ""
        )
    )
}


internal fun parseMailSuperModsForm(soup: Document): ResultWrapper<MailSuperModsForm, ErrorResponse> {
    val sessionElem: Element? = soup.selectFirst("#postform > input[name=session]")
    if (sessionElem == null) {
        val title = soup.selectFirst("body > div > h2").text()
        if (title.equals("please go back and try again in about 5 minutes", true)) {
            return ResultWrapper.failure(ErrorResponse.TooManyModEmails)
        }
        return ResultWrapper.failure(ErrorResponse.Unknown(title))
    }
    val session = sessionElem.attr("value")!!
    val subject: String? = null
    val body = soup.selectFirst("#body").text() ?: ""


    return ResultWrapper.success(
        MailSuperModsForm(
            session = session,
            body = body,
            subject = subject ?: ""
        )
    )
}

internal fun parseMailBoardModsForm(soup: Document): ResultWrapper<MailBoardModsForm, ErrorResponse> {
    val sessionElem: Element? = soup.selectFirst("#postform > input[name=session]")
    if (sessionElem == null) {
        val title = soup.selectFirst("body > div > h2").text()
        if (title.equals("please go back and try again in about 5 minutes", true)) {
            return ResultWrapper.failure(ErrorResponse.TooManyModEmails)
        }
        return ResultWrapper.failure(ErrorResponse.Unknown(title))
    }
    val session = sessionElem.attr("value")!!
    val subject: String? = null
    val body = soup.selectFirst("#body").text() ?: ""
    val boardId = soup.selectFirst("#postform > input[name=board]").attr("value").toInt()

    return ResultWrapper.success(
        MailBoardModsForm(
            session = session,
            body = body,
            subject = subject ?: "",
            boardNo = boardId
        )
    )
}

internal fun parseModifyPost(soup: Document): ResultWrapper<ModifyForm, AreYouMuslimDeclarationForm> {
    throwIfTopicLocked(soup)
    val muslim = parseMuslimDeclarationIfExists(soup)
    if (muslim != null) {
        return ResultWrapper.failure(muslim)
    }
    val body = soup.selectFirst("#body").text()
    val titleElem = soup.select("input[name=\"title\"]").firstOrNull()!!
    val titleEditable = titleElem.attr("type")!! != "hidden"
    val title = titleElem.attr("value")!!
    val session = soup.selectFirst("#session").attr("value")!!
    val post = soup.selectFirst("input[name=\"post\"]").attr("value")!!
    val redirect =
        URLDecoder.decode(soup.selectFirst("input[name=\"redirect\"]").attr("value")!!, "UTF-8")


    val attachments = mutableListOf<Attachment>()
    soup.select("form[action=\"/do_removeattachment\"]")?.forEach {
        val session = it.selectFirst("input[name=\"session\"]").attr("value")!!
        val post = it.selectFirst("input[name=\"post\"]").attr("value")!!
        val attachment = it.selectFirst("input[name=\"attachment\"]").attr("value")!!
        val redirect = it.selectFirst("input[name=\"redirect\"]").attr("value")!!
        val name = (it.selectFirst("input[type=\"submit\"]").previousSibling() as TextNode).text()
        attachments.add(Attachment(name, attachment.toLong(), post.toLong(), session, redirect))
    }
    return ResultWrapper.success(
        ModifyForm(
            body = body,
            title = title,
            titleEditable = titleEditable,
            post = post.toLong(),
            redirect = redirect,
            session = session,
            attachments = attachments
        )
    )
}

internal fun parseLikeShareUrl(url: String): LikeShareUrlParseResult {
    // e.g.
    // https://www.nairaland.com/do_likepost?session=36B627F92E780A6F1334CB6A4C1C783EF27598C93034719EA8B016B364746A36&redirect=%2F5653372%2Fihedioha-returns-supreme-court-seeks%2386142304&post=86142304
    // https://www.nairaland.com/do_share?session=36B627F92E780A6F1334CB6A4C1C783EF27598C93034719EA8B016B364746A36&redirect=%2F5653372%2Fihedioha-returns-supreme-court-seeks%2386142382&post=86142382


    val uri = Uri.parse(url)
    val redirect = URLDecoder.decode(uri.getQueryParameter("redirect")!!, "UTF-8")
    val postId = uri.getQueryParameter("post")!!
    val session = uri.getQueryParameter("session")!!
    return LikeShareUrlParseResult(
        postId,
        redirect,
        session
    )
}

//internal fun parseReportPost(soup: Document): ReportPostForm {
//    val session = soup.selectFirst("input[name=\"session\"]").attr("value")!!
//    val post = soup.selectFirst("input[name=\"post\"]").attr("value")!!
//    val redirect =
//        URLDecoder.decode(soup.selectFirst("input[name=\"redirect\"]").attr("value")!!, "UTF-8")
//    return ReportPostForm(post, session, redirect)
//}

internal fun parseFollowTopicUrl(url: String): FollowTopicUrlParseResult {
    // e.g.
    // https://www.nairaland.com/do_followtopic?session=E457407E3EB4DA8BCB31ADAB5E23D726AEAC4EC871BF1B516130B415318743C7&topic=5831212&redirect=%2F5831212%2Ffellow-married-women-mothers-how
    val uri = Uri.parse(url)
    val redirect = URLDecoder.decode(uri.getQueryParameter("redirect")!!, "UTF-8")
    val topic = uri.getQueryParameter("topic")!!
    val session = uri.getQueryParameter("session")!!
    return FollowTopicUrlParseResult(
        topic,
        redirect,
        session
    )
}

internal fun parseFollowBoardUrl(url: String): FollowBoardUrlParseResult {
    // e.g.
    // https://www.nairaland.com/do_unfollowboard?session=0604D5BBB63ECE595E29A10BB026394518DA0707BB685D93BEF0BC2062E7EE6F&board=20&redirect=%2Fpolitics
    val uri = Uri.parse(url)
    val redirect = URLDecoder.decode(uri.getQueryParameter("redirect")!!, "UTF-8")
    val session = uri.getQueryParameter("session")!!
    val board = uri.getQueryParameter("board")!!
    return FollowBoardUrlParseResult(
        board.toInt(),
        redirect,
        session
    )
}

internal fun parseMuslimDeclarationIfExists(soup: Document): AreYouMuslimDeclarationForm? {
    val heading = soup.selectFirst("body > div > h2") ?: return null
    val form = "form[action=\"/do_areyoumuslim\"]"
    if (heading.text().equals("Are you a muslim?", ignoreCase = true)) {
        val redirect = soup.selectFirst("$form input[name=\"redirect\"]").attr("value")!!
        val accept = soup.selectFirst("$form input[name=\"accept\"]").attr("value")!!
        val session = soup.selectFirst("$form input[name=\"session\"]").attr("value")!!
        val decline = soup.selectFirst("$form input[name=\"decline\"]").attr("value")!!
        return AreYouMuslimDeclarationForm(
            redirect = redirect,
            accept = accept,
            session = session,
            decline = decline
        )
    }
    return null
}

private fun throwIfTopicLocked(soup: Document) {
    val closed = soup.selectFirst("div.body h2")
    if (closed != null && closed.text().trim().equals("topic locked", ignoreCase = true)) {
        throw TopicLockedException
    }
}