package com.amebo.amebo.common.extensions

import com.amebo.amebo.common.Resource
import com.amebo.core.domain.ErrorResponse
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result


fun <T : Any> Result<T, ErrorResponse>.toResource(
    existing: T?
): Resource<T> {
    return when (this) {
        is Ok -> Resource.Success(this.value)
        is Err -> Resource.Error(this.error, existing)
    }
}