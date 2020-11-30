package com.amebo.core.data.datasources.impls

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.core.appContext
import com.amebo.core.data.datasources.BoardDataSource
import com.amebo.core.di.DaggerAndroidTestComponent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class BoardDataSourceImplTest {

    @Inject
    lateinit var src: BoardDataSourceImpl.SourceDatabase

    @Inject
    lateinit var source: BoardDataSource

    @Before
    fun before() {
        DaggerAndroidTestComponent
            .builder()
            .contextModule(appContext)
            .build().inject(this)
    }

    @Test
    fun testDataSourceWorksWell() {
        val boards = src.fetchBoards()
        assertEquals(65, boards.size)
    }

    @Test
    fun testLoadAllReturnsAllBoards() {
       runBlocking {
           var boards = source.loadAll()
           assertEquals(0, boards.size)
           source.initialize()
           boards = source.loadAll()
           assertEquals(65, boards.size)
       }
    }
}