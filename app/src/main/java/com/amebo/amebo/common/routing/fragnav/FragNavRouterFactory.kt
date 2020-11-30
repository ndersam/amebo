package com.amebo.amebo.common.routing.fragnav

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.amebo.amebo.R
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.ncapdevi.fragnav.FragNavController

class FragNavRouterFactory : RouterFactory {
    private lateinit var router: FragNavRouter

    override fun create(activity: FragmentActivity): Router {
        router = FragNavRouter(
            fragmentManager = activity.supportFragmentManager,
            controller = FragNavController(activity.supportFragmentManager, R.id.hostFragment)
        )
        return router
    }

    override fun create(fragment: Fragment): Router = router
}