package com.amebo.core.auth

import com.amebo.core.apis.AuthServiceApi
import com.amebo.core.common.extensions.RawResponse
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.common.extensions.awaitResultResponse
import com.amebo.core.crawler.auth.AuthResponse
import com.amebo.core.crawler.auth.hasLoggedInSuccessfully
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.cookies.CookieStore
import com.amebo.core.data.cookies.InMemoryCookieJar
import com.amebo.core.domain.ErrorResponse
import com.github.michaelbull.result.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class Authenticator @Inject constructor(
    private val api: AuthServiceApi,
    private val context: CoroutineContextProvider
) {
    suspend fun login(
        username: String,
        password: String
    ): Result<CookieStore, ErrorResponse> = withContext(context.IO) {
        api.gotoLogin().awaitResult()
            .flatMap {
                doLogin(username, password)
            }
    }

    private suspend fun doLogin(
        username: String,
        password: String
    ): Result<CookieStore, ErrorResponse> =
        api.login(username, password).awaitResultResponse { resp, result ->
            Ok(hasLoggedInSuccessfully(resp, result) to resp)
        }.flatMap {
            retry(it.first, it.second)
        }.flatMap { code ->
            when (code) {
                AuthResponse.SuccessNoRedirect -> Ok(JAR.getStoreAndClear())
                AuthResponse.WrongUsernameOrPassword -> Err(ErrorResponse.Login)
                AuthResponse.SuccessRedirect, AuthResponse.Failure -> Err(ErrorResponse.Network)
                is AuthResponse.Unknown -> Err(ErrorResponse.Unknown(code.msg))
            }
        }


    private suspend fun retry(
        code: AuthResponse,
        resp: RawResponse
    ): Result<AuthResponse, ErrorResponse> {
        var tries = 1
        var currentCode = code
        var currentResp = resp
        var returnVal: Result<AuthResponse, ErrorResponse> = Ok(currentCode)

        while (tries <= 2 && currentCode == AuthResponse.SuccessRedirect) {
            val url = currentResp.request.url.toString()
            returnVal = api.visit(url).awaitResultResponse { newResp, soup ->
                Ok(hasLoggedInSuccessfully(newResp, soup) to newResp)
            }.onSuccess {
                currentCode = it.first
                currentResp = it.second
                tries += 1
            }.onFailure {
                tries = Int.MAX_VALUE
            }.map { it.first }
        }
        return returnVal
    }

    companion object {

        @JvmField
        val JAR: InMemoryCookieJar = InMemoryCookieJar()
    }
}