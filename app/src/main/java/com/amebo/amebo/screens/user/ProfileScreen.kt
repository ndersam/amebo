package com.amebo.amebo.screens.user

import android.os.Bundle
import androidx.core.os.bundleOf
import com.amebo.amebo.common.drawerLayout.DrawerLayoutToolbarMediator
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.core.domain.User

class ProfileScreen : UserScreen(), AuthenticationRequired {

    private val isRootScreen get() = requireArguments().getBoolean(IS_ROOT_SCREEN, false)


    override fun onViewCreated(savedInstanceState: Bundle?) {
        when (val user = pref.user) {
            is User -> {
                arguments = arguments ?: Bundle()
                requireArguments().putParcelable(USER, user)
            }
        }
        super.onViewCreated(savedInstanceState)

        if (isRootScreen) {
            DrawerLayoutToolbarMediator(this, binding.toolbar)
        }
    }

    companion object {
        private const val IS_ROOT_SCREEN = "IS_ROOT_SCREEN"

        fun ProfileScreen.rootScreen() = apply {
            arguments = bundleOf(IS_ROOT_SCREEN to true)
        }
    }
}
