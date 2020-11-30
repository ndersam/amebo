package com.amebo.core.crawler.form

import com.amebo.core.crawler.TestCase
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class FormCrawlerTest: TestCase() {

    @Test
    fun fetch_quotable_posts() {
        val tBody =
            fetchDocument("/forms/data/newpost01.html").selectFirst("table[summary=\"posts\"]")
        val posts = fetchQuotablePosts(tBody)
        assertThat(posts.size).isEqualTo(33)
    }

    @Test
    fun newPost() {
        for (case in fetchTestCases("/forms/cases.json")) {
            val soup = fetchDocument("/forms/data/" + case["file"]!!)
            val form = parseNewPost(soup).asSuccess.data
            assertEquals(case["session"]!!, form.session)
            assertEquals(case["body"]!!, form.body)
            assertEquals(case["maxpost"]!!.toInt(), form.maxPost)
            assertEquals(case["topic"]!!, form.topic)
            assertEquals(case["follow"]!!.toBoolean(), form.follow)
            assertEquals(case["title"]!!, form.title)
        }
    }



}