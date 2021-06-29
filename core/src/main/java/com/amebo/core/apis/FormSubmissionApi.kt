package com.amebo.core.apis

import com.amebo.core.common.Values
import okhttp3.MultipartBody
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.*
import java.util.*


internal interface FormSubmissionApi {

    @FormUrlEncoded
    @POST("do_areyoumuslim")
    fun areYouMuslim(
        @FieldMap fields: Map<String, String>
    ): Call<Document>

    @POST("do_newtopic")
    fun newTopic(
        @Body body: MultipartBody,
        @Header("Referer") referer: String
    ): Call<Document>

    @POST("do_newpost")
    fun newPost(
        @Body body: MultipartBody,
        @Header("Referer") referer: String
    ): Call<Document>

    /**
     * Retrofit automatically adds "Content-Transfer-Encoding: binaryContent-Type: text/plain; charset=utf-8"
     * which is not handled by the nairaland server. Hence manually creating the body
     * https://stackoverflow.com/questions/45700669/upload-picture-to-server-using-retrofit-2#answer-45817050
     */
    @POST("do_modifypost")
    @Headers("Accept: $ACCEPT")
    fun modifyPost(
        @Body body: MultipartBody,
        @Header("Referer") referer: String
    ): Call<Document>

    @FormUrlEncoded
    @POST("do_removeattachment")
    fun removeAttachment(
        @Field("post") postId: String,
        @Field("attachment") attachment: String,
        @Field("session") session: String,
        @Field("redirect") redirect: String,
        @Header("referer") referer: String,
        @Header("Accept") accept: String = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        @Header("Accept-Language") acceptLang: String = "en-US,en;q=0.5"
    ): Call<Document>

    @GET("{action}")
    fun postAction(
        @Path("action") action: String,
        @Query("post") postId: String,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Call<Document>

    @FormUrlEncoded
    @POST("do_makereport")
    fun reportPost(
        @Field("reason") reason: String,
        @Field("post") postId: String,
        @Field("redirect") redirect: String,
        @Field("session") session: String,
        @Header("referer") referer: String,
    ): Call<Document>

    @GET("{action}")
    fun topicAction(
        @Path("action") action: String,
        @Query("topic") topicId: String,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Call<Document>

    @GET("{action}")
    fun boardAction(
        @Path("action") action: String,
        @Query("board") boardNo: Int,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Call<Document>

    @GET("{action}")
    fun userFollowAction(
        @Path("action") action: String,
        @Query("member") user: String,
        @Query("redirect") redirect: String,
        @Query("session") session: String
    ): Call<Document>


    @GET("{action}")
    fun userAvatarAction(
        @Path("action") action: String,
        @Query("avatar") avatar: String,
        @Query("name") redirect: String,
        @Query("session") session: String
    ): Call<Document>

    @POST("do_editprofile")
    fun editProfile(
        @Body body: MultipartBody
    ): Call<Document>


    @FormUrlEncoded
    @POST("do_sendemail")
    fun newMail(
        @Field("recipient_name") recipientName: String,
        @Field("subject") subject: String,
        @Field("body") body: String,
        @Field("session") session: String,
        @Header("referer") referer: String = Values.URL + "/sendemail/" + recipientName.toLowerCase(
            Locale.ROOT
        )
    ): Call<Document>

    @FormUrlEncoded
    @POST("do_mailsupermods")
    fun newMail(
        @Field("subject") subject: String,
        @Field("body") body: String,
        @Field("session") session: String,
        @Header("referer") referer: String = Values.URL + "/mailsupermods"
    ): Call<Document>

    @FormUrlEncoded
    @POST("do_mailmods")
    fun newMail(
        @Field("subject") subject: String,
        @Field("body") body: String,
        @Field("session") session: String,
        @Field("board") board: Int,
        @Header("referer") referer: String = Values.URL + "/mailmods?board=" + board
    ): Call<Document>

    @FormUrlEncoded
    @POST("do_dismiss")
    fun dismissMailNotification(
        @Field("session") session: String,
        @Field("redirect") redirect: String,
        @Field("pmsenders") senders: List<String>
    ): Call<Document>


    @GET
    fun visit(@Url url: String): Call<Document>

    companion object {
        private const val ACCEPT =
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
    }
}