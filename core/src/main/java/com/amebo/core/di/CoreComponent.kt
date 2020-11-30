package com.amebo.core.di

import android.content.Context
import com.amebo.core.Nairaland
import com.amebo.core.domain.NairalandSessionObservable
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CoroutineContextProviderModule::class,
        ApiModule::class,
        DatabaseModule::class,
        DataSourcesModule::class,
        AuthModule::class,
        NetworkModule::class,
        CookieModule::class,
        UserModule::class
    ]
)
interface CoreComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun userSupplier(supplier: UserSupplier): Builder

        @BindsInstance
        fun observable(nairalandSessionObservable: NairalandSessionObservable): Builder

        fun build(): CoreComponent
    }

    fun provideNairaland(): Nairaland

    fun provideObservable(): NairalandSessionObservable
}