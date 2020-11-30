package com.amebo.core.migration.old;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

public class CookieStore {
    /*
       Maps "URL_HOST" To COOKIE_LIST
        */
    private final String userName;
    private final Map<String, List<Cookie>> map;


    private CookieStore(String userName, Map<String, List<Cookie>> map) {
        this.map = map;
        this.userName = userName;
    }

    private static CookieStore from(String userName, String url, List<Cookie> cookieList) {
        Map<String, List<Cookie>> map = new HashMap<>();
        map.put(url, cookieList);
        return new CookieStore(userName, map);
    }

    public static CookieStore from(String userName, List<Cookie> cookieList) {
        return from(userName, "www.nairaland.com", cookieList);
    }

    public static CookieStore newInstance(String userName) {
        return new CookieStore(userName, new HashMap<>());
    }

    private static String toString(CookieStore map) {
        Gson gson = new Gson();
        Type type = new TypeToken<CookieStore>() {
        }.getType();
        return gson.toJson(map, type);
    }

    public static CookieStore fromString(@NonNull String string) {
        Gson gson = new Gson();
        Type type = new TypeToken<CookieStore>() {
        }.getType();
        return gson.fromJson(string, type);
    }

    public String getUserName() {
        return userName;
    }

    public String getSession() {
        List<Cookie> cookies = map.get("www.nairaland.com");
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (cookie.name().equalsIgnoreCase("session"))
                return cookie.value();
        }
        return null;
    }

    /**
     * Replaces old cookies with new cookies.
     *
     * @param host
     * @param newCookies
     */
    public void update(String host, List<Cookie> newCookies) {
        List<Cookie> oldCookies = map.get(host);

        if (oldCookies == null) {
            map.put(host, newCookies);
        } else {
            Map<String, Cookie> nameToCookies = new HashMap<>();
            for (Cookie old : oldCookies)
                nameToCookies.put(old.name(), old);

            for (Cookie cookie : newCookies) {
                if (nameToCookies.containsKey(cookie.name())) {
                    nameToCookies.put(cookie.name(), cookie);
                }
            }
            map.put(host, new ArrayList<>(nameToCookies.values()));
        }
    }

    /**
     * Returns a list of {@link Cookie}s.
     *
     * @param host HTTPUrl host
     * @return is null if userName doesn't exist in cookie store. Returns an empty list if no cookies
     * for the desired host is found.
     */
    public List<Cookie> getCookies(String host) {
        List<Cookie> cookies = map.get(host);
        return cookies != null ? cookies : new ArrayList<>();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof CookieStore && ((CookieStore) obj).userName.equalsIgnoreCase(userName);
    }

    @NonNull
    @Override
    public String toString() {
        return toString(this);
    }

}