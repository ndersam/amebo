package com.amebo.core.common

import com.amebo.core.crawler.TestCase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


fun String.openResourceAsText() = TestCase::class.java.getResource(this)!!.readText()

fun String.openAsDocument(): Document = Jsoup.parse(openResourceAsText())