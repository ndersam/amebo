package com.amebo.core.di

import com.amebo.core.data.local.Database
import com.amebo.core.data.cookies.InMemoryCookieJar
import com.amebo.core.data.cookies.PersistentCookieJar
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.User
import dagger.Module
import dagger.Provides
import okhttp3.CookieJar

@Module
class CookieModule {

    @Provides
    fun provideInMemoryCookieJar() = InMemoryCookieJar()


    @Provides
    fun provideCookieJar(
        database: Database,
        user: User?,
        nairalandSessionObservable: NairalandSessionObservable
    ): CookieJar {
        return when (user) {
            is User -> PersistentCookieJar(database, user.slug, nairalandSessionObservable)
            else -> InMemoryCookieJar()
        }
    }
}

