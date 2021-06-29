package com.amebo.core.data.datasources.impls

import com.amebo.core.apis.FormApi
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.di.NetworkModule
import com.amebo.core.di.TestSessionCookieJar
import com.amebo.core.di.TestSessionCookieModule
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.User
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onSuccess
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import javax.inject.Inject

class FormDataSourceImplTest {

    @Inject
    lateinit var retrofit: Retrofit

    @Before
    fun setUp() {
        DaggerFormDataSourceImplTestComponent.builder().build().inject(this)
    }

    @Test
    fun get_post(): Unit = runBlocking {
        val api = retrofit.create(FormApi::class.java)
        api.getPost(
            postID = "96105107",
            session = TestSessionCookieJar.session,
            referer = "https://www.nairaland.com/newpost?topic=6259418&post=96105107"
        ).awaitResult { Ok(it)}
            .onSuccess {
                assertThat(it).startsWith("[quote")
                assertThat(it).endsWith("[/quote]")
            }

    }
}

@Component(
    modules = [
        NetworkModule::class,
        TestSessionCookieModule::class,
        NetworkModuleExtraDepsModule::class
    ]
)
interface FormDataSourceImplTestComponent {
    fun inject(test: FormDataSourceImplTest)
}

@Module
class NetworkModuleExtraDepsModule {
    @Provides
    fun provideNairalandObservable(): NairalandSessionObservable = NairalandSessionObservable()

    @Provides
    fun provideUser(): User? = null
}