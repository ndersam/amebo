package com.amebo.core.di.real

import com.amebo.amebo.data.local.Cookie
import com.amebo.amebo.data.local.UserAccountData
import com.amebo.core.Database
import com.amebo.core.Nairaland
import com.amebo.core.data.cookies.CookieStore
import com.amebo.core.data.datasources.impls.BoardDataSourceImpl
import com.amebo.core.di.*
import com.amebo.core.di.tests.TestCookieModule
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.User
import com.nhaarman.mockitokotlin2.mock
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CoroutineContextProviderModule::class,
        ApiModule::class,
        TestDatabaseModule::class,
        DataSourcesModule::class,
        AuthModule::class,
        NetworkModule::class,
        TestCookieModule::class
    ]
)
interface TestCoreComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun user(user: User?): Builder

        @BindsInstance
        fun observable(nairalandSessionObservable: NairalandSessionObservable): Builder

        fun build(): TestCoreComponent
    }

    fun provideNairaland(): Nairaland
}

@Module
internal class TestDatabaseModule {
    @Provides
    fun provideDatabase(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    @Provides
    fun provideCookieAdapter(): Cookie.Adapter = Cookie.Adapter(
        cookieAdapter = object : ColumnAdapter<CookieStore, String> {
            override fun decode(databaseValue: String): CookieStore =
                CookieStore.from(databaseValue)

            override fun encode(value: CookieStore): String = value.toString()
        }
    )

    @Provides
    fun provideDelightfulDatabase(driver: SqlDriver, cookie: Cookie.Adapter): Database =
        Database(
            driver, cookie, userAccountDataAdapter = UserAccountData.Adapter(
                usersFollowingAdapter = DatabaseModule.usersFollowingAdapter
            )
        )

    @Provides
    fun provideSourcesModule(): BoardDataSourceImpl.SourceDatabase = mock()
}
