package com.amebo.core.di.mocks

import com.amebo.core.data.local.Database
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
object TestDatabaseModule {

     val database: Database = mock {
         on { boardQueries }
     }

    @Provides
    @JvmStatic
    fun provideDatabase()= database
}