package com.amebo.amebo.di

import androidx.lifecycle.ViewModelProvider
import com.nhaarman.mockitokotlin2.mock
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class TestViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Module
    companion object {
        val viewModelFactory: ViewModelFactory = mock()

        @JvmStatic
        @Provides
        fun provideFavoritesViewModelFactory(): ViewModelFactory =
            viewModelFactory
    }
}