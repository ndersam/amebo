package com.amebo.core.data.datasources

import com.amebo.core.domain.*

interface FormSubmissionDataSource {
    suspend fun areYouMuslim(form: AreYouMuslimDeclarationForm): ResultWrapper<Form?, ErrorResponse>
    suspend fun modifyPost(form: ModifyForm): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun newPost(form: NewPostForm): ResultWrapper<TopicPostListDataPage, ErrorResponse>
    suspend fun newTopic(form: NewTopicForm): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun removeAttachments(attachments: List<Attachment>)
    suspend fun removeAttachment(attachment: Attachment): ResultWrapper<Unit, ErrorResponse>
    suspend fun likePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun sharePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun unLikePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun unSharePost(post: SimplePost): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun reportPost(form: ReportPostForm): ResultWrapper<PostListDataPage, ErrorResponse>
    suspend fun followTopic(topicPostListDataPage: TopicPostListDataPage): ResultWrapper<TopicPostListDataPage, ErrorResponse>
    suspend fun unFollowTopic(topicPostListDataPage: TopicPostListDataPage): ResultWrapper<TopicPostListDataPage, ErrorResponse>
    suspend fun unFollowTopic(topic: Topic): ResultWrapper<TopicListDataPage, ErrorResponse>
    suspend fun followBoard(boardsDataPage: BoardsDataPage): ResultWrapper<BoardsDataPage, ErrorResponse>
    suspend fun unFollowBoard(boardsDataPage: BoardsDataPage): ResultWrapper<BoardsDataPage, ErrorResponse>
    suspend fun followBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): ResultWrapper<FollowedBoardsDataPage, ErrorResponse>

    suspend fun unFollowBoard(
        followedBoardsDataPage: FollowedBoardsDataPage,
        board: Board
    ): ResultWrapper<FollowedBoardsDataPage, ErrorResponse>

    suspend fun likeProfilePhoto(like: Boolean, user: User): ResultWrapper<User.Data, ErrorResponse>
    suspend fun newMail(userForm: MailUserForm): ResultWrapper<User.Data, ErrorResponse>
    suspend fun newMail(form: MailSuperModsForm): ResultWrapper<Unit, ErrorResponse>
    suspend fun newMail(form: MailBoardModsForm): ResultWrapper<BoardsDataPage, ErrorResponse>
    suspend fun followUser(user: User, follow: Boolean): ResultWrapper<User.Data, ErrorResponse>
    suspend fun editProfile(form: EditProfileForm): ResultWrapper<User.Data, ErrorResponse>
    suspend fun dismissMailNotification(form: DismissMailNotificationForm): ResultWrapper<Unit, ErrorResponse>
}