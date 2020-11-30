package com.amebo.core.di

import com.amebo.core.domain.User
import dagger.Module
import dagger.Provides

@Module
class UserModule {
    @Provides
    fun provideUser(userSupplier: UserSupplier): User? = userSupplier.get()
}