package com.amebo.amebo.screens.newpost.newpost

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [NewPostScreenModule::class])
abstract class NewPostScreenBindingModule {
    @Binds
    abstract fun bindFragment(screen: NewPostScreen): Fragment
}

@Module
class NewPostScreenModule {

    @Provides
    fun provideFormView(fragment: NewPostScreen): NewPostView {
        return NewPostView(fragment, fragment.pref, fragment.binding, fragment.topic, fragment)
    }
}

