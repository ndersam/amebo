package com.amebo.core.apis

import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PostListApi {

    @GET("{id}/{slug}/{page}")
    fun fetchTopicPosts(
        @Path("id") id: String,
        @Path("slug") slug: String,
        @Path("page") page: Int
    ): Call<Document>

    @GET("shared/{page}")
    fun fetchPostsSharedWithMe(@Path("page") page: Int): Call<Document>

    @GET("likesandshares/0/{page}")
    fun fetchLikesAndShares(@Path("page") page: Int): Call<Document>

    /**
     * Fetching first page of mentions using this route results in the mentions(MT) notification on
     * https://www.nairaland.com being cleared.
     */
    @GET("mentions")
    fun fetchMentionsPageOne(): Call<Document>

    @GET("search/{username}/0/0/0/{page}")
    fun fetchMentions(
        @Path("username") username: String,
        @Path("page") page: Int
    ): Call<Document>

    @GET("recent/{page}")
    fun fetchRecent(@Path("page") page: Int): Call<Document>

    @GET("following/{page}")
    fun fetchFollowing(@Path("page") page: Int): Call<Document>

    @GET("{user}/posts/{page}")
    fun fetchUserPosts(
        @Path("user") user: String,
        @Path("page") page: Int
    ): Call<Document>

    @GET("search/{query}/{topics_only}/{board}/{images_only}/{page}")
    fun fetchSearchResults(
        @Path("query") query: String,
        @Path("board") board: Int,
        @Path("topics_only") topicsOnly: Int,
        @Path("images_only") imagesOnly: Int,
        @Path("page") page: Int
    ): Call<Document>

    @GET("mylikes/{page}")
    fun fetchMyLikes(@Path("page") page: Int): Call<Document>

    @GET("myshares/{page}")
    fun fetchMyShares(@Path("page") page: Int): Call<Document>

    @GET("post/{postID}")
    fun fetchPageWithPost(@Path("postID") post: String): Call<Document>
}