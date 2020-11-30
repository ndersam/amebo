package com.amebo.amebo.common.routing

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface RouterFactory {
    fun create(activity: FragmentActivity): Router
    fun create(fragment: Fragment): Router
}