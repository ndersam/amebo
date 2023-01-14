package com.amebo.core.data.datasources.impls

import com.amebo.core.data.local.Database
import com.amebo.core.apis.UserApi
import com.amebo.core.common.CoreUtils
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.crawler.user.fetchUserData
import com.amebo.core.crawler.user.parseFollowers
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.UserDataSource
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.User
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import javax.inject.Inject

internal class UserDataSourceImpl @Inject constructor(
    private val db: Database,
    private val userApi: UserApi,
    private val context: CoroutineContextProvider
) : UserDataSource {


    override suspend fun fetchData(user: User): Result<User.Data, ErrorResponse> =
        withContext(context.IO) {
            if (CoreUtils.isAKnownNairalandPath(user.name, db)) {
                return@withContext Err(
                    ErrorResponse.Unknown(
                        exception = Exception("No user exists with name ${user.name}")
                    )
                )
            }
            val func = when {
                user.slug.toIntOrNull() == null -> userApi::fetchUser
                else -> userApi::fetchUserViaProfilePath
            }
            func(user.slug.substringAfter('/'))
                .awaitResult {
                    fetchUserData(it)
                        .onSuccess { _ ->
                            insertIfNotExists(user, it)
                        }
                }
        }

    override suspend fun fetchCached(user: User): User.Data? = withContext(context.IO) {
        val data = db.userDataQueries.find(user.name.toLowerCase(Locale.ROOT))
            .executeAsOneOrNull() ?: return@withContext null
        fetchUserData(Jsoup.parse(data)).component1()
    }

    override suspend fun fetchFollowers(): Result<List<User>, ErrorResponse> =
        withContext(context.IO) {
            userApi.fetchFollowers()
                .awaitResult { parseFollowers(it) }
        }

    private fun insertIfNotExists(user: User, soup: Document) {
        db.transaction {
            val key = user.name.toLowerCase(Locale.ROOT)
            if (db.userDataQueries.find(key).executeAsOneOrNull() == null) {
                db.userDataQueries.insert(
                    userSlug = key,
                    data_ = soup.outerHtml()
                )
            } else {
                db.userDataQueries.update(
                    userSlug = key,
                    data_ = soup.outerHtml()
                )
            }
        }
    }
}