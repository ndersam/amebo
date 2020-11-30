package com.amebo.core.api

import com.amebo.core.apis.MiscApi
import com.amebo.core.di.NetworkModule
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ErrorHandlingTest {
    var retrofit = NetworkModule().provideAuthRetrofit()

    @Test
    fun test(): Unit = runBlocking{
//        val api = retrofit.create(UserApi::class.java)
//        val result = api.fetchUser("1234fsadfdfdfdf45")
        val result = retrofit.create(MiscApi::class.java).visitPage("https://www.nairaland.com/areyoumuslim")
        print(result.toString())
    }
}