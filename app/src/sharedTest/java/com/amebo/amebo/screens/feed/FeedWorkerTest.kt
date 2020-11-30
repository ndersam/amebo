package com.amebo.amebo.screens.feed

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.amebo.amebo.TestApp
import com.amebo.amebo.di.TestAppModule
import com.amebo.amebo.suite.injectIntoTestApp
import com.amebo.core.domain.ResultWrapper
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedWorkerTest {
    lateinit var context: TestApp

    @Before
    fun before() {
        injectIntoTestApp()
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            context.workManagerConfiguration
        )
        runBlocking {
            whenever(TestAppModule.nairaland.sources.misc.feed()).thenReturn(
                ResultWrapper.success(
                    emptyList()
                )
            )
        }
    }

    @Test
    fun testWorkerPullsFeedSuccessfully() {

        runBlocking {
            val worker: FeedWorker = TestListenableWorkerBuilder<FeedWorker>(context).apply {
                setWorkerFactory(context.workManagerConfiguration.workerFactory)
            }.build()
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
        }
    }
}