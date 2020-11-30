package com.amebo.core.di

import com.amebo.core.apis.AuthServiceApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {
    @Provides
    fun provideAuth(@Auth retrofit: Retrofit): AuthServiceApi {
        return retrofit.create(AuthServiceApi::class.java)
    }
}