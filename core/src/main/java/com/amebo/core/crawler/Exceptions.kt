package com.amebo.core.crawler

import org.jsoup.nodes.Document

class ParseException(msg: String) : Exception(msg)

class DocumentParseException(private val document: Document, private val url: String, private val throwable: Throwable) :
    Exception() {

    override val message: String
        get() = """
                    Parse Exception
                    ================================================================
                    URL: $url
                    ---------------------------------------------------------------
                    
                    ORIGINAL EXCEPTION
                    ---------------------------------------------------------------
                    ${throwable.stackTraceToString()}
                    
                    DOCUMENT
                    ---------------------------------------------------------------
                    ${document.outerHtml()}
                """.trimIndent()
}
