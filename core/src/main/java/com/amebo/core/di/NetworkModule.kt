package com.amebo.core.di

import com.amebo.core.BuildConfig
import com.amebo.core.Values
import com.amebo.core.apis.util.SoupConverterFactory
import com.amebo.core.auth.Authenticator
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.User
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


@Module
class NetworkModule {
    @Provides
    fun provideRetrofit(cookieJar: CookieJar, soupConverterFactory: SoupConverterFactory) =
        makeRetrofit(
            makeHttpClient(cookieJar),
            soupConverterFactory
        )


    @Provides
    fun provideHttpClient(cookieJar: CookieJar) = makeHttpClient(cookieJar)

    @Provides
    @Auth
    fun provideAuthRetrofit() = makeRetrofit(
        makeHttpClient(Authenticator.JAR),
        SoupConverterFactory(null)
    )

    @Provides
    fun provideSoupConverterFactory(
        nairalandSessionObservable: NairalandSessionObservable,
        user: User?
    ) =
        SoupConverterFactory(if (user == null) null else nairalandSessionObservable)

    companion object {
        private fun makeRetrofit(
            okHttpClient: OkHttpClient,
            soupConverterFactory: SoupConverterFactory
        ): Retrofit = Retrofit.Builder()
            .baseUrl(Values.URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(soupConverterFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .build()

        private fun makeHttpClient(cookieJar: CookieJar): OkHttpClient =
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(NetworkInterceptor)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                })
                .cookieJar(cookieJar)
                .build()

        private object NetworkInterceptor : Interceptor {
            private const val USER_AGENT =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"

            override fun intercept(chain: Interceptor.Chain): Response {
                val original = chain.request()
                val request = original.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Connection", "keep-alive")
                    .build()
                return chain.proceed(request)
            }
        }

    }
}


