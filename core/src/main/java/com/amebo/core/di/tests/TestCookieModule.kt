package com.amebo.core.di.tests

import com.amebo.core.data.cookies.InMemoryCookieJar
import dagger.Module
import dagger.Provides
import okhttp3.CookieJar

@Module
class TestCookieModule {
    @Provides
    fun provideCookieJar(): CookieJar = InMemoryCookieJar()
}