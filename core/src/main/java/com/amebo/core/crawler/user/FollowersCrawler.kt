package com.amebo.core.crawler.user

import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.Gender
import com.amebo.core.domain.User
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import org.jsoup.nodes.Document

internal fun parseFollowers(soup: Document): Result<List<User>, ErrorResponse> {
    return runCatching {
        val users = mutableListOf<User>()
        soup.select("html body div.body table:nth-of-type(2) tbody tr td")?.forEach {
            if (it.text().trim() == "You have no followers at this time") {
                return@forEach
            }
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
         users
    }.mapError { ErrorResponse.Unknown(exception = Exception(it)) }
}
