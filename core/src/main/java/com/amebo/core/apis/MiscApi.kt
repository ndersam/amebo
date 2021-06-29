package com.amebo.core.apis

import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

internal interface MiscApi {
    @GET
    fun visitPage(@Url url: String): Call<Document>

    @GET("feed")
    fun fetchFeed(): Call<String>
}