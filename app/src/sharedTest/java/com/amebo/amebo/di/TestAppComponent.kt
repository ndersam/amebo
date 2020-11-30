package com.amebo.amebo.di

import android.app.Application
import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import com.amebo.amebo.TestApp
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Pref
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.User
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        TestAppModule::class,
        ScreenBindingModule::class,
        TestViewModelModule::class
    ]
)
interface TestAppComponent {
    fun inject(app: TestApp)

    @Component.Builder
    interface Builder {
        fun build(): TestAppComponent

        @BindsInstance
        fun context(app: Context): Builder

        @BindsInstance
        fun user(user: User): Builder

        @BindsInstance
        fun pref(pref: Pref): Builder

        @BindsInstance
        fun app(app: Application): Builder

        @BindsInstance
        fun sessionObservable(sessionObservable: NairalandSessionObservable): Builder
    }
}

@Module
abstract class ScreenBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestFragmentModule::class])
    abstract fun emptyFragmentActivity(): FragmentScenario.EmptyFragmentActivity

//    @ActivityScope
    @ContributesAndroidInjector(modules = [TestFragmentModule::class])
    abstract fun testFragmentActivity(): TestFragmentActivity
}