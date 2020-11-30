package com.amebo.core.data.datasources.impls

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.core.apis.TopicListApi
import com.amebo.core.appContext
import com.amebo.core.di.DaggerAndroidTestComponent
import com.amebo.core.domain.TopicListSorts
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AndroidTopicListDataSourceImplTest {
    @Inject
    lateinit var api: TopicListApi

    @Inject
    lateinit var src: BoardDataSourceImpl.SourceDatabase

    @Before
    fun before() {
        DaggerAndroidTestComponent
            .builder()
            .contextModule(appContext)
            .build()
            .inject(this)
    }

    @Test
    fun testBoardLoadingWorks() {
        runBlocking {
            val boards = src.fetchBoards()

            boards.forEach {
                val soup = api.fetchBoardSoup(it.url, TopicListSorts.UPDATED.value, 0)
                assertNotNull(soup)
            }
        }
    }
}