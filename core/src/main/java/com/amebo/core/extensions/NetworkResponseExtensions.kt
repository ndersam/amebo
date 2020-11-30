package com.amebo.core.extensions

import com.haroldadmin.cnradapter.NetworkResponse

fun <T : Any, U : Any> NetworkResponse<T, U>.either(
    success: (NetworkResponse.Success<T>) -> Unit,
    failure: (NetworkResponse<T, U>) -> Unit
) {
    if (this is NetworkResponse.Success) {
        success(this)
    } else {
        failure(this)
    }
}

inline fun <T : Any, U : Any, reified R> NetworkResponse<T, U>.map(
    success: (NetworkResponse.Success<T>) -> R,
    failure: (NetworkResponse<T, U>) -> R
): R {
    return if (this is NetworkResponse.Success) {
        success(this)
    } else {
        failure(this)
    }
}

//inline fun <T : Any, U : Any, reified R, reified S> NetworkResponse<T, U>.onSuccess(noinline listener: (NetworkResponse.Success<T>) -> R): NetworkResponseConverter<T, U, R, S> {
//    return NetworkResponseConverter<T, U, R, S>(this).onSuccess(listener)
//}
//
//inline fun <T : Any, U : Any, reified R, reified S> NetworkResponse<T, U>.onServerError(noinline listener: (NetworkResponse.ServerError<U>) -> S): NetworkResponseConverter<T, U, R, S> {
//    return NetworkResponseConverter<T, U, R, S>(this).onServerError(listener)
//}
//
//inline fun <T : Any, U : Any, reified R, reified S> NetworkResponse<T, U>.onNetworkError(noinline listener: (NetworkResponse.NetworkError) -> S): NetworkResponseConverter<T, U, R, S> {
//    return NetworkResponseConverter<T, U, R, S>(this).onNetworkError(listener)
//}
//
//inline fun <T : Any, U : Any, reified R, reified S> NetworkResponse<T, U>.onUnknownError(noinline listener: (Exception) -> S): NetworkResponseConverter<T, U, R, S> {
//    return NetworkResponseConverter<T, U, R, S>(this).onUnknownError(listener)
//}