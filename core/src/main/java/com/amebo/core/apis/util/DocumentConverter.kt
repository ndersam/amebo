package com.amebo.core.apis.util

import com.amebo.core.domain.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import org.jsoup.nodes.Document

class DocumentConverter<R>(networkResponse: NetworkResponse<Document, Document>) :
    NetworkResponseConverter<Document, Document, R, ErrorResponse>(networkResponse) {

    override var onNetworkError: ((NetworkResponse.NetworkError) -> ErrorResponse)? = {
        ErrorResponse.Network
    }

    override var onUnknownError: ((Exception) -> ErrorResponse)? = {
        ErrorResponse.Unknown(exception = it)
    }

    override var onServerError: ((NetworkResponse.ServerError<Document>) -> ErrorResponse)? = { r ->
        val exception = when (val soup = r.body) {
            is Document -> {
                var msg = ""
                soup.selectFirst("body > div > h2")?.let { msg = it.text() }
                if (msg.isBlank())
                    soup.selectFirst("body > h1")?.let { msg = it.text() }
                if (msg.isBlank())
                    msg = soup.selectFirst("title").text()
                Exception(msg)
            }
            else -> throw Exception("Unknown case")
        }
        ErrorResponse.Unknown(exception = exception)
    }
}

fun <R : Any> NetworkResponse<Document, Document>.onSuccess(listener: (NetworkResponse.Success<Document>) -> R): NetworkResponseConverter<Document, Document, R, ErrorResponse> {
    return DocumentConverter<R>(this).onSuccess(listener)
}

fun <R : Any> NetworkResponse<Document, Document>.onServerError(listener: (NetworkResponse.ServerError<Document>) -> ErrorResponse): NetworkResponseConverter<Document, Document, R, ErrorResponse> {
    return DocumentConverter<R>(this).onServerError(
        listener
    )
}

fun <R : Any> NetworkResponse<Document, Document>.onNetworkError(listener: (NetworkResponse.NetworkError) -> ErrorResponse): NetworkResponseConverter<Document, Document, R, ErrorResponse> {
    return DocumentConverter<R>(this).onNetworkError(
        listener
    )
}

fun <R : Any> NetworkResponse<Document, Document>.onUnknownError(listener: (Exception) -> ErrorResponse): NetworkResponseConverter<Document, Document, R, ErrorResponse> {
    return DocumentConverter<R>(this).onUnknownError(
        listener
    )
}