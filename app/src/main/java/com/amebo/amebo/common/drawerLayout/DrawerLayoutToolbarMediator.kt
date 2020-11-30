package com.amebo.amebo.common.drawerLayout

import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.amebo.amebo.R
import com.amebo.amebo.common.Badge
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.screens.accounts.UserManagementViewModel


object DrawerLayoutToolbarMediator {
    operator fun invoke(fragment: BaseFragment, toolbar: Toolbar) {
        val drawerLayout = DrawerLayoutContainer.from(fragment).drawerLayout
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            fragment.requireActivity(), drawerLayout,
            toolbar,
            R.string.navigation_drawer_close,
            R.string.navigation_drawer_open
        )
        val badge = Badge(fragment.requireContext())
        fragment.activityViewModels<UserManagementViewModel>().value.sessionEvent.observe(fragment.viewLifecycleOwner) {
            badge.setCount(if (it.count > 0) " " else "")
        }

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = false
        actionBarDrawerToggle.toolbarNavigationClickListener = View.OnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        actionBarDrawerToggle.setHomeAsUpIndicator(badge.icon)
        actionBarDrawerToggle.syncState()

        fragment.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
            fun onLifecycleDestroy() {
                drawerLayout.removeDrawerListener(actionBarDrawerToggle)
            }
        })
    }
}