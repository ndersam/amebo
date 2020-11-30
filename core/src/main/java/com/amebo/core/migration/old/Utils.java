package com.amebo.core.migration.old;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    private static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("GMT+1");


    private Utils() {
    }

    /**
     * Returns the SERVER current date in format MMM dd e.g Sep 03
     */
    public static String currentDate() {
        SimpleDateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return df.format(LocalDateTime.now(DateTimeZone.forID("Africa/Lagos")).toDate());
    }

    static String dateTimeToday() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MMM_dd_mm_ss", Locale.getDefault());
        return df.format(LocalDateTime.now().toDate());
    }

    /**
     * Returns the SERVER current date year
     */
    public static int currentYear() {
        return LocalDateTime.now(DateTimeZone.forID("Africa/Lagos")).getYear();
    }

    public static String monthYearFormat(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        return sdf.format(new LocalDateTime(timestamp, DateTimeZone.forID("Africa/Lagos")).toDate());
    }

    /**
     * Converts String expressions of date and time to timestamp
     *
     * @param time time in format like 9:38pm
     * @param date date in format like Sep 08 or Aug 17
     * @return timestamp in milliseconds
     */
    public static long toTimeStamp(String time, String date, String year) {
        try {
            int col = time.indexOf(':');
            int hour = Integer.parseInt(time.substring(0, col));
            int minute = Integer.parseInt(time.substring(col + 1, col + 3));
            char amPmTime = time.charAt(time.length() - 2);

            if (amPmTime == 'a') {
                if (hour == 12) {
                    hour = 0;
                }
            } else {
                if (hour != 12) {
                    hour += 12;
                }
            }

            String dateTime = String.format(Locale.getDefault(), "%02d", hour) + ":" + String.format(Locale.getDefault(), "%02d", minute) + " " + date.trim() + " " + year.trim();
            String dateTimePattern = "HH:mm MMM dd yyyy";

            DateTimeFormatter f = DateTimeFormat.forPattern(dateTimePattern)
                    .withZone(DateTimeZone.forID("Africa/Lagos"));
            return f.parseDateTime(dateTime).getMillis();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @param dateString e.g. January 09, 1997
     * @return timestamp
     */
    public static long timeRegisteredToStamp(String dateString) {
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("MMMMM dd, yyyy", Locale.getDefault());
            sourceFormat.setTimeZone(SERVER_TIME_ZONE);
            return sourceFormat.parse(dateString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the local time delta from timestamp
     */
    public static String getRelativeTimeSpan(long timestamp) {
        long diff = (DateTime.now().getMillis() - timestamp) / 1000;
        if (diff < 60L)
            return diff + "s";
        else if (diff < 3600L)
            return diff / 60 + "m";
        else if (diff < (3600L * 24L))
            return diff / 3600 + "h";
        else if (diff < (3600L * 24L * 31L))
            return diff / (3600 * 24) + "d";
        else if (diff < (3600L * 24L * 365L))
            return diff / (3600 * 24 * 30) + "mth";
        else {
            return diff / (3600L * 24L * 365L) + "yrs";
        }
    }


//    public static String getNairalandAge(long timestamp) {
//        SimpleDateFormat sourceFormat = new SimpleDateFormat("MMMMM dd, yyyy", Locale.getDefault());
//        sourceFormat.setTimeZone(SERVER_TIME_ZONE);
//
//        String output;
//
//        long diff = (System.currentTimeMillis() - timestamp) / 1000;
//
//        final long MIN = 60L;
//        final long HOUR = MIN * 60L;
//        final long DAY = 24 * HOUR;
//        final long MONTH = DAY * 30;
//        final long YEAR = DAY * 365;
//
//        long year = diff / YEAR;
//        long month = (diff % YEAR) / MONTH;
//        long day = (diff % MONTH) / DAY;
//
//        output = year == 0 ? "" : year + "y";
//        output = month == 0 ? output : output.isEmpty() ? month + "m" : output + " " + month + "m";
//        output = day == 0 ? output : output.isEmpty() ? day + "d" : output + " " + day + "d";
//
//        return output;
//    }

//    public static int getColor(int color) {
//        return App.getInstance().getResources().getColor(color);
//    }

    public static String largeValueFormat(int count) {
        if (count < 1000)
            return String.valueOf(count);
        else if (count < 1000 * 1000) {
            int thousand = count / 1000;
            int hundred = (count / 100) % 10;
            return thousand + (hundred == 0 ? "" : "." + hundred) + "k";
        } else {
            int million = count / (1000 * 1000);
            int hundredThousand = (count / (1000 * 100)) % 10;
            return million + (hundredThousand == 0 ? "" : "." + hundredThousand) + "m";
        }
    }

    public static String millisToDateTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

//
//    private static SharedPreferences preferences() {
//        return PreferenceManager.getDefaultSharedPreferences(App.getContext());
//    }

//    public static void putBoolean(String key, boolean value) {
//        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean(key, value);
//        editor.apply();
//    }

//    public static void putInt(String key, int value) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt(key, value);
//        editor.apply();
//    }
//
//    public static void putLong(String key, long value) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putLong(key, value);
//        editor.apply();
//    }

//    public static void putString(String key, String value) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString(key, value);
//        editor.apply();
//        editor.commit();
//    }

//    public static void putFloat(String key, float value) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putFloat(key, value);
//        editor.apply();
//        editor.commit();
//    }
//
//    public static boolean putStringSet(Context context, String key, Set<String> value) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putStringSet(key, value);
//        editor.apply();
//        return true;
//    }

//    public static boolean getBoolean(String key, boolean alt) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        return preferences.getBoolean(key, alt);
//    }

//    public static int getInt(String key, int alt) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        return preferences().getInt(key, alt);
//    }
//
//    public static int getIntAt(@ArrayRes int res, int position) {
//        return App.getInstance().getResources().getIntArray(res)[position];
//    }

//    public static long getLong(String key, long alt) {
//        return preferences().getLong(key, alt);
//    }
//
//    public static float getFlaot(String key, float alt) {
//        return preferences().getFloat(key, alt);
//    }

//    public static String getString(String key, String alt) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        return preferences.getString(key, alt);
//    }

//    public static String getStringRes(int key) {
//        return App.getInstance().getResources().getString(key);
//    }

//    public static int getIntRes(int key) {
//        return App.getInstance().getResources().getInteger(key);
//    }

//    public static int[] getIntArrayRes(int key) {
//        return App.getContext().getResources().getIntArray(key);
//    }

//    public static String[] getStringArrayRes(int key) {
//        return App.getContext().getResources().getStringArray(key);
//    }

//    public static Set<String> getStringSet(Context context, String key, Set<String> alt) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        return preferences.getStringSet(key, alt);
//    }
//
//    public static void removeKey(Context context, String key) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.remove(key).apply();
//    }

    @ColorInt
    public static int resolveColor(Context context, @AttrRes int colorRes) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(colorRes, typedValue, true);
        return typedValue.data;
    }

//    @ColorInt
//    public static int resolveColor(@AttrRes int colorRes) {
//        return resolveColor(App.getContext(), colorRes);
//    }


//    public static String format(@StringRes int res, @NonNull Object... objects) {
//        return String.format(getStringRes(res), objects);
//    }

    public static String userListToString(List<User> userList) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<User>>() {
        }.getType();
        return gson.toJson(userList, type);
    }

    public static List<User> userListFromString(String string) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<User>>() {
        }.getType();
        return gson.fromJson(string, type);
    }


    public static String actionsToString(List<EditAction> actions) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<EditAction>>() {
        }.getType();
        return gson.toJson(actions, type);
    }

    public static List<EditAction> actionsFromString(String string) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<EditAction>>() {
        }.getType();
        return gson.fromJson(string, type);
    }

    public static String topicUrl(Topic topic) {
        String url = Consts.URL_BASE + "/" + topic.getId();
        if (!topic.getUrl().startsWith("/"))
            url = url + "/";
        return url + topic.getUrl();
    }
}