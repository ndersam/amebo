package com.amebo.amebo.di

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection

object MainActivityInjector {

    private val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentAttached(
            fm: FragmentManager,
            f: Fragment,
            context: Context
        ) {
            if (f is Injectable) {
                AndroidSupportInjection.inject(f)
            }
        }
    }

    fun inject(activity: Activity) {
        AndroidInjection.inject(activity)

        if (activity is FragmentActivity) {
            activity.supportFragmentManager
                .registerFragmentLifecycleCallbacks(
                    callback,
                    true
                )
        }
    }
}
