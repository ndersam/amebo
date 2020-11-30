package com.amebo.core.data.datasources.impls

import com.amebo.core.CoreUtils
import com.amebo.core.Database
import com.amebo.core.apis.UserApi
import com.amebo.core.apis.util.onSuccess
import com.amebo.core.crawler.user.fetchUserData
import com.amebo.core.crawler.user.parseFollowers
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.UserDataSource
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.User
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val db: Database,
    private val userApi: UserApi,
    private val context: CoroutineContextProvider
) : UserDataSource {


    override suspend fun fetchData(user: User): ResultWrapper<User.Data, ErrorResponse> =
        withContext(context.IO) {
            if (CoreUtils.isAKnownNairalandPath(user.name, db)) {
                return@withContext ResultWrapper.Failure(
                    ErrorResponse.Unknown(
                        exception = Exception("No user exists with name ${user.name}")
                    )
                )
            }
            val func =
                if (user.slug.toIntOrNull() == null) userApi::fetchUser else userApi::fetchUserViaProfilePath
            func(user.slug.substringAfter('/'))
                .onSuccess {
                    val data = fetchUserData(it.body)
                    insertIfNotExists(user, it.body)
                    data
                }
                .convert()
        }

    override suspend fun fetchCached(user: User): User.Data? = withContext(context.IO) {
        val data = db.userDataQueries.find(user.name.toLowerCase(Locale.ROOT))
            .executeAsOneOrNull() ?: return@withContext null
        fetchUserData(Jsoup.parse(data))
    }

    override suspend fun fetchFollowers(): ResultWrapper<List<User>, ErrorResponse> =
        withContext(context.IO) {
            userApi.fetchFollowers()
                .onSuccess { parseFollowers(it.body) }
                .convert()
        }

    private fun insertIfNotExists(user: User, soup: Document) {
        db.transaction {
            val key = user.name.toLowerCase(Locale.ROOT)
            if (db.userDataQueries.find(key).executeAsOneOrNull() == null) {
                db.userDataQueries.insert(
                    userSlug = key,
                    data = soup.outerHtml()
                )
            } else {
                db.userDataQueries.update(
                    userSlug = key,
                    data = soup.outerHtml()
                )
            }
        }
    }
}