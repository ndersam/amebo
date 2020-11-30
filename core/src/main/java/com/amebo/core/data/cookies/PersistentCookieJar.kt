package com.amebo.core.data.cookies

import com.amebo.core.Database
import com.amebo.core.domain.NairalandSessionObservable
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Named

class PersistentCookieJar @Inject constructor(
    private val database: Database, @Named("current_user") val user: String,
    private val nairalandSessionObservable: NairalandSessionObservable
) : CookieJar {
    private val cookieStore: CookieStore by lazy {
        database.cookieQueries.select(user).executeAsOne().cookie
    }

    val session: String?
        get() = cookieStore.session

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.update(url.host, cookies)
        database.cookieQueries.insert(user, cookieStore)
        nairalandSessionObservable.cookieSession = session
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.getCookies(url.host)
    }
}
