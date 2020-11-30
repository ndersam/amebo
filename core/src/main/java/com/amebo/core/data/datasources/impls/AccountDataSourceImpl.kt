package com.amebo.core.data.datasources.impls

import android.content.Context
import com.amebo.core.Database
import com.amebo.core.apis.UserApi
import com.amebo.core.crawler.ParseException
import com.amebo.core.crawler.user.fetchUserData
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.AccountDataSource
import com.amebo.core.domain.*
import com.amebo.core.extensions.map
import com.amebo.core.migration.DBMigration
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountDataSourceImpl @Inject constructor(
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

    override suspend fun displayPhoto(user: User): ResultWrapper<DisplayPhoto, ErrorResponse> =
        withContext(context.IO) {
            // FIXME: Store full url to avoid this
            val func =
                if (user.slug.toIntOrNull() == null) api::fetchUser else api::fetchUserViaProfilePath
            func(user.slug).map({
                try {
                    val userData = fetchUserData(it.body)
                    val url = userData.image?.url
                    ResultWrapper.success(if (url == null) NoDisplayPhoto else DisplayPhotoUrl(url))
                } catch (e: ParseException) {
                    ResultWrapper.failure(ErrorResponse.Parse)
                }
            }, {
                ResultWrapper.failure(ErrorResponse.Network)
            })
        }

    override suspend fun migrate(context: Context, currentUserName: String?) {
        DBMigration.migrateFromTwoToDelight(context, db, currentUserName)
    }
}