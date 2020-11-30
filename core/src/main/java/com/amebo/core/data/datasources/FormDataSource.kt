package com.amebo.core.data.datasources

import com.amebo.core.domain.*

interface FormDataSource {
    suspend fun newPost(topicId: String): ResultWrapper<ResultWrapper<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun getQuotablePostContent(quotablePost: QuotablePost): ResultWrapper<String, ErrorResponse>
    suspend fun newTopic(boardId: Int): ResultWrapper<ResultWrapper<NewTopicForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun modifyPost(post: SimplePost): ResultWrapper<ResultWrapper<ModifyForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun quotePost(post: SimplePost): ResultWrapper<ResultWrapper<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse>
    suspend fun reportPost(post: SimplePost): ResultWrapper<ReportPostForm, ErrorResponse>
    suspend fun mailUser(user: User): ResultWrapper<MailUserForm, ErrorResponse>
    suspend fun mailSuperMods(): ResultWrapper<MailSuperModsForm, ErrorResponse>
    suspend fun mailBoardMods(boardId: Int): ResultWrapper<MailBoardModsForm, ErrorResponse>
    suspend fun editProfile(): ResultWrapper<EditProfileForm, ErrorResponse>
}