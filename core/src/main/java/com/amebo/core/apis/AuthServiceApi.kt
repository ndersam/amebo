package com.amebo.core.apis

import com.amebo.core.common.Values
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.*

internal interface AuthServiceApi {
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
    fun gotoLogin(): Call<Document>


    @GET
    fun visit(@Url url: String): Call<Document>
}