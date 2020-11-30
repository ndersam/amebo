package com.amebo.amebo.common.extensions

import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.ResultWrapper

inline fun <T : Any, U : Any> ResultWrapper<T, U>.act(
    success: (T) -> Unit,
    failure: (U) -> Unit = {}
) {
    if (this is ResultWrapper.Success) {
        success(this.data)
    } else {
        failure((this as ResultWrapper.Failure).data)
    }
}

inline fun <T : Any, U : Any, reified R : Any> ResultWrapper<T, U>.map(
    success: (T) -> R,
    failure: (U) -> R
): R {
    return if (this is ResultWrapper.Success) {
        success(this.data)
    } else {
        failure((this as ResultWrapper.Failure).data)
    }
}

fun <T : Any> ResultWrapper<T, ErrorResponse>.toResource(
    existing: T?
): Resource<T> {
    return when (this) {
        is ResultWrapper.Success -> Resource.Success(this.data)
        is ResultWrapper.Failure -> Resource.Error(this.data, existing)
    }
}

inline fun <T : Any, U : Any> Event<ResultWrapper<T, U>>.map(
    success: (T) -> Unit,
    failure: (U) -> Unit
): Any? {
    return getContentIfNotHandled()?.map(success, failure)
}