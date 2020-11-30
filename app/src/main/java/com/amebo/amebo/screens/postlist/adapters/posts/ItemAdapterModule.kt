package com.amebo.amebo.screens.postlist.adapters.posts

import androidx.fragment.app.Fragment
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import dagger.Module
import dagger.Provides

@Module
class ItemAdapterModule {

    @Provides
    fun provideContentAdapterModule(
        fragment: Fragment,
        pref: Pref,
        postListAdapterListener: PostListAdapterListener
    ) = ItemAdapter(
        fragment,
        pref,
        postListAdapterListener
    )
}