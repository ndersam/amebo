package com.amebo.amebo.di

import com.amebo.amebo.application.MainActivity
import com.amebo.amebo.screens.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("UNUSED")
@Module
abstract class ScreenBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentModule::class, RouterModule::class])
    abstract fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun splashActivity(): SplashActivity
}