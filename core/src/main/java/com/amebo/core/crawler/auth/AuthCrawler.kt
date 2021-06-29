package com.amebo.core.crawler.auth

import com.amebo.core.common.extensions.RawResponse
import com.amebo.core.crawler.SessionParser
import org.jsoup.nodes.Document
import timber.log.Timber


sealed class AuthResponse {
    object SuccessNoRedirect: AuthResponse()
    object SuccessRedirect: AuthResponse()
    object Failure: AuthResponse()
    object WrongUsernameOrPassword: AuthResponse()
    class Unknown(val msg: String): AuthResponse()
}

internal fun hasLoggedInSuccessfully(res: RawResponse, soup: Document): AuthResponse {
    if (res.request.url.toString() == "https://www.nairaland.com/?x=2178109") {
        return AuthResponse.SuccessNoRedirect
    }
    return parseLoginResponse(soup)
}


internal fun parseLoginResponse(soup: Document): AuthResponse {
    soup.selectFirst("h2")?.let {
        if (it.text().toString().equals("Wrong Username or Password", true)) {
            return AuthResponse.WrongUsernameOrPassword
        }
        if (it.text().toString().trim() == "Logging In...") {
            return AuthResponse.SuccessRedirect
        }
        return AuthResponse.Unknown(it.text())
    }

    soup.select("table tr td b").forEach {
        if (it.text().toString().trim() == "Please refresh this page to complete your login!") {
            return AuthResponse.SuccessRedirect
        }
    }

    soup.select("b").forEach {
        if (it.text().toString().equals("re-enter your username and password", true)) {
            return AuthResponse.WrongUsernameOrPassword
        }
    }

    try {
        val session = SessionParser.parse(soup.selectFirst("#up"))
        if (session.activeUser != null || session.isLoggedIn) {
            return AuthResponse.SuccessNoRedirect
        }
    } catch (ex: Exception) {
        Timber.e(ex)
    }

    return AuthResponse.Failure
}