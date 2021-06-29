package com.amebo.core.data.datasources.impls

import android.net.Uri
import com.amebo.core.apis.FormApi
import com.amebo.core.common.Either
import com.amebo.core.common.Values
import com.amebo.core.common.extensions.RawResponse
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.common.extensions.awaitResultResponse
import com.amebo.core.crawler.form.*
import com.amebo.core.crawler.isHomeUrl
import com.amebo.core.crawler.topicList.parseTopicUrl
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.FormDataSource
import com.amebo.core.domain.*
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import java.net.URLEncoder
import javax.inject.Inject

internal class FormDataSourceImpl @Inject constructor(
    private val api: FormApi,
    private val context: CoroutineContextProvider
) : FormDataSource {


    override suspend fun newPost(topicId: String): Result<Either<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            api.newPost(topicId)
                .awaitResultResponse { resp, soup ->
                    checkIfMuslim(resp, soup, ::parseNewPost)
                }
        }

    override suspend fun getQuotablePostContent(quotablePost: QuotablePost): Result<String, ErrorResponse> =
        withContext(context.IO) {
            val parseResult = parseTopicUrl(quotablePost.url)!!
            api.getPost(
                postID = quotablePost.id, session = quotablePost.session,
                referer = Values.URL + "/newpost?topic=${parseResult.topicId}&post=${quotablePost.id}"
            ).awaitResult {
                Ok(it)
            }
        }

    override suspend fun modifyPost(post: SimplePost): Result<Either<ModifyForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            val uri = Uri.parse(post.editUrl)
            val redirect = uri.getQueryParameter("redirect")!!
            val postId = uri.getQueryParameter("post")!!
            api.modifyPost(redirect, postId)
                .awaitResultResponse { resp, soup ->
                    checkIfMuslim(resp, soup, ::parseModifyPost)
                }
        }

    override suspend fun newTopic(boardId: Int): Result<Either<NewTopicForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            api.newTopic(boardId)
                .awaitResultResponse { resp, result ->
                    checkIfMuslim(resp, result, ::parseNewTopic)
                }
        }

    override suspend fun quotePost(post: SimplePost): Result<Either<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse> =
        withContext(context.IO) {
            api.quote(post.topic.id.toString(), post.id)
                .awaitResultResponse { resp, soup ->
                    checkIfMuslim(resp, soup, ::parseNewPost)
                }
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun reportPost(post: SimplePost): Result<ReportPostForm, ErrorResponse> {
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
        return Ok(form)
    }


    override suspend fun mailUser(user: User): Result<MailUserForm, ErrorResponse> =
        withContext(context.IO) {
            api.mailUser(user.slug)
                .awaitResult {
                    parseMailToUserForm(it)
                }
        }

    override suspend fun mailSuperMods(): Result<MailSuperModsForm, ErrorResponse> =
        withContext(context.IO) {
            api.mailSuperMods()
                .awaitResult { parseMailSuperModsForm(it) }
        }

    override suspend fun mailBoardMods(boardId: Int): Result<MailBoardModsForm, ErrorResponse> =
        withContext(context.IO) {
            api.mailBoardMods(boardId)
                .awaitResult { parseMailBoardModsForm(it) }
        }

    override suspend fun editProfile(): Result<EditProfileForm, ErrorResponse> =
        withContext(context.IO) {
            api.editProfile()
                .awaitResult { Ok(parseEditProfile(it)) }
        }

    private fun <T : Form> checkIfMuslim(
        resp: RawResponse,
        soup: Document,
        parser: (Document) -> Result<Either<T, AreYouMuslimDeclarationForm>, ErrorResponse>
    ): Result<Either<T, AreYouMuslimDeclarationForm>, ErrorResponse> {
        // if redirect to home ==> user declared as muslim
        return if (isHomeUrl(resp)) {
            Err(ErrorResponse.Muslim)
        } else {
            parser(soup)
        }
    }
}