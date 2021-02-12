package com.amebo.amebo.common

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class AvatarGeneratorTest{

    @Test
    fun testGenerationWorks() {
        runBlocking {
            val context =  ApplicationProvider.getApplicationContext<Context>()
            val result = AvatarGenerator.generate(context, "")
        }
    }
}