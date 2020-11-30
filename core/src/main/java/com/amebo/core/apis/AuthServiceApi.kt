package com.amebo.core.apis

import com.amebo.core.Values
import com.amebo.core.domain.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.*

interface AuthServiceApi {
    @FormUrlEncoded
    @POST("do_login")
    fun login(
        @Field("name") name: String,
        @Field("password") password: String,
        @Header("referer") referer: String = "${Values.URL}/login",
        @Field("redirect") redirect: String = "",
        @Header("User-Agent") agent: String = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.85 Safari/537.36"
    ): Call<Document>

    @GET("login")
    suspend fun gotoLogin(): NetworkResponse<Document, ErrorResponse>


    @GET
    fun visit(@Url url: String): Call<Document>
}