package com.amebo.core.crawler.user

import com.amebo.core.domain.Gender
import com.amebo.core.domain.User
import org.jsoup.nodes.Document


internal fun parseFollowers(soup: Document): List<User> {
    val users = mutableListOf<User>()
    soup.select("html body div.body table:nth-of-type(2) tbody tr td")?.forEach {
        val userElem = it.selectFirst("b a")
        val genderStr: String? =
            it.selectFirst("b span.m")?.text() ?: it.selectFirst("b span.f")?.text()
        val gender = when (genderStr?.trim()) {
            "m" -> Gender.Male
            "f" -> Gender.Female
            else -> Gender.Unknown
        }
        val isFollowingUser = it.selectFirst("a").text().equals("un-follow", true)
        val followOrUnFollowUrl = it.selectFirst("a").attr("href")
        if (userElem != null) {
            val data = User.Data(
                gender = gender,
                followUserUrl = followOrUnFollowUrl,
                isFollowing = isFollowingUser
            )
            users.add(User(userElem.text(), data = data))
        }
    }
    return users
}
