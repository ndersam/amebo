package com.amebo.amebo.screens.postlist


import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import com.amebo.amebo.R
import com.amebo.amebo.common.*
import com.amebo.amebo.common.extensions.applyEnableTint
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.screens.postlist.adapters.image.ImageLoadingListener
import com.amebo.amebo.screens.postlist.adapters.image.ImageLoadingListenerImpl
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.core.domain.*


abstract class BasePostListScreen<T : PostList>(layoutRes: Int) : BaseFragment(layoutRes),
    IPostListView.Listener {

    companion object {
        private const val LAYOUT_MGR = "layout_manager"
        private const val LAST_SELECTED_POST = "lastSelectedPost"


        private fun newPopupMenu(view: View): PopupMenu {
            // https://stackoverflow.com/questions/23516247/how-change-position-of-popup-menu-on-android-overflow-button#answer-29702608
            val context = ContextThemeWrapper(view.context, R.style.PopupMenuOverlapAnchor)
            return PopupMenu(context, view)
        }
    }


    abstract val viewModel: PostListScreenViewModel<T>
    abstract var postListView: IPostListView?
    abstract val postList: T
    open val initialPage: Int = 0

    private val postListViewNN get() = postListView!!


    // Image transitions
    private var selectedImageView: View? = null
    private var lastImagePosition: Int? = null
    private var lastPostPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareTransitions()
        setFragmentResultListener(FragKeys.RESULT_SELECTED_PAGE) { _, bundle ->
            val page = bundle.getInt(FragKeys.BUNDLE_SELECTED_PAGE)
            viewModel.loadPage(page)
        }
        setFragmentResultListener(FragKeys.RESULT_POST_LIST) { _, bundle ->
            val postList = bundle.getParcelable<PostListDataPage>(FragKeys.BUNDLE_POST_LIST)!!
            viewModel.setData(postList)
        }
        setFragmentResultListener(FragKeys.RESULT_LAST_IMAGE_POSITION) { _, bundle ->
            lastImagePosition = bundle.getInt(FragKeys.BUNDLE_LAST_IMAGE_POSITION)
            lastPostPosition = requireArguments().getInt(LAST_SELECTED_POST)

            /**
             * @see [canPostponeEnterTransition] for the reason this needs to be removed.
             */
            requireArguments().remove(LAST_SELECTED_POST)
        }
        setFragmentResultListener(FragKeys.RESULT_RESHOW_ACCOUNT_LIST) { _, _ ->
            router.toAccountList()
        }
    }

    override val listenerLifecycle: Lifecycle
        get() = this.viewLifecycleOwner.lifecycle

    override val shouldHighlightPost: Boolean get() = viewModel.shouldHighlightPost

    override fun onDestroyView() {
        super.onDestroyView()
        postListView = null
        selectedImageView = null
        viewModel.shouldHighlightPost = false
    }

    override fun onPause() {
        super.onPause()
        saveLayoutState()
    }

    override fun onResume() {
        super.onResume()
        restoreLayoutState()
    }

    private fun canPostponeEnterTransition(): Boolean {
        val postPosition = requireArguments().getInt(LAST_SELECTED_POST, -1)
        return postPosition != -1
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (canPostponeEnterTransition()) postponeEnterTransition()

        initializeViews()

        viewModel.initialize(postList, initialPage)
        viewModel.dataEvent.observe(viewLifecycleOwner, EventObserver(::onEventChanged))
        viewModel.metaEvent.observe(
            viewLifecycleOwner,
            EventObserver(postListViewNN::setPostListMeta)
        )
        viewModel.unknownUriResultEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onUnknownUrlResult)
        )
    }

    abstract fun initializeViews()

    open fun preparePopupMenu(popupMenu: PopupMenu) {
        with(popupMenu) {
            inflate(R.menu.topic)
            menu.findItem(R.id.view_recent_topics).isVisible = postList is Topic
            menu.findItem(R.id.topic_info).isVisible = postList is Topic
            // disable page navigation if data hasn't been loaded
            run {
                val item = menu.findItem(R.id.jump_to_page)
                item.isEnabled = viewModel.lastPage != null
                item.applyEnableTint(requireContext())
            }

        }
    }

    open fun onPostListPopupMenuItemClicked(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.settings -> router.toSettings()
            R.id.expand_all -> postListViewNN.expandAllPosts()
            R.id.collapse_all -> postListViewNN.collapseAllPosts()
            R.id.jump_to_page -> router.toPostListPageOptions(
                viewModel.currentPage,
                viewModel.lastPage!!
            )
            R.id.search -> router.toSearch()
            R.id.go_to_profile -> router.toGotoUserProfile()
            else -> return false
        }
        return true
    }


    open fun onEventChanged(content: Resource<PostListDataPage>) {
        when (content) {
            is Resource.Loading -> onLoading(content)
            is Resource.Success -> onSuccess(content)
            is Resource.Error -> onError(content)
        }
        postListViewNN.setHasPrevPage(viewModel.hasPrevPage)
        postListViewNN.setHasNextPage(viewModel.hasNextPage)
    }

    private fun onUnknownUrlResult(content: Pair<String, Optional<IntentParseResult>>) {
        val (url, parseResult) = content
        when (val result = parseResult.value) {
            is IntentParseResult.PostListResult -> {
                val postList = result.postList
                when {
                    postList is SearchQuery && postList.query.isBlank() -> router.toSearch(query = postList)
                    else -> router.toPostList(result.postList, result.page)
                }
            }
            is IntentParseResult.TopicListResult -> router.toTopicList(
                result.topicList,
                result.page
            )
            is IntentParseResult.UserResult -> router.toUser(result.user)
            else -> {
                AppUtil.openInCustomTabs(requireContext(), url)
            }
        }
    }

    open fun onLoading(loading: Resource.Loading<PostListDataPage>) =
        postListViewNN.onLoading(loading)

    open fun onSuccess(success: Resource.Success<PostListDataPage>) =
        postListViewNN.onSuccess(success)

    open fun onError(error: Resource.Error<PostListDataPage>) = postListViewNN.onError(error)


    override val imageLoadingListener: ImageLoadingListener
        get() =
            ImageLoadingListenerImpl(this) { postPosition, imagePosition ->
                postPosition == lastPostPosition && lastImagePosition == imagePosition
            }

    override fun likePost(post: SimplePost, like: Boolean) = viewModel.likePost(post, like)

    override fun sharePost(post: SimplePost, share: Boolean) = viewModel.sharePost(post, share)

    override fun showContextMenuOnPost(post: SimplePost, postPosition: Int, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.menu_simple_post_context)
            menu.findItem(R.id.report_post)?.isVisible = pref.isLoggedIn
            menu.findItem(R.id.modify_post)?.isVisible = pref.isCurrentAccount(post.author)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.report_post -> onReportClicked(post)
                    R.id.view_post_author -> onUserClicked(post.author)
                    R.id.modify_post -> onModifyClicked(post)
                    R.id.collapse_post -> postListViewNN.collapsePostAt(postPosition)
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }.show()
    }

    override fun hasNextPage() = viewModel.hasNextPage

    override fun nextPage() = viewModel.loadNextPage()


    fun wrapLoggedIn(callback: () -> Unit) {
        if (pref.isLoggedIn) {
            callback()
        } else {
            router.toAccountList()
        }
    }

    override fun replyPost(post: SimplePost) {
        wrapLoggedIn { router.toReplyPost(post) }
    }

    private fun onReportClicked(post: SimplePost) {
        wrapLoggedIn { router.toReportPost(post) }
    }

    private fun onAuthorClicked(user: User) = router.toUser(user, true)

    open fun onModifyClicked(post: SimplePost) = wrapLoggedIn { router.toModifyPost(post) }

    override fun onImageClicked(
        imageView: View,
        position: Int,
        images: List<String>,
        postPosition: Int
    ) {
        handleImageClicks(imageView, postPosition, position, images)
    }

    override fun onImageClicked(textView: View, postPosition: Int, url: String) {
        handleImageClicks(textView, postPosition, 0, arrayListOf(url))
    }

    override fun onItemCollapsed(position: Int) {
        viewModel.collapsedItems.add(position)
    }

    override fun onItemExpanded(position: Int) {
        viewModel.collapsedItems.remove(position)
    }

    override fun isItemCollapsed(position: Int): Boolean =
        viewModel.collapsedItems.contains(position)


    override fun onYoutubeUrlClick(videoId: String) = router.toYoutubeScreen(videoId)


    override fun onPostTopicClick(topic: Topic, view: View) {
        newPopupMenu(view).apply {
            inflate(R.menu.go_to_topic_options)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.view_original_topic -> router.toTopic(topic, 0)
                    R.id.view_this_comment_in_thread -> router.toTopic(topic, topic.linkedPage)
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
            show()
        }
    }

    override fun onTopicLinkClick(topic: Topic) = router.toTopic(topic)

    override fun onBoardClicked(board: Board) = router.toBoard(board)

    override fun onReferencedPostClick(
        view: View,
        postPosition: Int,
        postID: String,
        author: String
    ) {
        newPopupMenu(view).apply {
            inflate(R.menu.referenced_post)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.view_post -> postListViewNN.scrollToPost(postID)
                    R.id.view_post_author -> onAuthorClicked(User(author))
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
            show()
        }
    }

    override fun onPostLinkClick(view: View, postPosition: Int, postID: String) = viewPost(postID)

    override fun onUnknownLinkClick(url: String) = viewModel.handleUnknownUrl(url)

    override fun onPageMetaClicked() {
        router.toPostListPageOptions(
            currentPage = viewModel.currentPage,
            lastPage = viewModel.lastPage ?: return
        )
    }

    override fun viewPost(postId: String) = router.toPost(postId)

    private fun handleImageClicks(
        imageView: View,
        postPosition: Int,
        position: Int,
        images: List<String>
    ) {
        selectedImageView = imageView
        requireArguments().putInt(LAST_SELECTED_POST, postPosition)

        val transitionName = "${getString(R.string.photo_transition)}_${postPosition}_$position"
        ViewCompat.setTransitionName(imageView, transitionName)

        // Exclude the clicked view from the exit transaction
        // (e.g. the card will disappear immediately instead of fading out
        // with the rest to prevent and overlapping animation of fade and move.
        (exitTransition as android.transition.TransitionSet).excludeTarget(imageView, true)
        router.toPhotoViewer(images, position, imageView, transitionName)
    }


    private fun prepareTransitions() {
        exitTransition = android.transition.TransitionInflater.from(context)
            .inflateTransition(R.transition.grid_exit_transition)

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
//        setExitSharedElementCallback(
//            object : SharedElementCallback() {
//                override fun onMapSharedElements(
//                    names: List<String>,
//                    sharedElements: MutableMap<String, View>
//                ) {
//                    sharedElements[names[0]] = selectedImageView ?: return
//                }
//            })
    }


    /**
     * Scrolls the recycler view to show the last viewed item in the list. This is important when
     * navigating back to the list.
     */
    private fun restoreLayoutState(bundle: Bundle = requireArguments()) {
        val parcelable = bundle.getParcelable<Parcelable?>(LAYOUT_MGR) ?: return
        postListViewNN.restoreState(parcelable, lastImagePosition, lastPostPosition)
        lastImagePosition = null
        lastPostPosition = null
    }

    private fun saveLayoutState(bundle: Bundle = requireArguments()) {
        bundle.putParcelable(LAYOUT_MGR, postListViewNN.saveState())
    }

    override fun onUserClicked(user: User) = router.toUser(user, true)

    override fun onNextClicked() = viewModel.loadNextPage()

    override fun onPrevClicked() = viewModel.loadPrevPage()

    override fun onFirstClicked() = viewModel.loadFirstPage()

    override fun onLastClicked() = viewModel.loadLastPage()


    override fun onMoreClicked(view: View) {
        PopupMenu(requireContext(), view).apply {
            preparePopupMenu(this)
            setOnMenuItemClickListener(::onPostListPopupMenuItemClicked)
            show()
        }
    }

    override fun onNavigationClicked() {
        router.back()
    }


    override fun onRetryClicked() = viewModel.retry()

    override fun onRefreshTriggered() = viewModel.refreshPage()
}
