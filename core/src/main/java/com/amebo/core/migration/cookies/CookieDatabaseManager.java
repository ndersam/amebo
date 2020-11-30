package com.amebo.core.migration.cookies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class CookieDatabaseManager extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "amebo-cookies";
    public static final int DATABASE_VERSION = 1;

    // Table Schema
    private static final String TABLE_COOKIES = "table_cookies";
    private static final String COL_USER_NAME = "username";
    private static final String COL_COOKIES = "cookies";

    private static CookieDatabaseManager sInstance;

    private CookieDatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized CookieDatabaseManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (CookieDatabaseManager.class) {
                if (sInstance == null) {
                    sInstance = new CookieDatabaseManager(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_COOKIES + " (" +
                COL_USER_NAME + " TEXT PRIMARY KEY, " +
                COL_COOKIES + " TEXT NOT NULL " +
                ");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_COOKIES + ";";
            db.execSQL(DELETE_TABLE);
            onCreate(db);
        }
    }


    public void removeCookies(String userName) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(TABLE_COOKIES, COL_USER_NAME + "=?", new String[]{userName.trim().toLowerCase()});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public String readCookies(String userName) {
        String SELECT = "SELECT * FROM " + TABLE_COOKIES + " WHERE " + COL_USER_NAME + "=?";
        String cookies = null;

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(SELECT, new String[]{userName.trim().toLowerCase()});
        if (c.moveToNext()) {
            int index = c.getColumnIndex(COL_COOKIES);
            cookies = c.getString(index);
        }
        c.close();

        return cookies;
    }

    public void addCookies(String userName, String cookiesJSON) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, userName.trim().toLowerCase());
        values.put(COL_COOKIES, cookiesJSON);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.insertWithOnConflict(TABLE_COOKIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}