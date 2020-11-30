package com.amebo.core.apis

import com.amebo.core.domain.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import org.jsoup.nodes.Document
import retrofit2.http.GET
import retrofit2.http.Path

interface TopicListApi {

    @GET("{board}/{sort}/{page}")
    suspend fun fetchBoardSoup(
        @Path("board") board: String,
        @Path("sort") sort: String,
        @Path("page") page: Int
    ): NetworkResponse<Document, ErrorResponse>


    @GET("links/{page}")
    suspend fun fetchFeaturedSoup(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("trending/{page}")
    suspend fun fetchTrendingSoup(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>


    @GET("topics/{page}")
    suspend fun fetchNewSoup(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>


    @GET("followedboards/{sort}/{page}")
    suspend fun fetchFollowedBoardTopics(@Path("sort") sort: String, @Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("followed/{page}")
    suspend fun fetchFollowedTopics(@Path("page") page: Int): NetworkResponse<Document, ErrorResponse>

    @GET("{username}/topics/{page}")
    suspend fun fetchUserTopics(@Path("username") username: String, @Path("page") page: Int): NetworkResponse<Document, ErrorResponse>
}