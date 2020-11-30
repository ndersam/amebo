package com.amebo.core.data.cookies

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject

class InMemoryCookieJar @Inject constructor() : CookieJar {

    private var cookieStore = CookieStore()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.getCookies(url.host)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.update(url.host, cookies)
    }

    fun getStoreAndClear(): CookieStore {
        val store = cookieStore
        cookieStore = CookieStore()
        return store
    }
}