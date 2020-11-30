package com.amebo.core.apis.util

import com.amebo.core.domain.ResultWrapper
import com.haroldadmin.cnradapter.NetworkResponse

/**
 * @param T -> Retrofit Success input
 * @param U -> Retrofit Error input
 * @param R -> Output Success
 * @param S -> Output Error
 */
open class NetworkResponseConverter<T : Any, U : Any, R, S>(private val networkResponse: NetworkResponse<T, U>) {
    /**
     * Response
     */
    protected open var onSuccess: ((NetworkResponse.Success<T>) -> R)? = null

    /**
     * Non 2xx-3xx status code response
     */
    protected open var onServerError: ((NetworkResponse.ServerError<U>) -> S)? = null

    /**
     * Request was never made
     */
    protected open var onNetworkError: ((NetworkResponse.NetworkError) -> S)? = null

    /**
     * Parsing Error
     */
    protected open var onUnknownError: ((Exception) -> S)? = null

    fun onSuccess(listener: (NetworkResponse.Success<T>) -> R): NetworkResponseConverter<T, U, R, S> {
        this.onSuccess = listener
        return this
    }

    fun onServerError(listener: (NetworkResponse.ServerError<U>) -> S): NetworkResponseConverter<T, U, R, S> {
        this.onServerError = listener
        return this
    }

    fun onNetworkError(listener: (NetworkResponse.NetworkError) -> S): NetworkResponseConverter<T, U, R, S> {
        this.onNetworkError = listener
        return this
    }

    fun onUnknownError(listener: (Exception) -> S): NetworkResponseConverter<T, U, R, S> {
        this.onUnknownError = listener
        return this
    }

    fun convert(): ResultWrapper<R, S> {
        return try {
            when (networkResponse) {
                is NetworkResponse.Success -> {
                    checkNotNull(onSuccess)
                    ResultWrapper.Success(onSuccess!!(networkResponse))
                }
                is NetworkResponse.NetworkError -> {
                    checkNotNull(onNetworkError)
                    ResultWrapper.Failure(onNetworkError!!(networkResponse))
                }
                is NetworkResponse.ServerError -> {
                    checkNotNull(onServerError)
                    ResultWrapper.Failure(onServerError!!(networkResponse))
                }
            }
        } catch (e: Exception) {
            checkNotNull(onUnknownError)
            ResultWrapper.Failure(onUnknownError!!(e))
        }
    }
}