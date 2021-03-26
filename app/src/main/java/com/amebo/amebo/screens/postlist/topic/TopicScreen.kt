package com.amebo.amebo.screens.postlist.topic

import android.os.Bundle
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.setMenu
import com.amebo.amebo.common.extensions.shareText
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BackPressable
import com.amebo.amebo.databinding.TopicScreenBinding
import com.amebo.amebo.screens.postlist.BasePostListScreen
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.core.CoreUtils
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import com.amebo.core.domain.TopicPostListDataPage
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import javax.inject.Provider


class TopicScreen : BasePostListScreen<Topic>(R.layout.topic_screen), BackPressable {

    companion object {
        private const val PAGE = "page"
        private const val DATA = "data"
        private const val MARKED_VISITED = "marked_visited"
        const val POST_LIST = "postList"

        /**
         * @param page - must be a valid page. Use this when opening topic from link external to app
         */
        fun bundle(topic: Topic, page: Int = -1, data: TopicPostListDataPage? = null) =
            bundleOf(POST_LIST to topic, PAGE to page, DATA to data)

        fun setFollowing(item: MenuItem, isFollowing: Boolean) {
            item.setIcon(
                if (isFollowing)
                    R.drawable.ic_bookmark_24dp
                else
                    R.drawable.ic_bookmark_border_24dp
            )
            item.setTitle(
                if (isFollowing)
                    R.string.unfollow
                else R.string.follow
            )
        }
    }

    @Inject
    lateinit var postListViewProvider: Provider<TopicPostListView>

    val binding: TopicScreenBinding by viewBinding(TopicScreenBinding::bind)

    override val viewModel by viewModels<TopicViewModel>()
    override val initialPage: Int
        get() {
            val page = requireArguments().getInt(PAGE, -1)
            return if (page != -1) page else 0//postList.linkedPage
        }
    override var postListView: IPostListView? = null
    private lateinit var topicDrawerView: TopicDrawerView
    override val postList: Topic get() = requireArguments().getParcelable(POST_LIST)!!

    private var hasMarkedAsRecentlyVisited: Boolean
        get() = requireArguments().getBoolean(MARKED_VISITED, false)
        set(value) {
            requireArguments().putBoolean(MARKED_VISITED, value)
        }

    private val dataPage: TopicPostListDataPage? get() = requireArguments().getParcelable(DATA)

    override fun onViewCreated(savedInstanceState: Bundle?) {
        when (val dataPage = dataPage) {
            is TopicPostListDataPage -> {
                viewModel.setData(dataPage)
            }
        }

        super.onViewCreated(savedInstanceState)
        invalidateMenu()

        viewModel.addViewedTopicEvent.observe(viewLifecycleOwner, EventObserver {
            hasMarkedAsRecentlyVisited = true
        })

        if (!hasMarkedAsRecentlyVisited) {
            viewModel.markVisited()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toggle_follow) {
            setFollowing(
                item,
                !viewModel.isFollowing
            )
            viewModel.followTopic(!viewModel.isFollowing)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initializeViews() {
        binding.toolbar.setMenu(R.menu.topic_toolbar)
        binding.toolbar.setOnMenuItemClickListener(::onOptionsItemSelected)

        postListView = postListViewProvider.get()
        binding.bottomBar.btnReply.isVisible = true
        binding.bottomBar.btnReply.isEnabled = false
        binding.bottomBar.btnReply.setOnClickListener {
            handleClosed {
                router.toNewPost(postList)
            }
        }
        topicDrawerView = TopicDrawerView(this, binding, postList)
    }

    private fun invalidateMenu() {
        val menu = binding.toolbar.menu
        val itemFollow = menu.findItem(R.id.toggle_follow)!!
        itemFollow.isVisible = pref.isLoggedIn
        setFollowing(
            itemFollow,
            viewModel.isFollowing
        )
    }

    override fun onPostListPopupMenuItemClicked(it: MenuItem): Boolean {
        return when (it.itemId) {
            R.id.share -> {
                requireContext().shareText(CoreUtils.topicUrl(postList, viewModel.currentPage))
                true
            }
            R.id.view_recent_topics -> {
                topicDrawerView.viewRecentTopics()
                true
            }
            R.id.topic_info -> {
                topicDrawerView.viewTopicInfo()
                true
            }
            else -> super.onPostListPopupMenuItemClicked(it)
        }
    }


    override fun replyPost(post: SimplePost) {
        handleClosed { super.replyPost(post) }
    }

    override fun onModifyClicked(post: SimplePost) {
        handleClosed { super.onModifyClicked(post) }
    }

    private fun handleClosed(callback: () -> Unit) {
        wrapLoggedIn {
            val isHidden = viewModel.isHidden ?: return@wrapLoggedIn
            if (isHidden) {
                Snackbar.make(binding.root, R.string.topic_hidden, Snackbar.LENGTH_SHORT).show()
            } else {
                val isClosed = viewModel.isClosed ?: return@wrapLoggedIn
                if (isClosed) {
                    Snackbar.make(binding.root, R.string.closed_topic, Snackbar.LENGTH_SHORT).show()
                } else {
                    callback()
                }
            }

        }
    }

    override fun onEventChanged(content: Resource<PostListDataPage>) {
        super.onEventChanged(content)
        invalidateMenu()
    }

    override fun onSuccess(success: Resource.Success<PostListDataPage>) {
        super.onSuccess(success)
        val dataPage = (success.content as TopicPostListDataPage)
        val updatedTopic = dataPage.topic
        requireArguments().putParcelable(POST_LIST, updatedTopic)
        updateInfoFragment(dataPage)
    }

    override fun onLoading(loading: Resource.Loading<PostListDataPage>) {
        super.onLoading(loading)
        val dataPage = loading.content as? TopicPostListDataPage ?: return
        updateInfoFragment(dataPage)
    }

    override fun onError(error: Resource.Error<PostListDataPage>) {
        super.onError(error)
        val dataPage = error.content as? TopicPostListDataPage ?: return
        updateInfoFragment(dataPage)
    }

    private fun updateInfoFragment(dataPage: TopicPostListDataPage) {
        childFragmentManager.setFragmentResult(
            TopicInfoFragment.KEY_TOPIC_POSTS_PAGE,
            bundleOf(TopicInfoFragment.KEY_TOPIC_POSTS_PAGE to dataPage)
        )
    }

    override fun handledBackPress(): Boolean {
        if (view != null && binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            return true
        }
        return false
    }
}