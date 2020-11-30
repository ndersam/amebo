package com.amebo.core.crawler

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.reflect.Type


abstract class TestCase {
    fun fetchDocument(filename: String): Document {
        return Jsoup.parse(readFile(filename))
    }

    fun fetchElement(filename: String): Document {
        return Jsoup.parseBodyFragment(readFile(filename))
    }

    fun readFile(filename: String): String {
        //ClassLoader.
        return this.javaClass::class.java.getResource(filename)!!.readText()
    }

    fun fetchTestCases(filename: String): List<Map<String, String>> {
        val string: String = readFile(filename)
        val gson = Gson()
        val type: Type = object :
            TypeToken<Map<String,List<Map<String, String>>>>() {}.type
        val json: Map<String,List<Map<String, String>>> = gson.fromJson(string, type)
        return json["data"]!!
    }


//
//    fun <T> any(): T {
//        Mockito.any<T>()
//        return uninitialized()
//    }
//
//    fun <T> any(type : Class<T>): T {
//        Mockito.any(type)
//        return null as T
//    }
//
//    private fun <T> uninitialized(): T = null as T
}