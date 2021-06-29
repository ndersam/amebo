package com.amebo.core.apis

import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

internal interface FormApi {

    @GET("newpost")
    fun newPost(@Query("topic") topicId: String): Call<Document>

    @GET("newpost")
    fun quote(
        @Query("topic") topicId: String,
        @Query("post") post: String
    ): Call<Document>

    /**
     * @param referer e.g. https://www.nairaland.com/newpost?topic=6259418&post=96105107
     */
    @GET("getpost")
    fun getPost(
        @Query("post") postID: String,
        @Query("session") session: String,
        @Header("referer") referer: String
    ): Call<String>

    @GET("newtopic")
    fun newTopic(@Query("board") board: Int): Call<Document>

    @GET("modifypost")
    fun modifyPost(
        @Query("redirect") redirect: String,
        @Query("post") post: String
    ): Call<Document>

    @GET("makereport")
    fun reportPost(
        @Query("post") post: String,
        @Query("redirect") redirect: String
    ): Call<Document>

    @GET("sendemail/{username}")
    fun mailUser(@Path("username") username: String): Call<Document>

    @GET("mailsupermods")
    fun mailSuperMods(): Call<Document>

    @GET("mailmods")
    fun mailBoardMods(@Query("board") boardId: Int): Call<Document>

    @GET("editprofile")
    fun editProfile(): Call<Document>
}