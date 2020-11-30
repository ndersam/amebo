package com.amebo.core.crawler

import com.amebo.core.BuildConfig
import com.amebo.core.CoreUtils
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object DateTimeParser {
    /**
     * @param datum [Element] of tag "span" and class "s"
     * @return timestamp
     */
    fun parse(datum: Element): Long {
        val dateTime = datum.select("span.s b")
        val time = dateTime[0].text()
        if (BuildConfig.DEBUG && dateTime.size !in 1..3) {
            error("Number of <b> tags in 'span.s' selection must be in range 1..3 (both inclusive)")
        }
        return when (dateTime.size) {
            3 -> // if dateTime in form, 9:30pm Dec 07 2016
                CoreUtils.toTimeStamp(time, dateTime[1].text(), dateTime[2].text())
            2 ->// if dateTime in form, 9:30pm Dec 07
                CoreUtils.toTimeStamp(time, dateTime[1].text(), CoreUtils.currentYear.toString())
            else -> // if dateTime in form, 9:30pm
                CoreUtils.toTimeStamp(time, CoreUtils.currentDate, CoreUtils.currentYear.toString())
        }
    }

    fun parse(dateTime: List<Element>): Long {
        val filteredDateTime = dateTime.filter { it.text().isNotBlank() }
        val time = filteredDateTime[0].text()
        if (BuildConfig.DEBUG && filteredDateTime.size !in 1..3) {
            error("Number of <b> tags in 'span.s' selection must be in range 1..3 (both inclusive)")
        }
        return when (filteredDateTime.size) {
            3 -> // if dateTime in form, 9:30pm Dec 07 2016
                CoreUtils.toTimeStamp(time, filteredDateTime[1].text(), filteredDateTime[2].text())
            2 ->// if dateTime in form, 9:30pm Dec 07
                CoreUtils.toTimeStamp(time, filteredDateTime[1].text(), CoreUtils.currentYear.toString())
            else -> // if dateTime in form, 9:30pm
                CoreUtils.toTimeStamp(time, CoreUtils.currentDate, CoreUtils.currentYear.toString())
        }

    }

    /**
     * @param dateTime [Elements] list of bald tags (size must be in range 1..3, both inclusive)
     * @return timestamp [Long]
     */
    fun parse(dateTime: Elements): Long {
        check(dateTime.size in 1..3) {
            "Number of <b> tags in 'span.s' selection must be in range 1..3 (both inclusive)"
        }

        val time = dateTime[0].text()
        return when (dateTime.size) {
            3 -> // if dateTime in form, 9:30pm Dec 07 2016
                CoreUtils.toTimeStamp(time, dateTime[1].text(), dateTime[2].text())
            2 ->// if dateTime in form, 9:30pm Dec 07
                CoreUtils.toTimeStamp(time, dateTime[1].text(), CoreUtils.currentYear.toString())
            else -> // if dateTime in form, 9:30pm
                CoreUtils.toTimeStamp(time, CoreUtils.currentDate, CoreUtils.currentYear.toString())
        }
    }


    fun parseShareTime(datum: Element): Long {
        /**
         *  offset = 1 for `<b>Liked</b> at <b>4:30pm</b> ...` type of expression
         *              = 2 for `<b>Shared</b> by <b>Seun</b> at 4:30pm ...` type of expression
         */
        val boldTags = datum.select("b")
        val offset = if (boldTags.first().text().equals("shared", true) && boldTags[1].text().equals("you", true)) {
            2
        } else 1

        val dateTime = boldTags.subList(offset, boldTags.size)
        val time = dateTime[0].text()
//        assert(dateTime.size in 1..4) {
//            "Number of <b> tags in 'span.s' selection must be in range 1..4 (both inclusive)"
//        }
        return when (dateTime.size) {
            3 ->  // if dateTime in form, {"9:30pm",  "Dec 07", "2016"}
                CoreUtils.toTimeStamp(time, dateTime[1].text(), dateTime[2].text())
            2 ->// if dateTime in form, 9:30pm Dec 07
                CoreUtils.toTimeStamp(time, dateTime[1].text(), CoreUtils.currentYear.toString())
            else -> // if dateTime in form, 9:30pm
                CoreUtils.toTimeStamp(time, CoreUtils.currentDate, CoreUtils.currentYear.toString())
        }

    }

}