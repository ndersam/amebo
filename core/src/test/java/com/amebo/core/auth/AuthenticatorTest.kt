package com.amebo.core.auth

import com.amebo.core.BuildConfig
import com.amebo.core.Values
import com.amebo.core.apis.AuthServiceApi
import com.amebo.core.common.openAsDocument
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.di.mocks.DaggerTestComponent
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.ResultWrapper
import com.google.common.truth.Truth.assertThat
import com.haroldadmin.cnradapter.NetworkResponse
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Request
import org.jsoup.nodes.Document
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

class AuthenticatorTest {


    class RealAccount {
        lateinit var auth: Authenticator

        @Before
        fun before() {
            auth = DaggerTestComponent.builder().build().provideAuthenticator()
        }

        @Test
        fun testLoginWorks() {
            runBlocking {
                val result = auth.login(BuildConfig.TEST_USER, BuildConfig.TEST_PASSWORD)
                assertThat(result.isSuccess).isTrue()
            }
        }
    }

    class Scenarios {

        private lateinit var auth: Authenticator
        private lateinit var api: MockAuthServiceApi

        @Before
        fun before() {
            api = MockAuthServiceApi()
            auth = Authenticator(api, CoroutineContextProvider(Dispatchers.Default))
        }

        @Test
        fun `many failed login attempts parsed correctly`() = runBlocking {
            setResponse("/auth/many_failed_login_attempts.html")

            val result = auth.login("", "")
            assertThat(result is ResultWrapper.Failure && result.data is ErrorResponse.Unknown).isTrue()
            val data = (result as ResultWrapper.Failure).data
            if (data is ErrorResponse.Unknown) {
                assertThat(data.msg).isEqualTo("too many failed login attempts. please wait for up to 5 minutes")
            }
        }


        @Test
        fun `wrong username or password`() {
            runBlocking {
                setResponse("/auth/wrong_username_or_password.html")

                val result = auth.login("", "")
                assertThat(result is ResultWrapper.Failure && result.data is ErrorResponse.Login)
            }
        }

        private fun setResponse(fileName: String) {
            val call = mock<Call<Document>>()
            val r = resp(fileName.openAsDocument())

            api.loginCall = call
            whenever(call.execute()).thenReturn(r)
        }

        private class MockAuthServiceApi(
            var gotoLoginResp: NetworkResponse<Document, ErrorResponse> = NetworkResponse.Success(
                mock()
            ),
            var loginCall: Call<Document> = mock(),
            var visitCall: Call<Document> = mock()
        ) : AuthServiceApi {
            override fun login(
                name: String,
                password: String,
                referer: String,
                redirect: String,
                agent: String
            ): Call<Document> = loginCall

            override suspend fun gotoLogin(): NetworkResponse<Document, ErrorResponse> =
                gotoLoginResp

            override fun visit(url: String): Call<Document> = visitCall
        }

        private fun resp(soup: Document): Response<Document> {
            return Response.success(
                soup, okhttp3.Response.Builder() //
                    .code(200)
                    .message("OK")
                    .protocol(Protocol.HTTP_1_1)
                    .request(Request.Builder().url(Values.URL + "/login").build())
                    .build()
            )
        }
    }
}