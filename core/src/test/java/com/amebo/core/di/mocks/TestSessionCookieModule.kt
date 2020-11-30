package com.amebo.core.di

import com.amebo.core.data.cookies.CookieStore
import dagger.Module
import dagger.Provides
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl


@Module
class TestSessionCookieModule {
    @Provides
    fun provideCookieJar(): CookieJar = TestSessionCookieJar()
}

class TestSessionCookieJar : CookieJar {
    private val cookieString: String =
        """{"id":"default","map":{"www.nairaland.com":[{"domain":"nairaland.com","expiresAt":$cfduidExpiry,"hostOnly":false,"httpOnly":true,"name":"__cfduid","path":"/","persistent":true,"secure":true,"value":"$cfduid"},{"domain":"www.nairaland.com","expiresAt":$sessionExpiry,"hostOnly":true,"httpOnly":true,"name":"session","path":"/","persistent":true,"secure":true,"value":"$session"}]}}""".trimIndent()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return CookieStore.from(cookieString).getCookies(url.host)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {


    }

    companion object {
        const val session: String =
            "3BCD198A29A914C8898316974096E810AFC586BFA87F3F193DB4D67C1C6C9888"
        const val cfduid: String = "d64f8a8381939c99fc40573d8bc2430fa1605421666"
        const val sessionExpiry: String = "1636957666638"
        const val cfduidExpiry: String = "1608013666000"
    }
}