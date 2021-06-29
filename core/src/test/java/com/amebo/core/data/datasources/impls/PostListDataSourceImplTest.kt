package com.amebo.core.data.datasources.impls

import com.amebo.core.data.datasources.PostListDataSource
import com.amebo.core.di.real.DaggerTestCoreComponent
import com.amebo.core.domain.*
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PostListDataSourceImplTest {
    lateinit var data: PostListDataSource

    @Before
    fun before() {
        val component = DaggerTestCoreComponent.builder()
            .observable(NairalandSessionObservable())
            .build()
        data = component.postListDataSource()
    }

    @Test
    fun topic(){
       runBlocking {

           val topic = Topic(
               "Victim Of A Failed System A Story About 2 Great Nigerians",
               5671637,
               "victim-failed-system-story-2"
           )

           val res = data.fetch(topic, 0)
           assertThat(res).isInstanceOf(Ok::class.java)
           if (res is Ok){
               val post = res.value.data.first() as SimplePost
               println(post.text)
           }
       }
    }

    @Test
    fun `new posts`() {
        runBlocking {
            val res = data.fetch(RecentPosts, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }

    @Test
    fun `search query`() {
        runBlocking {
            val query = SearchQuery("Buhari", false, false, null)
            val res = data.fetch(query, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }
}