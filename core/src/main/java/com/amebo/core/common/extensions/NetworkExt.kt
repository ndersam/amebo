@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.amebo.core.common.extensions

import com.amebo.core.domain.ErrorResponse
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

typealias RawResponse = okhttp3.Response

suspend fun <T> Call<T>.awaitResult(): Result<Unit, ErrorResponse> =
    awaitResultResponse { _, _ -> Ok(Unit) }

suspend fun <T, R> Call<T>.awaitResult(map: (T) -> Result<R, ErrorResponse>): Result<R, ErrorResponse> =
    awaitResultResponse { _, t -> map(t) }

suspend fun <T, R> Call<T>.awaitResultResponse(map: (resp: RawResponse, result: T) -> Result<R, ErrorResponse>): Result<R, ErrorResponse> =
    suspendCancellableCoroutine { continuation ->
        try {
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    errorHappened(throwable)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        try {
                            continuation.resume(map(response.raw(), response.body()!!)) {

                            }
                        } catch (throwable: Throwable) {
                            errorHappened(throwable)
                        }
                    } else {
                        errorHappened(HttpException(response))
                    }
                }

                private fun errorHappened(throwable: Throwable) {
                    continuation.resume(Err(asNetworkException(throwable))) {

                    }
                }
            })
        } catch (throwable: Throwable) {
            continuation.resume(Err(asNetworkException(throwable))) {

            }
        }

        continuation.invokeOnCancellation {
            cancel()
        }
    }

private fun asNetworkException(ex: Throwable): ErrorResponse {
    return when (ex) {
        is IOException -> {
            ErrorResponse.Network
        }
        is HttpException -> extractHttpExceptions(ex)
        else -> ErrorResponse.Unknown("Something went wrong...")
    }
}

private fun extractHttpExceptions(ex: HttpException): ErrorResponse {
    when (ex.response()?.code()) {
        in 400..Int.MAX_VALUE -> {
            FirebaseCrashlytics.getInstance()
                .log("HttpException: StackTrace=${ex.stackTraceToString()}")
        }
    }

    val body = ex.response()?.errorBody()?.string() ?: ""
    val soup = Jsoup.parse(body)
    var msg = ""
    soup.selectFirst("body > div > h2")?.let { msg = it.text() }
    if (msg.isBlank())
        soup.selectFirst("body > h1")?.let { msg = it.text() }
    if (msg.isBlank())
        msg = soup.selectFirst("title").text()
    if (msg.isNotBlank()) {
        return ErrorResponse.Unknown(msg = msg)
    }


    return ErrorResponse.Unknown(msg = body)
}