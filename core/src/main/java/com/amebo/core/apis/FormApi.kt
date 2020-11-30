package com.amebo.core.apis

import com.amebo.core.domain.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface FormApi {

    @GET("newpost")
    suspend fun newPost(@Query("topic") topicId: String): Response<Document>

    @GET("newpost")
    suspend fun quote(
        @Query("topic") topicId: String,
        @Query("post") post: String
    ): Response<Document>

    /**
     * @param referer e.g. https://www.nairaland.com/newpost?topic=6259418&post=96105107
     */
    @GET("getpost")
    suspend fun getPost(
        @Query("post") postID: String,
        @Query("session") session: String,
        @Header("referer") referer: String
    ): String

    @GET("newtopic")
    suspend fun newTopic(@Query("board") board: Int): Response<Document>

    @GET("modifypost")
    suspend fun modifyPost(
        @Query("redirect") redirect: String,
        @Query("post") post: String
    ): Response<Document>

    @GET("makereport")
    suspend fun reportPost(
        @Query("post") post: String,
        @Query("redirect") redirect: String
    ): Document

    @GET("sendemail/{username}")
    suspend fun mailUser(@Path("username") username: String): NetworkResponse<Document, ErrorResponse>

    @GET("mailsupermods")
    suspend fun mailSuperMods(): NetworkResponse<Document, ErrorResponse>

    @GET("mailmods")
    suspend fun mailBoardMods(@Query("board") boardId: Int): NetworkResponse<Document, ErrorResponse>

    @GET("editprofile")
    suspend fun editProfile(): Document
}