package com.amebo.core.crawler.auth

import com.amebo.core.crawler.SessionParser
import org.jsoup.nodes.Document
import retrofit2.Response
import timber.log.Timber

internal enum class AuthResponse {
    SUCCESS_NO_REDIRECT,
    SUCCESS_REDIRECT,
    FAILURE,
    WRONG_USERNAME_OR_PASSWORD
}

internal fun hasLoggedInSuccessfully(res: Response<Document>): AuthResponse {
    val soup = res.body()
    if (!res.isSuccessful || soup == null) {
        return AuthResponse.FAILURE
    }

    if (res.raw().request.url.toString() == "https://www.nairaland.com/?x=2178109") {
        return AuthResponse.SUCCESS_NO_REDIRECT
    }

    return parseLoginResponse(soup)
}


internal fun parseLoginResponse(soup: Document): AuthResponse {
    soup.select("h2").forEach {
        if (it.text().toString().trim() == "Logging In...") {
            return AuthResponse.SUCCESS_REDIRECT
        }
    }

    soup.select("table tr td b").forEach {
        if (it.text().toString().trim() == "Please refresh this page to complete your login!") {
            return AuthResponse.SUCCESS_REDIRECT
        }
    }

    soup.select("h2").forEach {
        if (it.text().toString().equals("Wrong Username or Password", true)) {
            return AuthResponse.WRONG_USERNAME_OR_PASSWORD
        }
    }
    soup.select("b").forEach {
        if (it.text().toString().equals("re-enter your username and password", true)) {
            return AuthResponse.WRONG_USERNAME_OR_PASSWORD
        }
    }

    try {
        val session = SessionParser.parse(soup.selectFirst("#up"))
        if (session.activeUser != null || session.isLoggedIn) {
            return AuthResponse.SUCCESS_NO_REDIRECT
        }
    } catch (ex: Exception) {
        Timber.e(ex)
    }

    return AuthResponse.FAILURE
}