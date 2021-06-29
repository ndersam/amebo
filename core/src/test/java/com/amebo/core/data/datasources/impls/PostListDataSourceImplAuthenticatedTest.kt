package com.amebo.core.data.datasources.impls

import com.amebo.core.BuildConfig
import com.amebo.core.data.datasources.PostListDataSource
import com.amebo.core.di.real.DaggerTestCoreComponent
import com.amebo.core.domain.*
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test

class PostListDataSourceImplAuthenticatedTest {

    companion object {
        lateinit var data: PostListDataSource

        @JvmStatic
        @BeforeClass
        fun before() {
            val component = DaggerTestCoreComponent.builder()
                .observable(NairalandSessionObservable())
                .user(User(BuildConfig.TEST_USER))
                .build()
            runBlocking {
                val auth = component.authService()
                val result = auth.login(BuildConfig.TEST_USER, BuildConfig.TEST_PASSWORD)
                assert(result is Ok)
            }
            data = component.postListDataSource()
        }
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
            val query = SearchQuery("Buhari",
                onlyShowTopicPosts = false,
                onlyShowImages = false,
                board = null
            )
            val res = data.fetch(query, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }

    @Test
    fun `my likes`() {
        runBlocking {
            val res = data.fetch(MyLikes, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }

    @Test
    fun `my shares`() {
        runBlocking {
            val res = data.fetch(MyLikes, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }

    @Test
    fun `my shared posts`() {
        runBlocking {
            val res = data.fetch(MySharedPosts, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }

    @Test
    fun `posts by people you are following`() {
        runBlocking {
            val res = data.fetch(PostsByPeopleYouAreFollowing, 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }

    @Test
    fun mentions() {
        runBlocking {
            val res = data.fetch(Mentions(User(BuildConfig.TEST_USER)), 0)
            assertThat(res).isInstanceOf(Ok::class.java)
            if (res is Ok){
                val post = res.value.data.first() as TimelinePost
                println(post.post.text)
            }
        }
    }
}