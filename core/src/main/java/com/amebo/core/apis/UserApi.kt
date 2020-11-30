package com.amebo.core.apis

import com.haroldadmin.cnradapter.NetworkResponse
import org.jsoup.nodes.Document
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    /**
     * For urls like https://www.nairaland.com/profile/100
     */
    @GET("profile/{profileNum}")
    suspend fun fetchUserViaProfilePath(@Path("profileNum") username: String): NetworkResponse<Document, Document>

    @GET("{username}")
    suspend fun fetchUser(@Path("username") username: String): NetworkResponse<Document, Document>

    @GET("followers")
    suspend fun fetchFollowers(): NetworkResponse<Document, Document>
}