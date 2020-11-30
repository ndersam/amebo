package com.amebo.amebo.di

import android.app.Application
import android.content.Context
import com.amebo.amebo.application.App
import com.amebo.amebo.common.Pref
import com.amebo.core.di.CoreComponent
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        ScreenBindingModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {
    fun inject(app: App)

    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun context(app: Context): Builder

        @BindsInstance
        fun pref(pref: Pref): Builder

        @BindsInstance
        fun coreComponent(coreComponent: CoreComponent): Builder

        @BindsInstance
        fun app(app: Application): Builder
    }
}