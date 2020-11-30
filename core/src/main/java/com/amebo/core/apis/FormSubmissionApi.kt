package com.amebo.core.apis

import com.amebo.core.Values
import com.amebo.core.domain.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import okhttp3.MultipartBody
import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.*
import java.util.*


interface FormSubmissionApi {

    @FormUrlEncoded
    @POST("do_areyoumuslim")
    suspend fun areYouMuslim(
        @FieldMap fields: Map<String, String>
    ): Response<Document>

    @POST("do_newtopic")
    suspend fun newTopic(
        @Body body: MultipartBody,
        @Header("Referer") referer: String
    ): Response<Document>

    @POST("do_newpost")
    suspend fun newPost(
        @Body body: MultipartBody,
        @Header("Referer") referer: String
    ): Response<Document>

    /**
     * Retrofit automatically adds "Content-Transfer-Encoding: binaryContent-Type: text/plain; charset=utf-8"
     * which is not handled by the nairaland server. Hence manually creating the body
     * https://stackoverflow.com/questions/45700669/upload-picture-to-server-using-retrofit-2#answer-45817050
     */
    @POST("do_modifypost")
    @Headers("Accept: $ACCEPT")
    suspend fun modifyPost(
        @Body body: MultipartBody,
        @Header("Referer") referer: String
    ): Response<Document>

    @FormUrlEncoded
    @POST("do_removeattachment")
    suspend fun removeAttachment(
        @Field("post") postId: String,
        @Field("attachment") attachment: String,
        @Field("session") session: String,
        @Field("redirect") redirect: String,
        @Header("referer") referer: String,
        @Header("Accept") accept: String = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        @Header("Accept-Language") acceptLang: String = "en-US,en;q=0.5"
    ): Document

    @GET("{action}")
    suspend fun postAction(
        @Path("action") action: String,
        @Query("post") postId: String,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Response<Document>

    @FormUrlEncoded
    @POST("do_makereport")
    suspend fun reportPost(
        @Field("reason") reason: String,
        @Field("post") postId: String,
        @Field("redirect") redirect: String,
        @Field("session") session: String,
        @Header("referer") referer: String,
    ): Response<Document>

    @GET("{action}")
    suspend fun topicAction(
        @Path("action") action: String,
        @Query("topic") topicId: String,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Response<Document>

    @GET("{action}")
    suspend fun boardAction(
        @Path("action") action: String,
        @Query("board") boardNo: Int,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Response<Document>

    @GET("{action}")
    suspend fun userFollowAction(
        @Path("action") action: String,
        @Query("member") user: String,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Document


    @GET("{action}")
    suspend fun userAvatarAction(
        @Path("action") action: String,
        @Query("avatar") avatar: String,
        @Query("name") redirect: String,
        @Query("session") session: String
    ): Document

    @POST("do_editprofile")
    suspend fun editProfile(
        @Body body: MultipartBody
    ): Document


    @FormUrlEncoded
    @POST("do_sendemail")
    suspend fun newMail(
        @Field("recipient_name") recipientName: String,
        @Field("subject") subject: String,
        @Field("body") body: String,
        @Field("session") session: String,
        @Header("referer") referer: String = Values.URL + "/sendemail/" + recipientName.toLowerCase(
            Locale.ROOT
        )
    ): Response<Document>

    @FormUrlEncoded
    @POST("do_mailsupermods")
    suspend fun newMail(
        @Field("subject") subject: String,
        @Field("body") body: String,
        @Field("session") session: String,
        @Header("referer") referer: String = Values.URL + "/mailsupermods"
    ): Response<Document>

    @FormUrlEncoded
    @POST("do_mailmods")
    suspend fun newMail(
        @Field("subject") subject: String,
        @Field("body") body: String,
        @Field("session") session: String,
        @Field("board") board: Int,
        @Header("referer") referer: String = Values.URL + "/mailmods?board=" + board
    ): Response<Document>

    @FormUrlEncoded
    @POST("do_dismiss")
    suspend fun dismissMailNotification(
        @Field("session") session: String,
        @Field("redirect") redirect: String,
        @Field("pmsenders") senders: List<String>
    ): NetworkResponse<Document, Document>


    @GET
    suspend fun visit(@Url url: String): NetworkResponse<Document, ErrorResponse>

    companion object {
        private const val ACCEPT =
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
    }
}