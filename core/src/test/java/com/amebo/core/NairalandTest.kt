package com.amebo.core

import com.amebo.core.di.real.DaggerTestCoreComponent
import com.amebo.core.domain.Featured
import com.amebo.core.domain.NairalandSessionObservable
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import timber.log.Timber

class NairalandTest {

    companion object {
        @BeforeClass
        fun before() {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Test
    fun `can crawl for Featured topics for minutes without error`() {
        val nairaland = getNairaland()
        val start = 0
        val end = 200
        for (i in start until end) {
            try {
                val result = runBlocking {
                    nairaland.sources.topicLists.fetch(Featured, page = i, null)
                }
                assertThat(result).isInstanceOf(Ok::class.java)
                Thread.sleep(5_000L)
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }
    }

    private fun getNairaland(): Nairaland {
        val component = DaggerTestCoreComponent.builder()
            .observable(NairalandSessionObservable())
            .user(null)
            .build()
        return component.provideNairaland()
    }
}