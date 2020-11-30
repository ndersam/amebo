package com.amebo.amebo

import android.app.Application
import androidx.work.Configuration
import com.amebo.amebo.common.HasNairalandObservable
import com.amebo.core.domain.NairalandSessionObservable
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class TestApp : Application(), HasAndroidInjector, HasNairalandObservable, Configuration.Provider {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var configuration: Configuration

    override var sessionObservable: NairalandSessionObservable = NairalandSessionObservable()

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    override fun getWorkManagerConfiguration() = configuration

}