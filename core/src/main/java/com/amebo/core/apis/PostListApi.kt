package com.amebo.core.apis

import com.amebo.core.domain.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PostListApi {

    @GET("{id}/{slug}/{page}")
    suspend fun fetchTopicPosts(
        @Path("id") id: String,
        @Path("slug") slug: String,
        @Path("page") page: Int
    ): NetworkResponse<Document, ErrorResponse>

    @GET("shared/{page}")
    suspend fun fetchPostsSharedWithMe(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("likesandshares/0/{page}")
    suspend fun fetchLikesAndShares(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    /**
     * Fetching first page of mentions using this route results in the mentions(MT) notification on
     * https://www.nairaland.com being cleared.
     */
    @GET("mentions")
    suspend fun fetchMentionsPageOne(): NetworkResponse<Document, ErrorResponse>

    @GET("search/{username}/0/0/0/{page}")
    suspend fun fetchMentions(
        @Path("username") username: String,
        @Path("page") page: Int
    ): NetworkResponse<Document, ErrorResponse>

    @GET("recent/{page}")
    suspend fun fetchRecent(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("following/{page}")
    suspend fun fetchFollowing(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("{user}/posts/{page}")
    suspend fun fetchUserPosts(
        @Path("user") user: String,
        @Path("page") page: Int
    ): NetworkResponse<Document, ErrorResponse>

    @GET("search/{query}/{topics_only}/{board}/{images_only}/{page}")
    suspend fun fetchSearchResults(
        @Path("query") query: String,
        @Path("board") board: Int,
        @Path("topics_only") topicsOnly: Int,
        @Path("images_only") imagesOnly: Int,
        @Path("page") page: Int
    ): NetworkResponse<Document, ErrorResponse>

    @GET("mylikes/{page}")
    suspend fun fetchMyLikes(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("myshares/{page}")
    suspend fun fetchMyShares(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("post/{postID}")
    suspend fun fetchPageWithPost(@Path("postID") post: String): Response<Document>
}