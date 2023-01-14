package com.amebo.core.di

import android.content.Context
import com.amebo.amebo.data.local.Cookie
import com.amebo.amebo.data.local.UserAccountData
import com.amebo.core.data.local.Database
import com.amebo.core.data.cookies.CookieStore
import com.amebo.core.domain.User
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideSqlDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(Database.Schema, context, "appDatabase.db")
//
//    @Singleton
//    @Provides
//    fun provideAccountAdapter() = com.amebo.amebo.data.local.Account.Adapter(
//            usersFollowingAdapter = object : ColumnAdapter<List<User>, String> {
//                override fun decode(databaseValue: String): List<User> {
//                    if (databaseValue.isBlank())
//                        return emptyList()
//
//                    return databaseValue.split(",").map {
//                        Timber.d(it)
//                        val arr = it.split("|")
//                        User(arr[0], arr[1])
//                    }
//                }
//
//                override fun encode(value: List<User>) = when (value.isEmpty()) {
//                    true -> ""
//                    false -> value.joinToString(",") { it.name + "|" + it.url }
//                }
//            }
//    )

    @Singleton
    @Provides
    fun provideCookieAdapter(): Cookie.Adapter = Cookie.Adapter(
        cookieAdapter = object : ColumnAdapter<CookieStore, String> {
            override fun decode(databaseValue: String): CookieStore =
                CookieStore.from(databaseValue)

            override fun encode(value: CookieStore): String = value.toString()
        }
    )

    @Singleton
    @Provides
    fun provideDelightfulDatabase(driver: SqlDriver, cookie: Cookie.Adapter): Database =
        Database(
            driver, cookie, userAccountDataAdapter = UserAccountData.Adapter(
                usersFollowingAdapter = usersFollowingAdapter
            )
        )

    companion object {
        val usersFollowingAdapter = object : ColumnAdapter<List<User>, String> {
            override fun decode(databaseValue: String): List<User> {
                if (databaseValue.isBlank())
                    return emptyList()

                return databaseValue.split(",").map {
                    val arr = it.split("|")
                    User(arr[0])
                }
            }

            override fun encode(value: List<User>) = when (value.isEmpty()) {
                true -> ""
                false -> value.joinToString(",") { it.name + "|" + it.slug }
            }
        }
    }
}