package com.amebo.core.data.datasources.impls

import android.graphics.Bitmap
import com.amebo.core.Database
import com.amebo.core.Values
import com.amebo.core.apis.FormSubmissionApi
import com.amebo.core.apis.MiscApi
import com.amebo.core.apis.util.onSuccess
import com.amebo.core.crawler.ParseException
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
import com.amebo.core.extensions.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.nodes.Document
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject


class FormSubmissionDataSourceImpl @Inject constructor(
    private val db: Database,
    private val api: FormSubmissionApi,
    private val miscApi: MiscApi,
    private val context: CoroutineContextProvider
) : FormSubmissionDataSource {

    override suspend fun areYouMuslim(form: AreYouMuslimDeclarationForm): ResultWrapper<Form?, ErrorResponse> =
        withContext(context.IO) {
            val fields = mutableMapOf<String, String>()
            if (form.accepted) {
                fields["accept"] = form.accept
            } else {
                fields["decline"] = form.decline
            }
            fields["session"] = form.session
            fields["redirect"] = form.redirect
            try {
                val resp = api.areYouMuslim(fields)
                if (resp.isSuccessful) {
                    ResultWrapper.success(
                        parsePostForm(
                            resp.body()!!,
                            resp.raw().request.url.toString()
                        )
                    )
                } else {
                    ResultWrapper.failure(ErrorResponse.Network)
                }
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            }
        }

    override suspend fun modifyPost(form: ModifyForm): ResultWrapper<PostListDataPage, ErrorResponse> =
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
            val resp = api.modifyPost(
                body = builder.build(),
                referer = Values.URL + "/modifypost?redirect=" +
                        form.redirect + "&post=" + form.post.toInt()
            )
            try {
                if (resp.isSuccessful) {
                    val document = resp.body()!!
                    val url = resp.raw().request.url.toString()
                    return@withContext ResultWrapper.success(parseUnknownPostList(document, url))
                }
                return@withContext ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: ParseException) {
                return@withContext ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun newPost(form: NewPostForm): ResultWrapper<TopicPostListDataPage, ErrorResponse> =
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

            try {
                val response = api.newPost(
                    referer = referer,
                    body = builder.build()
                )
                if (response.isSuccessful) {
                    val url = response.raw().request.url.toString()
                    val document = response.body()!!
                    ResultWrapper.success(parseTopicPosts(document, url))
                } else {
                    ResultWrapper.failure(ErrorResponse.Network)
                }
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: Exception) {
                Timber.e(e)
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun newTopic(form: NewTopicForm): ResultWrapper<PostListDataPage, ErrorResponse> =
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
            val resp = api.newTopic(
                body = builder.build(),
                referer = Values.URL + "/newtopic?board=" + form.board
            )
            if (resp.isSuccessful) {
                try {
                    val soup = resp.body()!!
                    val url = resp.raw().request.url.toString()
                    ResultWrapper.success(parseTopicPosts(soup, url))
                } catch (e: ParseException) {
                    ResultWrapper.failure(ErrorResponse.Parse)
                }
            } else {
                ResultWrapper.failure(ErrorResponse.Network)
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
            ResultWrapper.success(Unit)
        } catch (e: IOException) {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }

    override suspend fun likePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.likeUrl!!)
            val resp = api.postAction(
                action = "do_likepost",
                postId = form.postId,
                session = form.session,
                redirect = form.redirect
            )
            parsePostListResp(resp)
        }

    override suspend fun unLikePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.likeUrl!!)
            val resp = api.postAction(
                action = "do_unlikepost",
                postId = form.postId,
                session = form.session,
                redirect = form.redirect
            )
            parsePostListResp(resp)
        }


    override suspend fun sharePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.shareUrl!!)
            parsePostListResp(
                api.postAction(
                    action = "do_share",
                    postId = form.postId,
                    session = form.session,
                    redirect = form.redirect
                )
            )
        }

    override suspend fun unSharePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseLikeShareUrl(post.shareUrl!!)
            parsePostListResp(
                api.postAction(
                    action = "do_unshare",
                    postId = form.postId,
                    session = form.session,
                    redirect = form.redirect
                )
            )
        }

    override suspend fun reportPost(form: ReportPostForm): ResultWrapper<PostListDataPage, ErrorResponse> =
        withContext(context.IO) {
            parsePostListResp(
                api.reportPost(
                    reason = form.reason,
                    postId = form.postId,
                    session = form.session,
                    redirect = form.redirect,
                    referer = form.referer
                )
            )
        }

    override suspend fun followTopic(topicPostListDataPage: TopicPostListDataPage): ResultWrapper<TopicPostListDataPage, ErrorResponse> =
        followTopic(topicPostListDataPage, true)

    override suspend fun unFollowTopic(topicPostListDataPage: TopicPostListDataPage): ResultWrapper<TopicPostListDataPage, ErrorResponse> =
        followTopic(topicPostListDataPage, false)

    override suspend fun unFollowTopic(topic: Topic): ResultWrapper<TopicListDataPage, ErrorResponse> =
        withContext(context.IO) {
            val form = parseFollowTopicUrl(topic.followOrUnFollowLink!!)
            val resp = api.topicAction(
                action = "do_unfollowtopic",
                topicId = form.topic,
                session = form.session,
                redirect = form.redirect
            )
            parseFollowedTopicListResp(resp)
        }

    override suspend fun followBoard(boardsDataPage: BoardsDataPage): ResultWrapper<BoardsDataPage, ErrorResponse> =
        followBoard(boardsDataPage, true)


    override suspend fun unFollowBoard(boardsDataPage: BoardsDataPage): ResultWrapper<BoardsDataPage, ErrorResponse> =
        followBoard(boardsDataPage, false)

    override suspend fun followBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): ResultWrapper<FollowedBoardsDataPage, ErrorResponse> =
        followBoard(followedBoardsDataPage, board, true)

    override suspend fun unFollowBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): ResultWrapper<FollowedBoardsDataPage, ErrorResponse> =
        followBoard(followedBoardsDataPage, board, false)

    override suspend fun likeProfilePhoto(
        like: Boolean,
        user: User
    ): ResultWrapper<User.Data, ErrorResponse> = withContext(context.IO) {
        val url = user.data?.image?.likeUrl!!
        api.visit(url).map(
            {
                try {
                    ResultWrapper.success(fetchUserData(it.body))
                } catch (e: ParseException) {

                    ResultWrapper.failure(ErrorResponse.Parse)
                }
            },
            {
                ResultWrapper.failure(ErrorResponse.Network)
            })
    }

    override suspend fun newMail(userForm: MailUserForm): ResultWrapper<User.Data, ErrorResponse> =
        withContext(context.IO) {
            try {
                val result = api.newMail(
                    recipientName = userForm.recipientName,
                    body = userForm.body,
                    subject = userForm.subject,
                    session = userForm.session
                )
                val body = result.body()
                if (body != null) {
                    ResultWrapper.success(fetchUserData(body))
                } else {
                    ResultWrapper.failure(ErrorResponse.Network)
                }
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun newMail(form: MailSuperModsForm): ResultWrapper<Unit, ErrorResponse> =
        withContext(context.IO) {
            try {
                val result =
                    api.newMail(session = form.session, subject = form.subject, body = form.body)
                val body = result.body()
                if (body != null) {
                    // ignore html content for now
                    ResultWrapper.success(Unit)
                } else {
                    ResultWrapper.failure(ErrorResponse.Network)
                }
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun newMail(form: MailBoardModsForm): ResultWrapper<BoardsDataPage, ErrorResponse> =
        withContext(context.IO) {
            val result = api.newMail(
                session = form.session,
                subject = form.subject,
                body = form.body,
                board = form.boardNo
            )
            parseBoardResp(result)
        }

    override suspend fun followUser(
        user: User,
        follow: Boolean
    ): ResultWrapper<User.Data, ErrorResponse> = withContext(context.IO) {
        val url = user.data?.followUserUrl
        checkNotNull(url) { "Follow User Url must not be null" }
        val response = miscApi.visitPage(url)
        when (response.isSuccessful) {
            true -> {
                val body = response.body()!!
                val data = fetchUserData(body)
                insertIfNotExists(user, body)
                ResultWrapper.success(data)
            }
            false -> {
                ResultWrapper.failure(ErrorResponse.Network)
            }
        }
    }

    override suspend fun editProfile(form: EditProfileForm): ResultWrapper<User.Data, ErrorResponse> =
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
            try {
                ResultWrapper.success(fetchUserData(api.editProfile(builder.build())))
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            }
        }

    override suspend fun dismissMailNotification(form: DismissMailNotificationForm): ResultWrapper<Unit, ErrorResponse> =
        withContext(context.IO) {
            api.dismissMailNotification(
                session = form.session,
                redirect = form.redirect,
                senders = form.senders.map { it.slug }
            ).onSuccess { Unit }
                .convert()
        }

    private suspend fun followTopic(topicPostListDataPage: TopicPostListDataPage, follow: Boolean) =
        withContext(context.IO) {
            val form = parseFollowTopicUrl(topicPostListDataPage.followOrUnFollowTopicUrl!!)
            val resp = api.topicAction(
                action = if (follow) "do_followtopic" else "do_unfollowtopic",
                topicId = form.topic,
                session = form.session,
                redirect = form.redirect
            )
            parseTopicResp(resp)
        }

    private suspend fun followBoard(boardsDataPage: BoardsDataPage, follow: Boolean) =
        withContext(context.IO) {
            val form = parseFollowBoardUrl(boardsDataPage.followOrUnFollowUrl!!)
            val resp = api.boardAction(
                action = if (follow) "do_followboard" else "do_unfollowboard",
                boardNo = form.board,
                session = form.session,
                redirect = form.redirect
            )
            parseBoardResp(resp)
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
            val resp = api.boardAction(
                action = if (follow) "do_followboard" else "do_unfollowboard",
                boardNo = form.board,
                session = form.session,
                redirect = form.redirect
            )
            parseFollowedBoardResp(resp)
        }

    private fun parsePostListResp(resp: Response<Document>): ResultWrapper<PostListDataPage, ErrorResponse> {
        return if (resp.isSuccessful) {
            val soup = resp.body()!!
            val url = resp.raw().request.url.toString()
            try {
                ResultWrapper.success(parseUnknownPostList(soup, url))
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        } else {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }

    private fun parseTopicResp(resp: Response<Document>): ResultWrapper<TopicPostListDataPage, ErrorResponse> {
        return if (resp.isSuccessful) {
            val soup = resp.body()!!
            val url = resp.raw().request.url.toString()
            try {
                ResultWrapper.success(parseTopicPosts(soup, url))
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        } else {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }

    private fun parseBoardResp(resp: Response<Document>): ResultWrapper<BoardsDataPage, ErrorResponse> {
        return if (resp.isSuccessful) {
            val soup = resp.body()!!
            val url = resp.raw().request.url.toString()
            try {
                ResultWrapper.success(parseBoardTopics(soup, url))
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        } else {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }

    private fun parseFollowedTopicListResp(resp: Response<Document>): ResultWrapper<TopicListDataPage, ErrorResponse> {
        return if (resp.isSuccessful) {
            val soup = resp.body()!!
            try {
                ResultWrapper.success(parseFollowedTopics(soup))
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        } else {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }

    private fun parseFollowedBoardResp(resp: Response<Document>): ResultWrapper<FollowedBoardsDataPage, ErrorResponse> {
        return if (resp.isSuccessful) {
            val soup = resp.body()!!
            val url = resp.raw().request.url.toString()
            try {
                ResultWrapper.success(parseFollowedBoards(soup, url))
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        } else {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }

    private fun insertIfNotExists(user: User, soup: Document) {
        db.transaction {
            val key = user.name.toLowerCase(Locale.ROOT)
            if (db.userDataQueries.find(key).executeAsOneOrNull() == null) {
                db.userDataQueries.insert(
                    userSlug = key,
                    data = soup.outerHtml()
                )
            } else {
                db.userDataQueries.update(
                    userSlug = key,
                    data = soup.outerHtml()
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
