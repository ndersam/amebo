package com.amebo.core.crawler

import com.amebo.core.domain.ErrorResponse
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.jsoup.nodes.Document

internal fun Document.validatePage(): Result<Document, ErrorResponse> {
    if (selectFirst("#up") == null) {
        return Err(ErrorResponse.Unknown(exception = ParseException("Page format not understood")))
    }
    if (selectFirst("#down") == null) {
        return Err(ErrorResponse.Unknown(exception = ParseException("Page format not understood")))
    }
    return Ok(this)
}

fun Document.errorResponse(url: String, throwable: Throwable): ErrorResponse {
    return ErrorResponse.Unknown(exception = DocumentParseException(this, url, throwable))
}