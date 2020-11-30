package com.amebo.amebo.application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Optional
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.TouchEventDispatcher
import com.amebo.amebo.common.activities.ThemedActivity
import com.amebo.amebo.common.drawerLayout.DrawerLayoutContainer
import com.amebo.amebo.common.extensions.forwardToBrowser
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.databinding.ActivityMainBinding
import com.amebo.amebo.di.MainActivityInjector
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.leftdrawer.DrawerLayoutController
import com.amebo.amebo.screens.leftdrawer.DrawerLayoutView
import com.amebo.core.domain.IntentParseResult
import com.amebo.core.domain.SearchQuery
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class MainActivity : ThemedActivity(), HasAndroidInjector, DrawerLayoutContainer,
    TouchEventDispatcher {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var pref: Pref

    @Inject
    lateinit var routerFactory: RouterFactory

    private lateinit var router: Router

    private val touchEventListeners = mutableListOf<(MotionEvent) -> Unit>()

    private val viewModel get() = createViewModel<MainActivityViewModel>(this)

    private val userViewModel get() = createViewModel<UserManagementViewModel>(this)

    private lateinit var drawerLayoutController: DrawerLayoutController

    private lateinit var drawerLayoutView: DrawerLayoutView

    override val drawerLayout: DrawerLayout get() = binding.drawerLayout

    private var cacheIntent: Intent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        MainActivityInjector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        router = routerFactory.create(this)

        drawerLayoutView = DrawerLayoutView(pref, binding.leftDrawer)

        drawerLayoutController = DrawerLayoutController(
            fragmentManager = supportFragmentManager,
            viewLifecycleOwner = this,
            view = drawerLayoutView,
            userManagementViewModel = userViewModel,
            pref = pref,
            router = router
        )
        router.initialize(savedInstanceState)

        viewModel.unknownUriResultEvent.observe(this, EventObserver(::handleNewContent))
        binding.drawerLayout.post {
            handleIntent(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawerLayoutController.saveInstanceState(outState)
        router.onSaveInstanceState(outState)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onBackPressed() {
        if (drawerLayoutController.onBackPress().not()) {
            super.onBackPressed()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            touchEventListeners.forEach { it(ev) }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun register(listener: (MotionEvent) -> Unit) {
        touchEventListeners.remove(listener)
        touchEventListeners.add(listener)
    }

    override fun unRegister(listener: (MotionEvent) -> Unit) {
        touchEventListeners.remove(listener)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val appLinkData: Uri? = intent.data
                appLinkData?.let { uri ->
                    viewModel.handleUri(uri)
                }
                cacheIntent = intent
            }
            INTENT_ACTION_TOPIC -> {
                router.toTopic(intent.getParcelableExtra(INTENT_EXTRA_TOPIC) ?: return)
            }
            INTENT_ACTION_GREET_USER -> {
                val username = intent.getStringExtra(INTENT_EXTRA_USERNAME) ?: return
                Toast.makeText(
                    this,
                    getString(R.string.login_success, username),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        clearIntent()
    }

    private fun clearIntent() {
        // 'Clear' intent, else when activity recreates it gets replayed
        intent?.let {
            it.action = ""
            it.replaceExtras(Bundle())
            it.data = null
            it.flags = 0
        }
    }

    private fun handleNewContent(content: Optional<IntentParseResult>) {
        val result = content.value
        if (result == null) {
            forwardToBrowser(cacheIntent ?: return)
        } else {
            onSuccess(result)
        }
        cacheIntent = null
    }

    private fun onSuccess(result: IntentParseResult) {
        when (result) {
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
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    companion object {
        const val INTENT_ACTION_TOPIC = "com.amebo.amebo.view_featured_topic"
        const val INTENT_EXTRA_TOPIC = "TOPIC"
        const val INTENT_ACTION_GREET_USER = "com.amebo.amebo.greet_user"
        const val INTENT_EXTRA_USERNAME = "USERNAME"
    }


}
