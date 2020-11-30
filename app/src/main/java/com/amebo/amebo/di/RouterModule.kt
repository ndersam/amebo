package com.amebo.amebo.di

import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.common.routing.fragnav.FragNavRouterFactory
import dagger.Module
import dagger.Provides

@Module
class RouterModule {
    @Provides
    @ActivityScope
    fun provideRouter(): RouterFactory {
        return FragNavRouterFactory()
    }
}