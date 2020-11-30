package com.amebo.amebo.di

import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import com.amebo.amebo.screens.feed.FeedWorkerFactory
import com.amebo.core.Nairaland
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestAppModule {
   companion object {
       var nairaland: Nairaland = TestNairalandProvider.newNairalandInstance()
           private set
   }

    @Singleton
    @Provides
    fun provideWorkerConfiguration(workerFactory: FeedWorkerFactory): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory)
            .build()

    @Provides
    fun provideNairaland(): Nairaland {
        return nairaland
    }
}
