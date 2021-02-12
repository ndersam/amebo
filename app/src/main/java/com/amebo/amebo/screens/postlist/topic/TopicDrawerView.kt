package com.amebo.amebo.screens.postlist.topic

import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.TopicScreenBinding
import com.amebo.core.domain.Topic
import java.lang.ref.WeakReference

class TopicDrawerView(
    fragment: BaseFragment,
    binding: TopicScreenBinding,
    topic: Topic
) : LifecycleObserver {
    private val drawerLayoutRef = WeakReference(binding.drawerLayout)
    private val drawerLayout get() = drawerLayoutRef.get()!!
    private val viewPagerRef = WeakReference(binding.viewPager)
    private val viewPager get() = viewPagerRef.get()!!

    init {
        fragment.viewLifecycleOwner.lifecycle.addObserver(this)
        viewPager.adapter = ViewPagerAdapter(topic, fragment)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }


    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy() {
        viewPagerRef.get()?.adapter = null
    }

    fun viewRecentTopics() {
        drawerLayout.openDrawer(GravityCompat.END)
        viewPager.currentItem = 1
    }

    fun viewTopicInfo() {
        drawerLayout.openDrawer(GravityCompat.END)
        viewPager.currentItem = 0
    }
}