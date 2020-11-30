package com.amebo.amebo.screens.newpost.newtopic

import dagger.Module
import dagger.Provides

@Module(includes = [NewTopicScreenModule::class])
abstract class NewTopicScreenBindingModule

@Module
class NewTopicScreenModule {

    @Provides
    fun provideFormView(fragment: NewTopicScreen): NewTopicView {
        return NewTopicView(fragment, fragment.pref, fragment.binding, fragment.board, fragment)
    }
}

