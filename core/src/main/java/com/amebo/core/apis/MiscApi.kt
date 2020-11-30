package com.amebo.core.apis

import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface MiscApi {
    @GET
    suspend fun visitPage(@Url url: String): Response<Document>

    @GET("feed")
    suspend fun fetchFeed(): String
}