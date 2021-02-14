package com.amebo.amebo.common.routing.fragnav

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.fragments.BackPressable
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.TabItem
import com.amebo.amebo.screens.accounts.AccountDeletionScreen
import com.amebo.amebo.screens.accounts.AccountListScreen
import com.amebo.amebo.screens.accounts.edit.DatePickerScreen
import com.amebo.amebo.screens.accounts.edit.EditAccountScreen
import com.amebo.amebo.screens.accounts.edit.GenderPickerScreen
import com.amebo.amebo.screens.editactions.EditActionsPickerScreen
import com.amebo.amebo.screens.explore.ExploreScreen
import com.amebo.amebo.screens.explore.SearchOptionsDialog
import com.amebo.amebo.screens.explore.SelectBoardDialog
import com.amebo.amebo.screens.imagepicker.ImagePickerScreen
import com.amebo.amebo.screens.mail.inbox.InboxScreen
import com.amebo.amebo.screens.mail.mods.MailBoardModsScreen
import com.amebo.amebo.screens.mail.supermods.MailSuperModsScreen
import com.amebo.amebo.screens.mail.user.MailUserScreen
import com.amebo.amebo.screens.newpost.modifypost.ModifyPostScreen
import com.amebo.amebo.screens.newpost.muslim.MuslimDeclarationScreen
import com.amebo.amebo.screens.newpost.newpost.NewPostScreen
import com.amebo.amebo.screens.newpost.newtopic.NewTopicScreen
import com.amebo.amebo.screens.newpost.quotepost.PostPickerScreen
import com.amebo.amebo.screens.photoviewer.BasePhotoViewerScreen
import com.amebo.amebo.screens.photoviewer.posts.PhotoViewerScreen
import com.amebo.amebo.screens.photoviewer.user.UserPhotoViewerScreen
import com.amebo.amebo.screens.postlist.PostListPageNavigationDialog
import com.amebo.amebo.screens.postlist.TimelinePostListScreen.Companion.rootScreen
import com.amebo.amebo.screens.postlist.following.PostsFromFollowingsScreen
import com.amebo.amebo.screens.postlist.likesandshares.LikesAndSharesScreen
import com.amebo.amebo.screens.postlist.mentions.MentionsScreen
import com.amebo.amebo.screens.postlist.mylikes.MyLikesScreen
import com.amebo.amebo.screens.postlist.myshares.MySharedPostsScreen
import com.amebo.amebo.screens.postlist.recentposts.RecentPostsScreen
import com.amebo.amebo.screens.postlist.sharedwithme.SharedWithMeScreen
import com.amebo.amebo.screens.postlist.topic.TopicScreen
import com.amebo.amebo.screens.postlist.topicloader.TopicLoaderScreen
import com.amebo.amebo.screens.postlist.userposts.UserPostsScreen
import com.amebo.amebo.screens.preview.PreviewPostScreen
import com.amebo.amebo.screens.reportpost.ReportPostScreen
import com.amebo.amebo.screens.rules.RulesScreen
import com.amebo.amebo.screens.search.SearchResultScreen
import com.amebo.amebo.screens.search.SearchScreen
import com.amebo.amebo.screens.settings.SettingsScreen
import com.amebo.amebo.screens.signin.SignInScreen
import com.amebo.amebo.screens.topiclist.BaseTopicListScreen
import com.amebo.amebo.screens.topiclist.TopicListPageSelectionDialog
import com.amebo.amebo.screens.topiclist.history.ViewedTopicListScreen
import com.amebo.amebo.screens.topiclist.home.HomeScreen
import com.amebo.amebo.screens.topiclist.main.TopicListScreen
import com.amebo.amebo.screens.topiclist.simple.SimpleTopicListScreen
import com.amebo.amebo.screens.topiclist.user.UserTopicsScreen
import com.amebo.amebo.screens.user.ProfileScreen
import com.amebo.amebo.screens.user.ProfileScreen.Companion.rootScreen
import com.amebo.amebo.screens.user.UserScreen
import com.amebo.amebo.screens.user.preview.UserPreviewScreen
import com.amebo.amebo.screens.user.search.GoToUserScreen
import com.amebo.amebo.screens.userlist.UserListScreen
import com.amebo.amebo.screens.userlist.followers.MyFollowersScreen
import com.amebo.amebo.screens.youtube.YoutubeScreen
import com.amebo.core.domain.*
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UniqueTabHistoryStrategy
import timber.log.Timber
import java.lang.ref.WeakReference


class FragNavRouter(
    private val fragmentManager: FragmentManager,
    private val controller: FragNavController
) : Router {

    private val tabSelectedListeners = mutableListOf<(TabItem) -> Unit>()
    private val transactionListener = mutableListOf<(Boolean) -> Unit>()

    private var currentDialogFrag: WeakReference<DialogFragment>? = null

    private val dialogDismissEvent = MutableLiveData<Event<Int>>()
    private var currentDismissEventListener: WeakReference<Fragment>? = null

    private val initialIndex = INDEX_TOPICS

    private val frags
        get() = listOf(
            HomeScreen(),
            RecentPostsScreen().rootScreen(),
            // for authenticated users
            ProfileScreen().rootScreen(),
            InboxScreen(),
            MentionsScreen().rootScreen(),
            LikesAndSharesScreen().rootScreen(),
            SharedWithMeScreen().rootScreen(),
            PostsFromFollowingsScreen().rootScreen(),
            MyLikesScreen().rootScreen(),
            MySharedPostsScreen().rootScreen(),
            MyFollowersScreen(),
        )

    private val dialogFragmentCount: Int
        get() = getFragmentManagerForDialog().fragments.count {
            it is DialogFragment
        }


    init {
        controller.createEager = false
        controller.fragmentHideStrategy = FragNavController.DETACH
        controller.defaultTransactionOptions = FragNavTransactionOptions.newBuilder()
            .allowStateLoss(false).build()
        controller.navigationStrategy =
            UniqueTabHistoryStrategy(object : FragNavSwitchController {
                override fun switchTab(
                    index: Int,
                    transactionOptions: FragNavTransactionOptions?
                ) {
                    notifyTabSelectionListeners(index)
                }
            })

        controller.transactionListener = object : FragNavController.TransactionListener {
            override fun onFragmentTransaction(
                fragment: Fragment?,
                transactionType: FragNavController.TransactionType
            ) {
                notifyTransactionListeners()
            }

            override fun onTabTransaction(fragment: Fragment?, index: Int) {
                notifyTransactionListeners()
            }
        }

        getFragmentManagerForDialog().registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentViewDestroyed(fm, f)
                if (f is DialogFragment && controller.currentFrag == currentDismissEventListener?.get()) {
                    dialogDismissEvent.value = Event(dialogFragmentCount)
                }
            }
        }, false)
    }

    override fun initialize(savedInstanceState: Bundle?) {
        controller.rootFragments = frags
        controller.initialize(initialIndex, savedInstanceState)

        notifyTransactionListeners()
        notifyTabSelectionListeners(controller.currentStackIndex)
    }

    override fun addOnTabSelectedListener(listener: (TabItem) -> Unit) {
        tabSelectedListeners.add(listener)
    }

    override fun addOnFragmentChanged(listener: (Boolean) -> Unit) {
        transactionListener.add(listener)
    }

    private fun notifyTransactionListeners() {
        transactionListener.forEach { it(controller.isRootFragment) }
    }

    private fun notifyTabSelectionListeners(index: Int) {
        tabSelectedListeners.forEach { it(positionToTabItem(index)) }
    }

    override fun toPhotoViewer(
        images: List<String>,
        position: Int,
        imageView: View,
        transitionName: String
    ) {
        val options = FragNavTransactionOptions.newBuilder()
            .addSharedElement(imageView to transitionName)
            .build()
        push(
            PhotoViewerScreen(),
            BasePhotoViewerScreen.newBundle(
                images,
                transitionName,
                position
            ),
            options
        )
    }

    override fun toPhotoViewer(user: User, url: String, imageView: View, transitionName: String) {
        val options = FragNavTransactionOptions.newBuilder()
            .addSharedElement(imageView to transitionName)
            .build()
        push(
            UserPhotoViewerScreen(),
            UserPhotoViewerScreen.newBundle(
                user,
                url,
                transitionName
            ),
            options
        )
    }

    override fun toPhotoViewerUri(
        images: List<Uri>,
        position: Int,
        imageView: View,
        transitionName: String
    ) {
        val options = FragNavTransactionOptions.newBuilder()
            .addSharedElement(imageView to transitionName)
            .build()
        push(
            PhotoViewerScreen(),
            BasePhotoViewerScreen.newBundleForUris(
                images,
                transitionName,
                position
            ),
            options
        )
    }

    override fun toMuslimDeclaration(form: AreYouMuslimDeclarationForm) {
        push(
            MuslimDeclarationScreen(),
            MuslimDeclarationScreen.newBundle(
                form
            )
        )
    }

    override fun toEditProfile() {
        push(EditAccountScreen())
    }

    override fun toMentions(page: Int?) {
        // TODO: Page numbers
        push(MentionsScreen())
    }

    override fun toRecentPosts(page: Int?) {
        push(RecentPostsScreen())
    }

    override fun toLikesAndShares(page: Int?) {
        push(LikesAndSharesScreen())
    }

    override fun toPeopleYouAreFollowingPosts(page: Int?) {
        push(PostsFromFollowingsScreen())
    }

    override fun toSharedPosts(page: Int?) {
        push(SharedWithMeScreen())
    }

    override fun toMyLikes(page: Int?) {
        push(MyLikesScreen())
    }

    override fun toMySharedPosts(page: Int?) {
        push(MySharedPostsScreen())
    }

    override fun toProfile() {
        push(ProfileScreen())
    }

    override fun toPostList(postList: PostList, page: Int?) {
        when (postList) {
            is Topic -> toTopic(postList, page)
            is Mentions -> toMentions(page)
            is LikesAndShares -> toLikesAndShares(page)
            is SearchQuery -> toSearchResults(postList, page)
            is RecentPosts -> toRecentPosts(page)
            is PostsByPeopleYouAreFollowing -> toPeopleYouAreFollowingPosts(page)
            is UserPosts -> toUserPosts(postList.user)
            is SharedPosts -> toSharedPosts(page)
            MyLikes -> toMyLikes(page)
            MySharedPosts -> toMySharedPosts(page)
        }
    }

    override fun toTopicList(topicList: TopicList, page: Int?) {
        push(TopicListScreen(), BaseTopicListScreen.newBundle(topicList))
    }

    override fun toTopicList(topics: List<Topic>, title: String) {
        push(
            SimpleTopicListScreen(),
            SimpleTopicListScreen.newBundle(
                topics,
                title
            )
        )
    }

    override fun toSettings() {
        push(SettingsScreen())
    }

    override fun toTopic(topic: Topic, page: Int?, replace: Boolean, data: TopicPostListDataPage?) {
        if (replace.not()) {
            push(
                TopicScreen(),
                TopicScreen.bundle(
                    topic = topic,
                    page = page ?: 0
                )
            )
        } else {
            replace(
                TopicScreen(),
                TopicScreen.bundle(
                    topic = topic,
                    page = page ?: 0,
                    data = data
                )
            )
        }
    }

    override fun toExplore() {
        show(ExploreScreen())
    }

    override fun toSearch(sharedView: View?, query: SearchQuery?) {
        val bundle = if (query == null) null else SearchScreen.newBundle(query)
        if (sharedView == null) {
            push(SearchScreen(), bundle)
        } else {
            val options = FragNavTransactionOptions.newBuilder()
                .addSharedElement(sharedView to ViewCompat.getTransitionName(sharedView)!!)
                .build()

            push(SearchScreen(), bundle, options)
        }
    }

    override fun toUser(user: User, preview: Boolean, dismiss: Boolean) {
        if (preview) {
            show(UserPreviewScreen(), UserPreviewScreen.newBundle(user))
        } else {
            push(UserScreen(), UserScreen.newBundle(user))
        }
    }

    override fun toAccountList() {
        show(AccountListScreen())
    }

    override fun toNewTopic(board: Board?) {
        val bundle = if (board == null) null else NewTopicScreen.newBundle(board)
        push(NewTopicScreen(), bundle)
    }

    override fun toUserList(users: List<User>, title: String) {
        push(UserListScreen(), UserListScreen.newBundle(users, title))
    }

    override fun toBoard(board: Board) {
        push(TopicListScreen(), BaseTopicListScreen.newBundle(board))
    }

    override fun toTopicListPageSelection(
        topicList: TopicList,
        baseTopicListDataPage: BaseTopicListDataPage
    ) {
        show(
            TopicListPageSelectionDialog(), TopicListPageSelectionDialog.newBundle(
                topicList,
                baseTopicListDataPage,
                baseTopicListDataPage.page,
                baseTopicListDataPage.last
            )
        )
    }

    override fun toPostListPageOptions(currentPage: Int, lastPage: Int) {
        show(
            PostListPageNavigationDialog(),
            PostListPageNavigationDialog.newBundle(currentPage, lastPage)
        )
    }

    override fun toNewPost(topic: Topic) {
        push(NewPostScreen(), NewPostScreen.newBundle(topic))
    }

    override fun toReplyPost(post: SimplePost) {
        push(NewPostScreen(), NewPostScreen.newBundle(post))
    }

    override fun toReportPost(post: SimplePost) {
        push(ReportPostScreen(), ReportPostScreen.newBundle(post))
    }

    override fun toModifyPost(post: SimplePost) {
        push(ModifyPostScreen(), ModifyPostScreen.newBundle(post))
    }

    override fun toYoutubeScreen(videoId: String) {
        show(YoutubeScreen(), YoutubeScreen.newBundle(videoId))
    }

    override fun toSearchResults(query: SearchQuery, page: Int?) {
        push(SearchResultScreen(), SearchResultScreen.newBundle(query))
    }

    override fun toExploreSearchOptions(
        onlyTopics: Boolean,
        onlyImages: Boolean,
        selectedBoard: Board?
    ) {
        show(
            SearchOptionsDialog(),
            SearchOptionsDialog.newBundle(onlyTopics, onlyImages, selectedBoard)
        )
    }

    override fun toPostingRules() {
        show(RulesScreen())
    }

    override fun toImagePicker() {
        push(ImagePickerScreen())
    }

    override fun back(): Boolean {
        controller.currentFrag?.let {
            if (it is BackPressable && it.handledBackPress()) {
                return true
            }
        }
        return controller.popFragment()
    }


    private fun positionToTabItem(index: Int) = when (index) {
        INDEX_TOPICS -> TabItem.Topics
        INDEX_RECENT_POSTS -> TabItem.RecentPosts
        INDEX_PROFILE -> TabItem.Profile
        INDEX_PROFILE_MY_LIKES -> TabItem.MyLikes
        INDEX_PROFILE_MY_SHARED_POSTS -> TabItem.MyShares
        INDEX_PROFILE_FOLLOWERS -> TabItem.MyFollowers
        INDEX_INBOX -> TabItem.Inbox
        INDEX_MENTIONS -> TabItem.Mentions
        INDEX_LIKES_AND_SHARES -> TabItem.LikesAndShares
        INDEX_SHARED_WITH_ME -> TabItem.SharedWithMe
        INDEX_FOLLOWING -> TabItem.Following
        else -> throw IllegalArgumentException("Unknown index '$index'")
    }

    private fun tabItemToPosition(destination: TabItem) = when (destination) {
        TabItem.RecentPosts -> INDEX_RECENT_POSTS
        TabItem.Mentions -> INDEX_MENTIONS
        TabItem.LikesAndShares -> INDEX_LIKES_AND_SHARES
        TabItem.Profile -> INDEX_PROFILE
        TabItem.Topics -> INDEX_TOPICS
        TabItem.SharedWithMe -> INDEX_SHARED_WITH_ME
        TabItem.Following -> INDEX_FOLLOWING
        TabItem.MyLikes -> INDEX_PROFILE_MY_LIKES
        TabItem.MyShares -> INDEX_PROFILE_MY_SHARED_POSTS
        TabItem.MyFollowers -> INDEX_PROFILE_FOLLOWERS
        TabItem.Inbox -> INDEX_INBOX
    }

    override fun toTabItem(destination: TabItem) {
        val tab = tabItemToPosition(destination)
        controller.switchTab(tab)
    }

    override fun toAccountDeletion(userAccount: RealUserAccount) {
        show(AccountDeletionScreen(), AccountDeletionScreen.newBundle(userAccount))
    }

    override fun toSignIn(userName: String?) {
        if (userName != null) {
            push(SignInScreen(), SignInScreen.newBundle(userName))
        } else {
            push(SignInScreen())
        }
    }

    override fun toPostEditorSettings() {
        show(EditActionsPickerScreen())
    }

    override fun toPostPreview(html: String) {
        show(PreviewPostScreen(), PreviewPostScreen.newBundle(html))
    }

    override fun setOnDialogDismissListener(
        viewLifecycleOwner: LifecycleOwner,
        listener: (numDialogsDisplayed: Int) -> Unit
    ) {
        currentDismissEventListener = WeakReference(controller.currentFrag)
        dialogDismissEvent.removeObservers(viewLifecycleOwner)
        dialogDismissEvent.observe(viewLifecycleOwner, EventObserver { listener(it) })
    }


    override fun toMailUser(user: User) {
        push(MailUserScreen(), MailUserScreen.newBundle(user))
    }

    override fun toMailSuperMods() {
        push(MailSuperModsScreen())
    }

    override fun toMailBoardMods(boardId: Int, board: Board) {
        push(MailBoardModsScreen(), MailBoardModsScreen.bundle(board, boardId))
    }

    override fun toSelectBoardDialog(selectedBoard: Board?, showAllBoard: Boolean) {
        show(
            SelectBoardDialog(),
            SelectBoardDialog.newBundle(board = selectedBoard, showAllBoards = showAllBoard)
        )
    }

    override fun toGenderPicker(gender: Gender?) {
        show(GenderPickerScreen(), GenderPickerScreen.newBundle(gender))
    }

    override fun toDatePicker(date: BirthDate?, minYear: Year) {
        show(DatePickerScreen(), DatePickerScreen.newBundle(date = date, minYear = minYear))
    }

    override fun toUserPosts(user: User) {
        push(UserPostsScreen(), UserPostsScreen.newBundle(user))
    }

    override fun toUserTopics(user: User) {
        push(UserTopicsScreen(), BaseTopicListScreen.newBundle(UserTopics(user)))
    }

    override fun toGotoUserProfile() {
        show(GoToUserScreen())
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        controller.onSaveInstanceState(outState)
    }

    override fun toPost(postId: String) {
        push(TopicLoaderScreen(), TopicLoaderScreen.newBundle(postId))
    }

    override fun toTopicHistory() {
        push(ViewedTopicListScreen())
    }

    override fun toPostPicker(posts: List<QuotablePost>) {
        show(PostPickerScreen(), PostPickerScreen.bundle(posts))
    }

    private fun push(
        fragment: Fragment,
        bundle: Bundle? = null,
        options: FragNavTransactionOptions? = null
    ) {
        clearDialogFragment()
        controller.pushFragment(fragment.apply { this.arguments = bundle }, options)
    }

    private fun replace(
        fragment: Fragment,
        bundle: Bundle? = null,
        options: FragNavTransactionOptions? = null
    ) {
        clearDialogFragment()
        controller.replaceFragment(fragment.apply { this.arguments = bundle }, options)
    }

    private fun show(dialogFragment: DialogFragment, bundle: Bundle? = null) {
        //clearDialogFragment()
        showDialogFragment(dialogFragment.also {
            it.arguments = bundle
        })
    }

    /**
     * Copy of [FragNavController.showDialogFragment] with call to [FragNavController.clearDialogFragment] removed
     */
    private fun showDialogFragment(dialogFragment: DialogFragment) {
        val fragmentManager: FragmentManager = getFragmentManagerForDialog()
        currentDialogFrag = WeakReference(dialogFragment)
        try {
            dialogFragment.show(fragmentManager, dialogFragment.javaClass.name)
        } catch (e: IllegalStateException) {
            Timber.e("Could not show dialog: $e")
            // Activity was likely destroyed before we had a chance to show, nothing can be done here.
        }
    }

    /**
     * Copy of [FragNavController.clearDialogFragment]
     */
    private fun clearDialogFragment() {
        val currentDialogFrag = currentDialogFrag?.get()
        if (currentDialogFrag != null) {
            currentDialogFrag.dismiss()
            this.currentDialogFrag = null
        } else {
            val fragmentManager: FragmentManager = getFragmentManagerForDialog()
            fragmentManager.fragments.forEach {
                if (it is DialogFragment) {
                    it.dismiss()
                }
            }
        }
    }

    /**
     * @see [FragNavController.getFragmentManagerForDialog] for difference in implementation.
     */
    private fun getFragmentManagerForDialog(): FragmentManager {
        return this.fragmentManager
    }


    companion object {
        private const val INDEX_TOPICS = FragNavController.TAB1
        private const val INDEX_RECENT_POSTS = FragNavController.TAB2
        private const val INDEX_PROFILE = FragNavController.TAB3
        private const val INDEX_INBOX = FragNavController.TAB4
        private const val INDEX_MENTIONS = FragNavController.TAB5
        private const val INDEX_LIKES_AND_SHARES = FragNavController.TAB6
        private const val INDEX_SHARED_WITH_ME = FragNavController.TAB7
        private const val INDEX_FOLLOWING = FragNavController.TAB8
        private const val INDEX_PROFILE_MY_LIKES = FragNavController.TAB9
        private const val INDEX_PROFILE_MY_SHARED_POSTS = FragNavController.TAB10
        private const val INDEX_PROFILE_FOLLOWERS = FragNavController.TAB11
    }


}
