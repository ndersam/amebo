package com.amebo.core.data.datasources.impls

import android.content.Context
import com.amebo.core.data.local.Database
import com.amebo.core.apis.UserApi
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.crawler.user.fetchUserData
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.AccountDataSource
import com.amebo.core.domain.*
import com.amebo.core.migration.DBMigration
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AccountDataSourceImpl @Inject  constructor(
    private val db: Database,
    private val api: UserApi,
    private val context: CoroutineContextProvider
) : AccountDataSource {

    override suspend fun loadAccountUsers(): List<UserAccount> = withContext(context.IO) {
        db.userAccountQueries.selectAll { _, name, _, isLoggedIn ->
            RealUserAccount(User(name), isLoggedIn)
        }.executeAsList()
    }

    override suspend fun removeUser(account: RealUserAccount) {
        val user = account.user
        withContext(context.IO) {
            db.transaction {
                db.cookieQueries.deleteThis(user.slug)
                db.userAccountDataQueries.deleteThis(user.slug)
                db.userAccountQueries.deleteThis(user.slug)
            }
        }
    }

    override suspend fun login(account: RealUserAccount) = withContext(context.IO) {
        db.userAccountQueries.login(account.user.slug)
    }

    override suspend fun logout(account: RealUserAccount) = logout(account.user)

    override suspend fun logout(user: User) = withContext(context.IO) {
        db.userAccountQueries.logout(user.slug)
    }

    override suspend fun displayPhoto(user: User): Result<DisplayPhoto, ErrorResponse> =
        withContext(context.IO) {
            // FIXME: Store full url to avoid this
            val func = when {
                user.slug.toIntOrNull() == null -> api::fetchUser
                else -> api::fetchUserViaProfilePath
            }
            func(user.slug).awaitResult {
                fetchUserData(it)
                    .map { userData ->
                        val url = userData.image?.url
                        if (url == null) NoDisplayPhoto else DisplayPhotoUrl(url)
                    }
            }
        }

    override suspend fun migrate(context: Context, currentUserName: String?) {
        DBMigration.migrateFromTwoToDelight(context, db, currentUserName)
    }
}