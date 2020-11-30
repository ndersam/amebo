package com.amebo.amebo.application

import android.app.Application
import androidx.work.Configuration
import com.amebo.amebo.BuildConfig
import com.amebo.amebo.common.HasNairalandObservable
import com.amebo.amebo.common.Pref
import com.amebo.amebo.di.AppInjector
import com.amebo.amebo.screens.feed.FeedWorker
import com.amebo.core.domain.NairalandSessionObservable
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasAndroidInjector, HasNairalandObservable, Configuration.Provider {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var workerConfiguration: Configuration

    @Inject
    lateinit var pref: Pref

    override val sessionObservable by lazy { NairalandSessionObservable() }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        AppInjector.inject(this)

        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(pref.crashlyticsEnabled)

        FeedWorker.schedule(this)
    }

    override fun getWorkManagerConfiguration() = workerConfiguration
}