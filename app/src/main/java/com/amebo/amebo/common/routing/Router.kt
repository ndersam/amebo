package com.amebo.amebo.common.routing

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.amebo.core.domain.*

interface Router {

    fun initialize(savedInstanceState: Bundle?)

    fun addOnTabSelectedListener(listener: (TabItem) -> Unit)

    fun addOnFragmentChanged(listener: (Boolean) -> Unit)

    fun toPhotoViewer(
        images: List<String>,
        position: Int,
        imageView: View,
        transitionName: String
    )

    fun toPhotoViewer(
        image: String,
        imageView: View,
        transitionName: String
    ) = toPhotoViewer(listOf(image), 0, imageView, transitionName)

    fun toPhotoViewerUri(
        images: List<Uri>,
        position: Int,
        imageView: View,
        transitionName: String
    )

    fun toPhotoViewerUri(
        image: Uri,
        imageView: View,
        transitionName: String
    ) = toPhotoViewerUri(listOf(image), 0, imageView, transitionName)

    fun toPhotoViewer(
        user: User,
        url: String,
        imageView: View,
        transitionName: String
    )

    fun toMuslimDeclaration(form: AreYouMuslimDeclarationForm)
    fun toEditProfile()

    fun toMentions(page: Int? = null)
    fun toRecentPosts(page: Int? = null)
    fun toLikesAndShares(page: Int? = null)
    fun toPeopleYouAreFollowingPosts(page: Int? = null)
    fun toSharedPosts(page: Int? = null)
    fun toMyLikes(page: Int?)
    fun toMySharedPosts(page: Int?)
    fun toProfile()
    fun toPostList(postList: PostList, page: Int? = null)
    fun toTopicList(topicList: TopicList, page: Int? = null)
    fun toTopicList(topics: List<Topic>, title: String)
    fun toSettings()
    fun toTopic(
        topic: Topic,
        page: Int? = null,
        replace: Boolean = false,
        data: TopicPostListDataPage? = null
    )

    fun toExplore()
    fun toSearch(sharedView: View? = null, query: SearchQuery? = null)
    fun toUser(user: User, preview: Boolean = false, dismiss: Boolean = true)
    fun toAccountList()
    fun toNewTopic(board: Board? = null)
    fun toUserList(users: List<User>, title: String)
    fun toBoard(board: Board)
    fun toTopicListPageSelection(
        topicList: TopicList,
        baseTopicListDataPage: BaseTopicListDataPage
    )

    fun toPostListPageOptions(currentPage: Int, lastPage: Int)
    fun toNewPost(topic: Topic)
    fun toReplyPost(post: SimplePost)
    fun toReportPost(post: SimplePost)
    fun toModifyPost(post: SimplePost)
    fun toYoutubeScreen(videoId: String)
    fun toSearchResults(query: SearchQuery, page: Int? = null)
    fun toExploreSearchOptions(onlyTopics: Boolean, onlyImages: Boolean, selectedBoard: Board?)
    fun toPostingRules()
    fun toImagePicker()
    fun back(): Boolean
    fun toTabItem(destination: TabItem)
    fun toAccountDeletion(userAccount: RealUserAccount)
    fun toSignIn(userName: String? = null)

    fun toPostEditorSettings()
    fun toPostPreview(html: String)

    fun setOnDialogDismissListener(viewLifecycleOwner: LifecycleOwner, listener: (numDialogsDisplayed: Int) -> Unit)

    fun toMailUser(user: User)
    fun toMailSuperMods()
    fun toMailBoardMods(boardId: Int, board: Board)


    fun toSelectBoardDialog(selectedBoard: Board? = null, showAllBoard: Boolean = true)
    fun toGenderPicker(gender: Gender? = null)
    fun toDatePicker(date: BirthDate?, minYear: Year)

    fun toUserPosts(user: User)
    fun toUserTopics(user: User)
    fun toGotoUserProfile()
    fun onSaveInstanceState(outState: Bundle?)
    fun toPost(postId: String)
    fun toTopicHistory()

    fun toPostPicker(posts: List<QuotablePost>)
}