package com.amebo.core.auth

import com.amebo.core.apis.AuthServiceApi
import com.amebo.core.crawler.auth.AuthResponse
import com.amebo.core.crawler.auth.hasLoggedInSuccessfully
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.cookies.CookieStore
import com.amebo.core.data.cookies.InMemoryCookieJar
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.ResultWrapper
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Authenticator @Inject constructor(
    private val api: AuthServiceApi,
    private val context: CoroutineContextProvider
) {
    suspend fun login(
        username: String,
        password: String
    ): ResultWrapper<CookieStore, ErrorResponse> = withContext(context.IO) {
        when (api.gotoLogin()) {
            is NetworkResponse.Success -> {
                return@withContext doLogin(username, password)
            }
            else -> return@withContext ResultWrapper.Failure(ErrorResponse.Network)
        }
    }

    private fun doLogin(
        username: String,
        password: String
    ): ResultWrapper<CookieStore, ErrorResponse> {
        var res = api.login(username, password).execute()
        var tries = 1
        var code = hasLoggedInSuccessfully(res)
        while (tries <= 2 && code == AuthResponse.SUCCESS_REDIRECT) {
            res = api.visit(res.raw().request.url.toString()).execute()
            code = hasLoggedInSuccessfully(res)
            tries += 1
        }
        if (code == AuthResponse.SUCCESS_NO_REDIRECT)
            return ResultWrapper.Success(JAR.getStoreAndClear())
        if (code == AuthResponse.WRONG_USERNAME_OR_PASSWORD)
            return ResultWrapper.Failure(ErrorResponse.Login)
        return ResultWrapper.Failure(ErrorResponse.Network)
    }

    companion object {

        @JvmField
        val JAR: InMemoryCookieJar = InMemoryCookieJar()
    }
}