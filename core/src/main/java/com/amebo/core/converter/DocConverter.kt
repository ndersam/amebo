package com.amebo.core.converter

import android.content.Context
import androidx.annotation.RestrictTo
import com.amebo.core.converter.service.BBCodeParser
import com.amebo.core.converter.service.BBCodeToHTMLTransformer

object DocConverter {
    fun initialize(context: Context) {
        BBCodeToHTMLTransformer.initialize(context)
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun initialize(){
        BBCodeToHTMLTransformer.initialize()
    }

    fun bbCodeToHtml(bbCode: String): String {
        val document =
            BBCodeParser().buildDocument(bbCode, null)
        return BBCodeToHTMLTransformer().transform(
            document,
            { true },
            null,
            //Transformer.TransformFunction.HTMLTransformFunction(),
            null
        )
    }
}