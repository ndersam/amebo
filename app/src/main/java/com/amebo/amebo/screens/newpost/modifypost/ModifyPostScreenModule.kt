package com.amebo.amebo.screens.newpost.modifypost

import dagger.Module
import dagger.Provides

@Module(includes = [ModifyPostScreenModule::class])
abstract class ModifyPostScreenBindingModule

@Module
class ModifyPostScreenModule {

    @Provides
    fun provideFormView(fragment: ModifyPostScreen): ModifyPostView {
        return ModifyPostView(fragment, fragment.pref, fragment.binding, fragment.topic, fragment)
    }
}

