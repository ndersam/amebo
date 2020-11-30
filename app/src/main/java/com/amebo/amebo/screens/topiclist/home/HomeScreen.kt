package com.amebo.amebo.screens.topiclist.home

import android.os.Bundle
import com.amebo.amebo.common.drawerLayout.DrawerLayoutToolbarMediator
import com.amebo.amebo.screens.topiclist.main.TopicListScreen

class HomeScreen : TopicListScreen() {

    override fun onViewCreated(savedInstanceState: Bundle?) {
        // FIXME Can be improved
        arguments = newBundle(pref.homePageTopicList)
        super.onViewCreated(savedInstanceState)
    }

    override fun initializeViewBindings() {
        super.initializeViewBindings()
        DrawerLayoutToolbarMediator(this, binding.toolbar)
    }
}