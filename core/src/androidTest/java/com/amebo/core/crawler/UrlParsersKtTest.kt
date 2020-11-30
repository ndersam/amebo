package com.amebo.core.crawler

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UrlParsersKtTest {

    @Test
    fun board_url() {
        val uri = Uri.parse("https://www.nairaland.com/politics/2")
        val segments = uri.pathSegments
        println(uri.pathSegments)
    }
}