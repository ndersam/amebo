package com.amebo.core.data.datasources.impls

import android.graphics.Bitmap
import com.amebo.core.Database
import com.amebo.core.apis.FormSubmissionApi
import com.amebo.core.apis.MiscApi
import com.amebo.core.common.Values
import com.amebo.core.common.extensions.RawResponse
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.common.extensions.awaitResultResponse
import com.amebo.core.crawler.form.parseFollowBoardUrl
import com.amebo.core.crawler.form.parseFollowTopicUrl
import com.amebo.core.crawler.form.parseLikeShareUrl
import com.amebo.core.crawler.form.parsePostForm
import com.amebo.core.crawler.postList.parseTopicPosts
import com.amebo.core.crawler.postList.parseUnknownPostList
import com.amebo.core.crawler.topicList.parseBoardTopics
import com.amebo.core.crawler.topicList.parseFollowedBoards
import com.amebo.core.crawler.topicList.parseFollowedTopics
import com.amebo.core.crawler.user.fetchUserData
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.FormSubmissionDataSource
import com.amebo.core.domain.*
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.nodes.Document
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.set


internal class FormSubmissionDataSourceImpl @Inject constructor(
    private val db: Database,
    private val api: FormSubmissionApi,
    private val miscApi: MiscApi,
    private val context: CoroutineContextProvider
) : FormSubmissionDataSource {

    override suspend fun areYouMuslim(form: AreYouMuslimDeclarationForm): Result<Form?, ErrorResponse> =
        withContext(context.IO) {
            val fields = mutableMapOf<String, String>()
            if (form.accepted) {
                fields["accept"] = form.accept
            } else {
                fields["decline"] = form.decline
            }
            fields["session"] = form.session
            fields["redirect"] = form.redirect
            api.areYouMuslim(fields)
                .awaitResultResponse { resp, result ->
                    parsePostForm(
                        result,
                        resp.request.url.toString()
                    )
                }
        }

    override suspend fun modifyPost(form: ModifyForm): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val images =
                images(
                    form.bundle, "attachment"
                )
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", form.title)
                .addFormDataPart("body", form.body)
                .addFormDataPart("session", form.session)
                .addFormDataPart("redirect", form.redirect)
                .addFormDataPart("post", form.post.toString())
                .apply {
                    images.forEach {
                        addPart(it)
                    }
                }
            api.modifyPost(
                body = builder.build(),
                referer = Values.URL + "/modifypost?redirect=" +
                        form.redirect + "&post=" + form.post.toInt()
            ).awaitResultResponse { resp, soup ->
                soup.parseUnknownPostList(resp.request.url.toString())
            }
        }

    override suspend fun newPost(form: NewPostForm): Result<TopicPostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val referer = Values.URL + "/newpost?topic=" + form.topic
            val images =
                images(
                    form.bundle
                )
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", form.title)
                .addFormDataPart("body", form.body)
                .addFormDataPart("session", form.session)
                .addFormDataPart("topic", form.topic)
                .addFormDataPart("max_post", form.maxPost.toString())
                .apply {
                    if (form.follow) {
                        addFormDataPart("follow", "on")
                    }
                    images.forEach {
                        addPart(it)
                    }
                }

            api.newPost(
                referer = referer,
                body = builder.build()
            ).awaitResultResponse { resp, result ->
                parseTopicPosts(result, resp.request.url.toString())
            }
        }

    override suspend fun newTopic(form: NewTopicForm): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val images =
                images(
                    form.bundle
                )
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", form.title)
                .addFormDataPart("body", form.body)
                .addFormDataPart("session", form.session)
                .addFormDataPart("board", form.board.toString())
                .apply {
                    images.forEach {
                        addPart(it)
                    }
                }
            api.newTopic(
                body = builder.build(),
                referer = Values.URL + "/newtopic?board=" + form.board
            ).awaitResultResponse { resp, result ->
                parseTopicPosts(result, resp.request.url.toString())
            }
        }

    override suspend fun removeAttachments(attachments: List<Attachment>) {
        for (a in attachments) {
            removeAttachment(a)
        }
    }

    override suspend fun removeAttachment(attachment: Attachment) = withContext(context.IO) {
        try {
            api.removeAttachment(
                attachment.post.toString(),
                attachment.id.toString(),
                attachment.session,
                attachment.redirect,
                attachment.referer
            )
            Ok(Unit)
        } catch (e: IOException) {
            Err(ErrorResponse.Network)
        }
    }

    override suspend fun likePost(post: SimplePost): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.likeUrl!!)
            api.postAction(
                action = "do_likepost",
                postId = form.postId,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse(::parsePostListResp)
        }

    override suspend fun unLikePost(post: SimplePost): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.likeUrl!!)
            api.postAction(
                action = "do_unlikepost",
                postId = form.postId,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse(::parsePostListResp)
        }


    override suspend fun sharePost(post: SimplePost): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.shareUrl!!)
            api.postAction(
                action = "do_share",
                postId = form.postId,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse(::parsePostListResp)
        }

    override suspend fun unSharePost(post: SimplePost): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.shareUrl!!)
            api.postAction(
                action = "do_unshare",
                postId = form.postId,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse(::parsePostListResp)
        }

    override suspend fun reportPost(form: ReportPostForm): Result<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            api.reportPost(
                reason = form.reason,
                postId = form.postId,
                session = form.session,
                redirect = form.redirect,
                referer = form.referer
            ).awaitResultResponse(::parsePostListResp)
        }

    override suspend fun followTopic(topicPostListDataPage: TopicPostListDataPage): Result<TopicPostListDataPage, ErrorResponse> =
        followTopic(topicPostListDataPage, true)

    override suspend fun unFollowTopic(topicPostListDataPage: TopicPostListDataPage): Result<TopicPostListDataPage, ErrorResponse> =
        followTopic(topicPostListDataPage, false)

    override suspend fun unFollowTopic(topic: Topic): Result<TopicListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseFollowTopicUrl(topic.followOrUnFollowLink!!)
            api.topicAction(
                action = "do_unfollowtopic",
                topicId = form.topic,
                session = form.session,
                redirect = form.redirect
            ).awaitResult(::parseFollowedTopicListResp)
        }

    override suspend fun followBoard(boardsDataPage: BoardsDataPage): Result<BoardsDataPage, ErrorResponse> =
        followBoard(boardsDataPage, true)


    override suspend fun unFollowBoard(boardsDataPage: BoardsDataPage): Result<BoardsDataPage, ErrorResponse> =
        followBoard(boardsDataPage, false)

    override suspend fun followBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): Result<FollowedBoardsDataPage, ErrorResponse> =
        followBoard(followedBoardsDataPage, board, true)

    override suspend fun unFollowBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): Result<FollowedBoardsDataPage, ErrorResponse> =
        followBoard(followedBoardsDataPage, board, false)

    override suspend fun likeProfilePhoto(
        like: Boolean,
        user: User
    ): Result<User.Data, ErrorResponse> = withContext(context.IO) {
        val url = user.data?.image?.likeUrl!!
        api.visit(url).awaitResult(::fetchUserData)
    }

    override suspend fun newMail(userForm: MailUserForm): Result<User.Data, ErrorResponse> =
        withContext(context.IO) {
            api.newMail(
                recipientName = userForm.recipientName,
                body = userForm.body,
                subject = userForm.subject,
                session = userForm.session
            ).awaitResult(::fetchUserData)
        }

    override suspend fun newMail(form: MailSuperModsForm): Result<Unit, ErrorResponse> =
        withContext(context.IO) {
            api.newMail(
                session = form.session,
                subject =
                form.subject,
                body = form.body
            ).awaitResult {
                // ignore html content for now
                Ok(Unit)
            }
        }

    override suspend fun newMail(form: MailBoardModsForm): Result<BoardsDataPage, ErrorResponse> =
        withContext(context.IO) {
            api.newMail(
                session = form.session,
                subject = form.subject,
                body = form.body,
                board = form.boardNo
            ).awaitResultResponse(::parseBoardResp)
        }

    override suspend fun followUser(
        user: User,
        follow: Boolean
    ): Result<User.Data, ErrorResponse> = withContext(context.IO) {
        val url = user.data?.followUserUrl
        checkNotNull(url) { "Follow User Url must not be null" }
        miscApi.visitPage(url)
            .awaitResult {
                fetchUserData(it)
                    .onSuccess { _ ->
                        insertIfNotExists(user, it)
                    }
            }
    }

    override suspend fun editProfile(form: EditProfileForm): Result<User.Data, ErrorResponse> =
        withContext(context.IO) {
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "gender", when (form.gender) {
                        null, Gender.Unknown -> "-"
                        Gender.Female -> "f"
                        Gender.Male -> "m"
                    }
                )
                .addFormDataPart("birthday", form.birthDate?.day?.value?.toString() ?: "")
                .addFormDataPart("birthmonth", form.birthDate?.month?.value?.toString() ?: "")
                .addFormDataPart("birthyear", form.birthDate?.year?.value?.toString() ?: "")
                .addFormDataPart("personaltext", form.personalText)
                .addFormDataPart("signature", form.signature)
                .addFormDataPart("location", form.location)
                .addFormDataPart("twitter", form.twitter)
                .addFormDataPart("yim", form.yim)
                .addFormDataPart("session", form.session)
                .apply {
                    form.photo?.let {
                        addPart(
                            bitmapImageToPart(
                                fileName = it.first,
                                formPartName = "avatar",
                                bmp = it.second
                            )
                        )
                    }
                    if (form.removeThisImage) {
                        addFormDataPart("removeavatar", "on")
                    }
                }
            api.editProfile(builder.build())
                .awaitResult(::fetchUserData)
        }

    override suspend fun dismissMailNotification(form: DismissMailNotificationForm): Result<Unit, ErrorResponse> =
        withContext(context.IO) {
            api.dismissMailNotification(
                session = form.session,
                redirect = form.redirect,
                senders = form.senders.map { it.slug }
            ).awaitResult()
        }

    private suspend fun followTopic(topicPostListDataPage: TopicPostListDataPage, follow: Boolean) =
        withContext(context.IO) {
            val form = parseFollowTopicUrl(topicPostListDataPage.followOrUnFollowTopicUrl!!)
            api.topicAction(
                action = if (follow) "do_followtopic" else "do_unfollowtopic",
                topicId = form.topic,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse { resp, result ->
                parseTopicResp(resp, result)
            }

        }

    private suspend fun followBoard(boardsDataPage: BoardsDataPage, follow: Boolean) =
        withContext(context.IO) {
            val form = parseFollowBoardUrl(boardsDataPage.followOrUnFollowUrl!!)
            api.boardAction(
                action = if (follow) "do_followboard" else "do_unfollowboard",
                boardNo = form.board,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse { resp, result ->
                parseBoardResp(resp, result)
            }
        }

    private suspend fun followBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board,
        follow: Boolean
    ) =
        withContext(context.IO) {
            val url = followedBoardsDataPage.boards.first {
                it.first.name.equals(
                    board.name,
                    false
                )
            }.second
            val form = parseFollowBoardUrl(url)
            api.boardAction(
                action = if (follow) "do_followboard" else "do_unfollowboard",
                boardNo = form.board,
                session = form.session,
                redirect = form.redirect
            ).awaitResultResponse { resp, result ->
                parseFollowedBoardResp(resp, result)
            }

        }

    private fun parsePostListResp(resp: RawResponse, soup: Document) =
        soup.parseUnknownPostList(resp.request.url.toString())


    private fun parseTopicResp(resp: RawResponse, soup: Document) =
        parseTopicPosts(soup, resp.request.url.toString())


    private fun parseBoardResp(resp: RawResponse, soup: Document) =
        soup.parseBoardTopics(resp.request.url.toString())

    private fun parseFollowedTopicListResp(soup: Document) = soup.parseFollowedTopics()


    private fun parseFollowedBoardResp(resp: RawResponse, soup: Document) =
        soup.parseFollowedBoards(resp.request.url.toString())


    private fun insertIfNotExists(user: User, soup: Document) {
        db.transaction {
            val key = user.name.toLowerCase(Locale.ROOT)
            if (db.userDataQueries.find(key).executeAsOneOrNull() == null) {
                db.userDataQueries.insert(
                    userSlug = key,
                    data_ = soup.outerHtml()
                )
            } else {
                db.userDataQueries.update(
                    userSlug = key,
                    data_ = soup.outerHtml()
                )
            }
        }
    }

    companion object {
        private fun determineImageMimeType(filename: String): String {
            if (filename.endsWith("png"))
                return "image/png"
            return if (filename.endsWith("gif")) "image/gif" else "image/jpeg"
        }

        private fun bitmapImageToPart(
            fileName: String,
            bmp: Bitmap,
            formPartName: String
        ): MultipartBody.Part {
            val mediaType = determineImageMimeType(
                fileName
            ).toMediaTypeOrNull()
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            bmp.recycle()
            return MultipartBody.Part.createFormData(
                formPartName,
                fileName,
                byteArray.toRequestBody(mediaType, 0, byteArray.size)
            )
        }

        private fun images(
            bundle: ImageBundle,
            formPartName: String = "attachment",
            size: Int = 4
        ): ArrayList<MultipartBody.Part> {
            val arr = arrayListOf<MultipartBody.Part>()

            bundle.data.forEachIndexed { _, it ->
                val fileName = it.first
                val bmp = it.second
                arr.add(
                    bitmapImageToPart(
                        fileName = fileName,
                        bmp = bmp,
                        formPartName = formPartName
                    )
                )
            }

            var idx = bundle.data.size
            while (idx < size) {
                val part = MultipartBody.Part.createFormData(
                    formPartName,
                    "",
                    "".toRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
                arr.add(part)
                idx++
            }

            return arr
        }
    }
}
