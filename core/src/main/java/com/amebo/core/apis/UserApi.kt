package com.amebo.core.apis

import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface UserApi {
    /**
     * For urls like https://www.nairaland.com/profile/100
     */
    @GET("profile/{profileNum}")
    fun fetchUserViaProfilePath(@Path("profileNum") username: String): Call<Document>

    @GET("{username}")
    fun fetchUser(@Path("username") username: String): Call<Document>

    @GET("followers")
    fun fetchFollowers(): Call<Document>
}