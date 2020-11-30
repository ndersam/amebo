package com.amebo.core.di

import com.amebo.core.domain.User

fun interface UserSupplier {
    fun get(): User?
}