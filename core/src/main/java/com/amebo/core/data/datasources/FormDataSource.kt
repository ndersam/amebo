package com.amebo.core.data.datasources

import com.amebo.core.common.Either
import com.amebo.core.domain.*
import com.github.michaelbull.result.Result

interface FormDataSource {
    suspend fun newPost(topicId: String): Result<Either<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun getQuotablePostContent(quotablePost: QuotablePost): Result<String, ErrorResponse>
    suspend fun newTopic(boardId: Int): Result<Either<NewTopicForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun modifyPost(post: SimplePost): Result<Either<ModifyForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun quotePost(post: SimplePost): Result<Either<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun reportPost(post: SimplePost): Result<ReportPostForm, ErrorResponse>
    suspend fun mailUser(user: User): Result<MailUserForm, ErrorResponse>
    suspend fun mailSuperMods(): Result<MailSuperModsForm, ErrorResponse>
    suspend fun mailBoardMods(boardId: Int): Result<MailBoardModsForm, ErrorResponse>
    suspend fun editProfile(): Result<EditProfileForm, ErrorResponse>
}