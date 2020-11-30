package com.amebo.core.apis.util

import com.amebo.core.crawler.SessionParser
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.Session
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import timber.log.Timber
import java.lang.reflect.Type

/**
 * If user is signed in, pass in a sessionObserver
 */
class SoupConverterFactory(private val nairalandSessionObservable: NairalandSessionObservable? = null) :
    Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return SoupConverter(
            nairalandSessionObservable
        )
    }

    class SoupConverter(private val nairalandSessionObservable: NairalandSessionObservable? = null) :
        Converter<ResponseBody, Document> {

        override fun convert(value: ResponseBody): Document? {
            val soup = Jsoup.parse(value.string())
            try {
                handleSession(SessionParser.parse(soup.selectFirst("#up")))
            } catch (e: Exception) {
                Timber.e(e)
                Timber.e(soup.html())
            }
            return soup
        }

        private fun handleSession(session: Session) {
            nairalandSessionObservable?.value = session
        }
    }
}