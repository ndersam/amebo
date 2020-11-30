package com.amebo.core.di

import com.amebo.core.data.CoroutineContextProvider
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class CoroutineContextProviderModule {
    @Provides
    fun provideCoroutineContextProvider() = CoroutineContextProvider(Dispatchers.IO)
}