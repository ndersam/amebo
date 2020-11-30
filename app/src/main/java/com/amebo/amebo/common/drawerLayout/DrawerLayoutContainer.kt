package com.amebo.amebo.common.drawerLayout

import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment

interface DrawerLayoutContainer {
    val drawerLayout: DrawerLayout

    companion object {

        fun from(fragment: Fragment): DrawerLayoutContainer {
            return fragment.requireActivity() as DrawerLayoutContainer
        }
    }
}