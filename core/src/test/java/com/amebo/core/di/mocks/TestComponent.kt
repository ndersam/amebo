package com.amebo.core.di.mocks

import com.amebo.core.auth.Authenticator
import com.amebo.core.auth.AuthenticatorTest
import com.amebo.core.data.datasources.TopicListDataSourceImplTest
import com.amebo.core.data.datasources.impls.PostListDataSourceImplTest
import com.amebo.core.di.*
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
        AuthModule::class,
        CoroutineContextProviderModule::class,
        TestDatabaseModule::class,
        DataSourcesModule::class
    ]
)
interface TestComponent {
    fun inject(case: TopicListDataSourceImplTest)
    fun inject(auth: AuthenticatorTest)
    fun inject(case: PostListDataSourceImplTest)

    fun provideAuthenticator(): Authenticator

    @Component.Builder
    interface Builder {
        fun build(): TestComponent

        @BindsInstance
        fun user(user: User): Builder

        @BindsInstance
        fun sessionObservable(nairalandSessionObservable: NairalandSessionObservable): Builder
    }
}
