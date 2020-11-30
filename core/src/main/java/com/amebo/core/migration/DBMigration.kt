package com.amebo.core.migration

import android.content.Context
import com.amebo.core.Database
import com.amebo.core.data.cookies.CookieStore
import com.amebo.core.migration.cookies.CookieDatabaseManager
import com.amebo.core.migration.data.DatabaseManager
import com.amebo.core.migration.data.Schema
import timber.log.Timber
import java.util.*

object DBMigration {


    fun migrateFromTwoToDelight(context: Context, db: Database, currentUserName: String?) {
        val dbFile = context.getDatabasePath(Schema.DATABASE_NAME)
        val cookieFile = context.getDatabasePath(CookieDatabaseManager.DATABASE_NAME)
        if (dbFile.exists() && cookieFile.exists()) {
            db.transaction {
                val helper = DatabaseManager.getInstance(context)
                val cookieHelper = CookieDatabaseManager.getInstance(context)
                helper.fetchAccounts().forEach {
                    // Anonymous user was previously stored in database with name == "$"
                    if (it.name.toLowerCase(Locale.ENGLISH) != "$") {
                        db.userAccountQueries.insert(
                            name = it.name,
                            slug = it.url,
                            isLoggedIn = it.name.equals(currentUserName, ignoreCase = true)
                        )
                        val cookie = CookieStore.from(cookieHelper.readCookies(it.url))
                        db.cookieQueries.insert(it.url, cookie)
                    }
                }

                cookieHelper.close()
                helper.close()

                cookieFile.delete()
                dbFile.delete()

                Timber.d("Database migration successful")
            }
        } else {
            Timber.d("Database migration not necessary")
        }
    }

}