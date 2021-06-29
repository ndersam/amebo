package com.amebo.core.apis

import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface TopicListApi {

    @GET("{board}/{sort}/{page}")
    fun fetchBoardSoup(
        @Path("board") board: String,
        @Path("sort") sort: String,
        @Path("page") page: Int
    ): Call<Document>


    @GET("links/{page}")
    fun fetchFeaturedSoup(@Path("page") page: Int): Call<Document>

    @GET("trending/{page}")
    fun fetchTrendingSoup(@Path("page") page: Int): Call<Document>


    @GET("topics/{page}")
    fun fetchNewSoup(@Path("page") page: Int): Call<Document>


    @GET("followedboards/{sort}/{page}")
    fun fetchFollowedBoardTopics(
        @Path("sort") sort: String,
        @Path("page") page: Int
    ): Call<Document>

    @GET("followed/{page}")
    fun fetchFollowedTopics(@Path("page") page: Int): Call<Document>

    @GET("{username}/topics/{page}")
    fun fetchUserTopics(@Path("username") username: String, @Path("page") page: Int): Call<Document>
}