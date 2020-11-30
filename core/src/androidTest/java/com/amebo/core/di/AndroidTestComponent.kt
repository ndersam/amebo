package com.amebo.core.di

import android.content.Context
import com.amebo.core.data.datasources.impls.AndroidTopicListDataSourceImplTest
import com.amebo.core.data.datasources.impls.BoardDataSourceImplTest
import com.amebo.core.di.tests.DebugDatabaseModule
import com.amebo.core.di.tests.TestCookieModule
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.User
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        NetworkModule::class,
        TestCookieModule::class,
        ApiModule::class,
        DebugDatabaseModule::class,
        DataSourcesModule::class,
        CoroutineContextProviderModule::class
    ]
)
interface AndroidTestComponent {
    fun inject(case: BoardDataSourceImplTest)
    fun inject(case: AndroidTopicListDataSourceImplTest)


    @Component.Builder
    interface Builder {
        fun build(): AndroidTestComponent

        @BindsInstance
        fun contextModule(context: Context): Builder

        @BindsInstance
        fun user(user: User): Builder

        @BindsInstance
        fun sessionObservable(nairalandSessionObservable: NairalandSessionObservable): Builder
    }
}