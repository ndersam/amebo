package com.amebo.core.data.datasources

import com.amebo.core.domain.*
import com.github.michaelbull.result.Result

interface FormSubmissionDataSource {
    suspend fun areYouMuslim(form: AreYouMuslimDeclarationForm): Result<Form?, ErrorResponse>
    suspend fun modifyPost(form: ModifyForm): Result<PostListDataPage, ErrorResponse>
    suspend fun newPost(form: NewPostForm): Result<TopicPostListDataPage, ErrorResponse>
    suspend fun newTopic(form: NewTopicForm): Result<PostListDataPage, ErrorResponse>
    suspend fun removeAttachments(attachments: List<Attachment>)
    suspend fun removeAttachment(attachment: Attachment): Result<Unit, ErrorResponse>
    suspend fun likePost(post: SimplePost): Result<PostListDataPage, ErrorResponse>
    suspend fun sharePost(post: SimplePost): Result<PostListDataPage, ErrorResponse>
    suspend fun unLikePost(post: SimplePost): Result<PostListDataPage, ErrorResponse>
    suspend fun unSharePost(post: SimplePost): Result<PostListDataPage, ErrorResponse>
    suspend fun reportPost(form: ReportPostForm): Result<PostListDataPage, ErrorResponse>
    suspend fun followTopic(topicPostListDataPage: TopicPostListDataPage): Result<TopicPostListDataPage, ErrorResponse>
    suspend fun unFollowTopic(topicPostListDataPage: TopicPostListDataPage): Result<TopicPostListDataPage, ErrorResponse>
    suspend fun unFollowTopic(topic: Topic): Result<TopicListDataPage, ErrorResponse>
    suspend fun followBoard(boardsDataPage: BoardsDataPage): Result<BoardsDataPage, ErrorResponse>
    suspend fun unFollowBoard(boardsDataPage: BoardsDataPage): Result<BoardsDataPage, ErrorResponse>
    suspend fun followBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): Result<FollowedBoardsDataPage, ErrorResponse>

    suspend fun unFollowBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): Result<FollowedBoardsDataPage, ErrorResponse>

    suspend fun likeProfilePhoto(like: Boolean, user: User): Result<User.Data, ErrorResponse>
    suspend fun newMail(userForm: MailUserForm): Result<User.Data, ErrorResponse>
    suspend fun newMail(form: MailSuperModsForm): Result<Unit, ErrorResponse>
    suspend fun newMail(form: MailBoardModsForm): Result<BoardsDataPage, ErrorResponse>
    suspend fun followUser(user: User, follow: Boolean): Result<User.Data, ErrorResponse>
    suspend fun editProfile(form: EditProfileForm): Result<User.Data, ErrorResponse>
    suspend fun dismissMailNotification(form: DismissMailNotificationForm): Result<Unit, ErrorResponse>
}