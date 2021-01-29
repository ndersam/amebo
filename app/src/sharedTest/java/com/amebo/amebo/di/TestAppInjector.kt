package com.amebo.amebo.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.amebo.amebo.TestApp
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.di.mocks.Mocks
import com.amebo.core.domain.User

object TestAppInjector {
    fun inject(app: TestApp, user: User = User("random")) {

        DaggerTestAppComponent.builder()
            .context(app)
            .app(app)
            .user(user)
            .pref(Mocks.PrefModule.create())
            .sessionObservable(app.sessionObservable)
            .build()
            .inject(app)
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity) {

            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityStopped(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {

            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                if (p0 is TestFragmentActivity) {
                    MainActivityInjector.inject(p0)
                }
            }
        })
    }
}