package com.amebo.core.common

import android.net.Uri
import com.amebo.core.Database
import com.amebo.core.crawler.topicList.parseTopicUrl
import com.amebo.core.domain.Board
import com.amebo.core.domain.Topic
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.jsoup.Jsoup
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object CoreUtils {
    private val SERVER_TIME_ZONE: TimeZone = TimeZone.getTimeZone("GMT+1")

    // Returns the SERVER current date year
    @JvmStatic
    val currentYear: Int
        get() = LocalDateTime.now(DateTimeZone.forID("Africa/Lagos")).year

    // Returns the SERVER current date in format MMM dd e.g Sep 03
    @JvmStatic
    val currentDate: String
        get() {
            val df = SimpleDateFormat("MMM dd", Locale.getDefault())
            return df.format(LocalDateTime.now(DateTimeZone.forID("Africa/Lagos")).toDate())
        }

    val dateTimeToday: String
        get() {
            val df =
                SimpleDateFormat("yyyy_MMM_dd_mm_ss", Locale.getDefault())
            return df.format(LocalDateTime.now().toDate())
        }

    @JvmStatic
    fun toTimeStamp(time: String, date: String, year: String): Long {
        // DON'T CRASH app because of this
        try {
            val col = time.indexOf(':')
            var hour = time.substring(0, col).toInt()
            val minute = time.substring(col + 1, col + 3).toInt()
            val amPmTime: Char = time[time.length - 2]

            if (amPmTime == 'a') {
                if (hour == 12) {
                    hour = 0
                }
            } else {
                if (hour != 12) {
                    hour += 12
                }
            }

            val dateTime = String.format(
                Locale.getDefault(),
                "%02d",
                hour
            ) + ":" + String.format(
                Locale.getDefault(),
                "%02d",
                minute
            ) + " " + date.trim() + " " + year.trim()
            val dateTimePattern = "HH:mm MMM dd yyyy"
            val f: DateTimeFormatter = DateTimeFormat.forPattern(dateTimePattern)
                .withZone(DateTimeZone.forID("Africa/Lagos"))
            return f.parseDateTime(dateTime).millis
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .log("Exception in CoreUtils.toTimestamp($time, $date, $year): $e")
            return System.currentTimeMillis()
        }
    }

    /**
     * Returns the local time delta from timestamp
     */
    @JvmStatic
    fun howLongAgo(timestamp: Long): String {
        val diff = (DateTime.now().millis - timestamp) / 1000
        return when {
            diff < 60L -> "${diff}s"
            diff < 3600L -> "${diff / 60}m"
            diff < 3600L * 24L -> "${diff / 3600}h"
            diff < 3600L * 24L * 31L -> "${diff / (3600 * 24)}d"
            diff < 3600L * 24L * 365L -> "${diff / (3600 * 24 * 30)}mth"
            else -> "${diff / (3600L * 24L * 365L)}yrs"
        }
    }

    @JvmStatic
    fun textOnly(html: String): String = Jsoup.parse(html).text()

    @JvmStatic
    fun isPostUrl(string: String) = string.startsWith("/post/")

    fun topicUrl(url: String): Topic? {
        if (isNotNairalandUrl(url)) {
            return null
        }
        val result = parseTopicUrl(url) ?: return null
        // Anyway to get de-slug slug??
        return Topic(
            title = result.slug,
            id = result.topicId,
            slug = result.slug,
            linkedPage = result.page,
            refPost = result.refPost,
            isOldUrl = result.isOldUrl
        )
    }

    private fun isNotNairalandUrl(url: String): Boolean {
        if (!url.startsWith('/')) {
            val uri = Uri.parse(url)
            val startsWithWWW = uri.host?.startsWith("www") ?: return false
            val domain = if (startsWithWWW)
                uri.host!!.substring(4)
            else
                uri.host!!
            return domain != "nairaland.com"
        }
        return false
    }

    fun topicUrl(topic: Topic, pageNum: Int) =
        Values.URL + "/" + topic.id + "/" + topic.slug + "/" + pageNum

    @JvmStatic
    fun postID(string: String): String? {
        if (!isPostUrl(string)) {
            return null
        }
        return string.substringAfterLast("/")
    }

    @JvmStatic
    fun timeRegisteredToStamp(dateString: String): Long {
        try {
            val sourceFormat =
                SimpleDateFormat("MMMMM dd, yyyy", Locale.getDefault())
            sourceFormat.timeZone = SERVER_TIME_ZONE
            return sourceFormat.parse(dateString)!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    fun timeRegistered(timestamp: Long): String {
        val date = Date().apply { time = timestamp }
        val sourceFormat =
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sourceFormat.format(date)
    }

    fun cleanHTML(html: String): String = Jsoup.parse(html).text()

    fun isIgnoredPathSegment(text: String): Boolean {
        return !ACCEPTED_SEGMENT_RE.matches(text)
    }

    internal fun isAKnownNairalandPath(
        text: String,
        database: Database,
        matchPatterns: Boolean = false
    ): Boolean {
        val board = database.boardQueries.select(text) { name, slug, id ->
            Board(
                name,
                slug,
                id = id.toInt()
            )
        }.executeAsOneOrNull()
        // is a board
        if (board != null) {
            return true
        }

        return PATHS.filter { matchPatterns || it != "\\d+" }.any { Regex(it).matches(text) }
    }

    private val PATHS = arrayOf(
        "links",
        "news",
        "trending",
        "topics", //new topics
        "followedboards",
        "search",
        "mentions",
        "followed", // followed topics
        "shared",
        "following",
        "likesandshares",
        "recent",
        "\\d+", // topics (new)
        "login", // really?
        "nigeria", // topics (old)
        "mylikes",
        "myshares",
        "newpost",
        "newtopic",
        "modifypost",
        "makereport",
        "sendemail",
        "editprofile",
        "areyoumuslim",
        "do_likeavatar",
        "do_likepost",
        "do_unlikepost",
        "do_share",
        "do_unshare",
        "do_followtopic",
        "do_unfollowtopic",
        "do_makereport",
        "do_followboard",
        "do_unfollowboard",
        "feed",
        "login",
        "register",
        "confirm_email",
        "do_confirm_email",
        "campaign",
        "adrates"
    )

    private val VALID_USER_NAME_REGEX = Regex("^(?=.*[a-zA-Z])[\\da-zA-Z]{4,15}$")


    private val ACCEPTED_SEGMENT_RE =
        Regex("^(?!(adrates|campaign|newpost|newtopic|modifypost|makereport|sendemail|editprofile|areyoumuslim|do_likeavatar|do_likepost|do_unlikepost|do_share|do_unshare|do_followtopic|do_unfollowtopic|do_makereport|do_followboard|do_unfollowboard|feed|login|register|confirm_email|do_confirm_email)\$).+")
}