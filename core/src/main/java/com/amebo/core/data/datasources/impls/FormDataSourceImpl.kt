package com.amebo.core.data.datasources.impls

import android.net.ParseException
import android.net.Uri
import com.amebo.core.Values
import com.amebo.core.apis.FormApi
import com.amebo.core.crawler.TopicLockedException
import com.amebo.core.crawler.form.*
import com.amebo.core.crawler.isHomeUrl
import com.amebo.core.crawler.topicList.parseTopicUrl
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.FormDataSource
import com.amebo.core.domain.*
import com.amebo.core.extensions.map
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import retrofit2.Response
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject


class FormDataSourceImpl @Inject constructor(
    private val api: FormApi,
    private val context: CoroutineContextProvider
) : FormDataSource {

    private val log get() = FirebaseCrashlytics.getInstance()::log

    override suspend fun newPost(topicId: String): ResultWrapper<ResultWrapper<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            try {
                val resp = api.newPost(topicId)
                checkIfMuslim(resp, ::parseNewPost)
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: TopicLockedException) {
                ResultWrapper.failure(ErrorResponse.TopicLocked)
            } catch (e: Exception) {
                log(e.stackTrace.joinToString { "\n" })
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun getQuotablePostContent(quotablePost: QuotablePost): ResultWrapper<String, ErrorResponse> =
        withContext(context.IO) {
            val parseResult = parseTopicUrl(quotablePost.url)!!
            try {
                val content = api.getPost(
                    postID = quotablePost.id, session = quotablePost.session,
                    referer = Values.URL + "/newpost?topic=${parseResult.topicId}&post=${quotablePost.id}"
                )
                ResultWrapper.Success(content)
            } catch (e: IOException) {
                ResultWrapper.Failure(ErrorResponse.Network)
            }
        }

    override suspend fun modifyPost(post: SimplePost): ResultWrapper<ResultWrapper<ModifyForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            try {
                val uri = Uri.parse(post.editUrl)
                val redirect = uri.getQueryParameter("redirect")!!
                val postId = uri.getQueryParameter("post")!!
                val resp = api.modifyPost(redirect, postId)
                checkIfMuslim(resp, ::parseModifyPost)
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: TopicLockedException) {
                ResultWrapper.failure(ErrorResponse.TopicLocked)
            } catch (e: Exception) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun newTopic(boardId: Int): ResultWrapper<ResultWrapper<NewTopicForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            try {
                val resp = api.newTopic(boardId)
                checkIfMuslim(resp, ::parseNewTopic)
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: Exception) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun quotePost(post: SimplePost): ResultWrapper<ResultWrapper<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            try {
                val resp = api.quote(post.topic.id.toString(), post.id)
                checkIfMuslim(resp, ::parseNewPost)
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: TopicLockedException) {
                ResultWrapper.failure(ErrorResponse.TopicLocked)
            } catch (e: Exception) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun reportPost(post: SimplePost): ResultWrapper<ReportPostForm, ErrorResponse> {
        val uri = Uri.parse(post.reportUrl)
        val redirect = uri.getQueryParameter("redirect")!!
        val postId = uri.getQueryParameter("post")!!
        val session = uri.getQueryParameter("session")!!
        // e.g. https://www.nairaland.com/makereport?post=95429345&redirect=%2F6211922%2Fbenue-covid-19-palliatives-sold-kano%2395429345&session=8B25B584471D09DE788E33B4B917E2B43705493284DA6B5496A0DD5A3B9278E5
        val form = ReportPostForm(
            postId,
            session,
            redirect,
            referer = "${Values.URL}/makereport?post=$postId&redirect=${
                URLEncoder.encode(
                    redirect,
                    "UTF-8"
                )
            }&session=${session}"
        )
        return ResultWrapper.success(form)
    }


    override suspend fun mailUser(user: User): ResultWrapper<MailUserForm, ErrorResponse> =
        withContext(context.IO) {
            try {
                api.mailUser(user.slug).map({
                    parseMailToUserForm(it.body)
                }, {
                    ResultWrapper.failure(ErrorResponse.Network)
                })
            } catch (e: Exception) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun mailSuperMods(): ResultWrapper<MailSuperModsForm, ErrorResponse> =
        withContext(context.IO) {
            try {
                api.mailSuperMods().map({
                    parseMailSuperModsForm(it.body)
                }, {
                    ResultWrapper.failure(ErrorResponse.Network)
                })
            } catch (e: Exception) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun mailBoardMods(boardId: Int): ResultWrapper<MailBoardModsForm, ErrorResponse> =
        withContext(context.IO) {
            try {
                api.mailBoardMods(boardId).map({
                    parseMailBoardModsForm(it.body)
                }, {
                    ResultWrapper.failure(ErrorResponse.Network)
                })
            } catch (e: Exception) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    override suspend fun editProfile(): ResultWrapper<EditProfileForm, ErrorResponse> =
        withContext(context.IO) {
            try {
                ResultWrapper.success(parseEditProfile(api.editProfile()))
            } catch (e: IOException) {
                ResultWrapper.failure(ErrorResponse.Network)
            } catch (e: ParseException) {
                ResultWrapper.failure(ErrorResponse.Parse)
            }
        }

    private fun <T : Form> checkIfMuslim(
        resp: Response<Document>,
        parser: (Document) -> ResultWrapper<T, AreYouMuslimDeclarationForm>
    ): ResultWrapper<ResultWrapper<T, AreYouMuslimDeclarationForm>, ErrorResponse> {
        return if (resp.isSuccessful) {
            val soup = resp.body()!!
            // if redirect to home ==> user declared as muslim
            if (isHomeUrl(resp)) {
                ResultWrapper.failure(ErrorResponse.Muslim)
            } else {
                ResultWrapper.success(parser(soup))
            }
        } else {
            ResultWrapper.failure(ErrorResponse.Network)
        }
    }
}