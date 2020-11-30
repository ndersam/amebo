package com.amebo.core.data.datasources.impls

import com.amebo.core.data.datasources.PostListDataSource
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class PostListDataSourceImplTest {
    @Inject
    lateinit var data: PostListDataSource

    @Before
    fun before() {
        //DaggerTestComponent.create().inject(this)
    }

    @Test
    fun `test correctly receives data`(){
        val topic = Topic(
            "Victim Of A Failed System A Story About 2 Great Nigerians",
            5671637,
            "victim-failed-system-story-2"
        )
       runBlocking {
           val res = data.fetch(topic, 0)
           println(res.isSuccess)
           if (res is ResultWrapper.Success){
               val post = res.data.data.first() as SimplePost
               println(post.text)
           }
       }
    }


}