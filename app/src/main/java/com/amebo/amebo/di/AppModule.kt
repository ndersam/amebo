package com.amebo.amebo.di

import androidx.work.Configuration
import com.amebo.amebo.BuildConfig
import com.amebo.amebo.screens.feed.FeedWorkerFactory
import com.amebo.core.di.CoreComponent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideWorkerConfiguration(workerFactory: FeedWorkerFactory): Configuration =
        Configuration.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    setMinimumLoggingLevel(android.util.Log.INFO)
                }
            }
            .setWorkerFactory(workerFactory)
            .build()


    @Singleton
    @Provides
    fun provideNairaland(coreComponent: CoreComponent) = coreComponent.provideNairaland()

    @Provides
    fun provideObservable(coreComponent: CoreComponent) = coreComponent.provideObservable()
}
