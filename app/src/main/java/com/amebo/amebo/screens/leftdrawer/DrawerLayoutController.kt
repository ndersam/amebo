package com.amebo.amebo.screens.leftdrawer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.TabItem
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.core.domain.DisplayPhoto
import com.amebo.core.domain.DisplayPhotoBitmap
import com.amebo.core.domain.DisplayPhotoUrl

/***
 * A delegate for managing interactions with the [DrawerLayoutView] and its parent [DrawerLayout]
 */
class DrawerLayoutController(
    private val router: Router,
    private val fragmentManager: FragmentManager,
    private val viewLifecycleOwner: LifecycleOwner,
    private val pref: Pref,
    private val userManagementViewModel: UserManagementViewModel,
    private val view: DrawerLayoutView
) : LifecycleObserver, DrawerLayoutView.Listener {

    private val handler = Handler(Looper.getMainLooper())
    private var countPressed = 0

    init {
        view.listener = this
        viewLifecycleOwner.lifecycle.addObserver(this)
        router.addOnTabSelectedListener(view::setSelection)

        // Locked initially, fragments that need it will unlock it via DrawerLayoutContainer interface
        view.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)

        router.addOnFragmentChanged { isRoot ->
            val drawer = view.drawerLayout
            if (isRoot) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START)
            } else {
                drawer.closeDrawer(GravityCompat.START)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)
            }
        }
    }

    fun onBackPress(): Boolean {
        val drawer = view.drawerLayout
        return when {
            drawer.isDrawerOpen(GravityCompat.START) -> {
                drawer.closeDrawer(GravityCompat.START)
                true
            }
            router.back() -> true
            pref.confirmExit -> confirmExit()
            else -> false
        }
    }

    private fun confirmExit(): Boolean {
        if (countPressed++ == 0) {
            Toast.makeText(
                view.drawerLayout.context,
                "Press BACK again to exit.",
                Toast.LENGTH_SHORT
            ).show()
            handler.postDelayed({ countPressed = 0 }, 2000)
            return true
        }
        return false
    }


    @OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
    fun onLifecycleCreated() {
        fragmentManager.setFragmentResultListener(
            FragKeys.RESULT_RESHOW_ACCOUNT_LIST,
            viewLifecycleOwner
        ) { _, _ ->
            router.toAccountList()
        }
        userManagementViewModel.sessionEvent.observe(
            viewLifecycleOwner,
            Observer {
                if (pref.isLoggedIn) {
                    view.setNotification(it)
                }
            }
        )
        userManagementViewModel.displayPhotoEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onDisplayPhotoEventContent)
        )

        userManagementViewModel.loadDisplayPhotoOrAvatar()
    }

    override fun onItemSelected(item: TabItem) = router.toTabItem(item)


    override fun onSettingsClicked() = router.toSettings()

    override fun onGoToProfileClicked() = router.toGotoUserProfile()

    override fun onImageClicked() {
        if (pref.isLoggedIn) {
            router.toProfile()
        }
    }

    override fun onHeaderClicked() = router.toAccountList()

    fun restoreSavedState(savedState: Bundle?) = view.restoreSavedState(savedState)

    fun saveInstanceState(outState: Bundle) = view.saveInstanceState(outState)

    private fun onDisplayPhotoEventContent(resource: Resource<DisplayPhoto>) {
        when (resource) {
            is Resource.Loading -> setPhoto(resource.content)
            is Resource.Success -> setPhoto(resource.content)
            is Resource.Error -> setPhoto(resource.content)
        }
    }

    private fun setPhoto(displayPhoto: DisplayPhoto?) {
        when (displayPhoto) {
            is DisplayPhotoBitmap ->
                view.setDisplayPhoto(displayPhoto.bitmap)
            is DisplayPhotoUrl ->
                view.setDisplayPhoto(displayPhoto.url)
            else -> {
                // Do nothing
            }
        }
    }
}

