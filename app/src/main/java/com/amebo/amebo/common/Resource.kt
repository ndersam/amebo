package com.amebo.amebo.common

import com.amebo.core.domain.ErrorResponse

/**
 * Generic class that contains data and status about data loading
 */
sealed class Resource<out T : Any> {
    class Success<out T : Any>(val content: T) : Resource<T>()
    class Error<out T : Any>(val cause: ErrorResponse, val content: T? = null) : Resource<T>()
    class Loading<out T : Any>(val content: T? = null) : Resource<T>()
}