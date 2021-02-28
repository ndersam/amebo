package com.amebo.amebo.screens.explore

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.dpToPx
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.databinding.ExploreScreenBinding
import com.amebo.amebo.di.Injectable
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.core.domain.Board
import com.amebo.core.domain.SearchQuery
import com.amebo.core.domain.Session
import com.amebo.core.domain.TopicList
import com.github.heyalex.bottomdrawer.BottomDrawerDialog
import com.github.heyalex.bottomdrawer.BottomDrawerFragment
import com.github.heyalex.handle.PlainHandleView
import javax.inject.Inject
import com.amebo.amebo.common.extensions.hideKeyboard as hideKeyboardExt

class ExploreScreen : BottomDrawerFragment(), Injectable, ExploreView.Listener {

    companion object {
        private const val SEARCH_QUERY = "query"
        fun newBundle(query: SearchQuery) = bundleOf(SEARCH_QUERY to query)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var routerFactory: RouterFactory

    lateinit var router: Router

    @Inject
    lateinit var pref: Pref

    private val viewModel by viewModels<ExploreScreenViewModel> { this }
    private val userManagementViewModel by viewModels<UserManagementViewModel> { requireActivity() }
    private val binding by viewBinding(ExploreScreenBinding::bind)
    private lateinit var exploreView: ExploreView

    override fun configureBottomDrawer(): BottomDrawerDialog {
        return BottomDrawerDialog.build(requireContext()) {
            theme = context.asTheme().exploreBottomDialogThemeRes
            handleView = PlainHandleView(context).apply {
                val widthHandle = requireContext().dpToPx(56)
                val heightHandle = requireContext().dpToPx(6)
                val params =
                    FrameLayout.LayoutParams(widthHandle, heightHandle, Gravity.CENTER_HORIZONTAL)

                params.topMargin = resources.getDimensionPixelSize(R.dimen.medium)

                layoutParams = params
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.explore_screen, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        router = routerFactory.create(this)
        exploreView = ExploreView(pref, binding, this)

        viewModel.exploreData.observe(
            viewLifecycleOwner,
            Observer(::onExploreDataEvent)
        )

        viewModel.fetchedFollowedBoardsEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFetchedFollowedBoards)
        )
        userManagementViewModel.sessionEvent.observe(viewLifecycleOwner, {
            binding.rvBoards.adapter?.notifyDataSetChanged()
        })

        viewModel.loadBoards()
    }


    private fun onFetchedFollowedBoards(resource: Resource<List<Board>>) {
        when (resource) {
            is Resource.Success -> {
                if (resource.content.isNotEmpty()) {
                    pref.showFollowedBoardHint = false
                }
                exploreView.onFetchedFollowedBoardsSuccess(resource)
                pref.setFollowedBoardsSyncTime()
            }
            is Resource.Loading -> exploreView.onFetchedFollowedBoardsLoading(resource)
            is Resource.Error -> exploreView.onFetchedFollowedBoardsError(resource)
        }
    }

    private fun onExploreDataEvent(data: ExploreData) {
        exploreView.setExploreData(data)
    }

    override fun onTopicListClicked(topicList: TopicList) {
        hideKeyboardExt()
        router.toTopicList(topicList)
    }

    override fun dismissFollowedBoardHint() {
        pref.showFollowedBoardHint = false
    }

    override val isLoggedIn: Boolean get() = pref.isLoggedIn

    override val showFollowedBoardHint: Boolean get() = pref.showFollowedBoardHint

    override fun showSearch(view: View) {
        router.toSearch(view)
    }

    override fun fetchFollowedBoards() = viewModel.fetchFollowedBoards()

    override fun onRecentTopicsClicked() = router.toTopicHistory()

    override fun getSession(): Session? {
        return userManagementViewModel.sessionEvent.value
    }

    inline fun <reified T : ViewModel> viewModels(crossinline owner: () -> ViewModelStoreOwner) =
        lazy {
            T::class.java.let { clazz ->
                ViewModelProvider(owner(), viewModelFactory).get(clazz)
            }
        }
}
