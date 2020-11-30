package com.amebo.core.auth

import com.amebo.core.BuildConfig
import com.amebo.core.di.mocks.DaggerTestComponent
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class AuthenticatorTest {
    @Inject
    lateinit var auth: Authenticator

    @Before
    fun before() {
        DaggerTestComponent.builder().build().inject(this)
    }

    @Test
    fun testLoginWorks() {
        runBlocking {
            val cookies = auth.login(BuildConfig.TEST_USER, BuildConfig.TEST_PASSWORD)
        }
    }
}