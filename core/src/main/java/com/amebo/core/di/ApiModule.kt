package com.amebo.core.di

import com.amebo.core.apis.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
internal class ApiModule {
    @Provides
    fun provideTopicListApi(retrofit: Retrofit): TopicListApi =
        retrofit.create(TopicListApi::class.java)

    @Provides
    fun providePostListApi(retrofit: Retrofit): PostListApi =
        retrofit.create(PostListApi::class.java)

    @Provides
    fun provideFormSubmissionApi(retrofit: Retrofit): FormSubmissionApi =
        retrofit.create(FormSubmissionApi::class.java)

    @Provides
    fun provideFormApi(retrofit: Retrofit): FormApi = retrofit.create(FormApi::class.java)

    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    fun provideMiscApi(retrofit: Retrofit): MiscApi = retrofit.create(MiscApi::class.java)
}

