package com.amebo.core.di.tests

import android.content.Context
import com.amebo.amebo.data.local.Cookie
import com.amebo.amebo.data.local.UserAccountData
import com.amebo.core.Database
import com.amebo.core.data.cookies.CookieStore
import com.amebo.core.di.DatabaseModule
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides

@Module
class DebugDatabaseModule {

    @Provides
    fun provideSqlDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(Database.Schema, context, "test.db")
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

    @Provides
    fun provideCookieAdapter(): Cookie.Adapter = Cookie.Adapter(
        cookieAdapter = object : ColumnAdapter<CookieStore, String> {
            override fun decode(databaseValue: String): CookieStore =
                CookieStore.from(databaseValue)

            override fun encode(value: CookieStore): String = value.toString()
        }
    )

    @Provides
    fun provideDelightfulDatabase(driver: SqlDriver, cookie: Cookie.Adapter): Database =
        Database(
            driver, cookie, userAccountDataAdapter = UserAccountData.Adapter(
                usersFollowingAdapter = DatabaseModule.usersFollowingAdapter
            )
        )
}