package com.amebo.core.data.cookies

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import java.util.*

class CookieStore  constructor(val id: String = IN_MEMORY, private val map: MutableMap<String, List<Cookie>> = HashMap()) {

    val session: String?
        get() {
            val cookies = map[NAIRALAND] ?: return null
            for (cookie in cookies) {
                if (cookie.name.equals("session", ignoreCase = true))
                    return cookie.value
            }
            return null
        }

    /**
     * Replaces old cookies with new cookies.
     *
     * @param host
     * @param newCookies
     */
    fun update(host: String, newCookies: List<Cookie>) {
        val oldCookies = map[host]

        if (oldCookies == null) {
            map[host] = newCookies
        } else {
            val nameToCookies = mutableMapOf<String, Cookie>()
            for (old in oldCookies)
                nameToCookies[old.name] = old

            for (cookie in newCookies) {
                if (nameToCookies.containsKey(cookie.name)) {
                    nameToCookies[cookie.name] = cookie
                }
            }
            map[host] = ArrayList(nameToCookies.values)
        }
    }

    /**
     * Returns a list of [Cookie]s.
     *
     * @param host HTTPUrl host
     * @return is null if userName doesn't exist in cookie store. Returns an empty list if no cookies
     * for the desired host is found.
     */
    fun getCookies(host: String): List<Cookie> {
        val cookies = map[host]
        return cookies ?: ArrayList()
    }

    override fun equals(other: Any?): Boolean {
        return other is CookieStore && other.id.equals(id, ignoreCase = true)
    }

    override fun toString(): String {
        return toString(this)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + map.hashCode()
        return result
    }

    companion object {

        private const val NAIRALAND = "www.nairaland.com"
        private const val IN_MEMORY = "default"

        fun from(userName: String, cookieList: List<Cookie>, url: String = NAIRALAND): CookieStore {
            val map = HashMap<String, List<Cookie>>()
            map[url] = cookieList
            return CookieStore(userName, map)
        }

        private fun toString(map: CookieStore): String {
            val gson = Gson()
            val type = object : TypeToken<CookieStore>() {

            }.type
            return gson.toJson(map, type)
        }

        fun from(string: String): CookieStore {
            val gson = Gson()
            val type = object : TypeToken<CookieStore>() {

            }.type
            return gson.fromJson(string, type)
        }
    }

}
