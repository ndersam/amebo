package com.amebo.core.migration.data;

public class Schema {

    public static final String DATABASE_NAME = "amebo-database";
    public static final int DATABASE_VERSION = 3;

    private Schema() {
    }

    static abstract class Topics {
        static final String TITLE = "title";
        static final String URL = "url";
        static final String ID = "id";
        static final String TIMESTAMP = "timestamp";
        static final String AUTHOR = "author";
        static final String TABLE_NAME = "topics";
        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                ID + " TEXT PRIMARY KEY, " +
                TITLE + " TEXT NOT NULL, " +
                URL + " TEXT NOT NULL, " +
                TIMESTAMP + " REAL, " +
                AUTHOR + " TEXT" + ");";

    }

    static abstract class ViewedTopics {
        static final String TABLE_NAME = "saved_topics";
        static final String ID = "id";


        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                ID + " TEXT PRIMARY KEY );";

        static final String SELECT_ALL = "SELECT " + ID + " FROM " + TABLE_NAME + ";";

        static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }

    static abstract class BoardData {
        static final String TABLE_NAME = "boards";
        static final String NAME = "name";
        static final String URL = "url";
        static final String BOARD_NO = "board_no";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                NAME + " TEXT NOT NULL, " +
                URL + " TEXT PRIMARY KEY, " +
                BOARD_NO + " INT" +
                ");";
        static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME + ";";
        static final String FIND_LIKE = "SELECT rowid _id, * FROM " + TABLE_NAME + " WHERE " + NAME + " LIKE ?;";
    }

    static abstract class FollowedBoards {
        static final String TABLE_NAME = "followed_boards";
        static final String USER_URL = "user_url";
        static final String BOARD_URL = "board";
        static final String IS_FAVOURITE = "is_favourite";
        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                USER_URL + " TEXT NOT NULL, " +
                BOARD_URL + " TEXT NOT NULL, " +
                IS_FAVOURITE + " INT NOT NULL, " +
                "FOREIGN KEY(" + BOARD_URL + ") REFERENCES " + BoardData.TABLE_NAME + "(" + BoardData.URL + "), " +
                "FOREIGN KEY(" + USER_URL + ") REFERENCES " + Account.TABLE_NAME + " (" + Account.URL + "), " +
                "PRIMARY KEY ( " + USER_URL + ", " + BOARD_URL + ")" +
                ");";

        static final String SELECT_BOARDS_FOLLOWED_BY = "SELECT A.*, B." + FollowedBoards.IS_FAVOURITE + " FROM " + BoardData.TABLE_NAME +
                " A JOIN " + FollowedBoards.TABLE_NAME +
                " B ON A." + BoardData.URL + "=B." + FollowedBoards.BOARD_URL + " AND B." +
                FollowedBoards.USER_URL + "=? ORDER BY A." + BoardData.NAME;
    }

    static abstract class Account {
        static final String TABLE_NAME = "account_data";
        static final String NAME = "name";
        static final String URL = "url";
        static final String LAST_SYNC_TIME = "last_sync_time";
        static final String TIME_REGISTERED = "time_registered";
        static final String EMAIL = "email";
        static final String GENDER = "gender";
        static final String DISPLAY_PHOTO = "display_photo";
        static final String LOCATION = "location";
        static final String PERSONAL_TEXT = "personal_text";
        static final String SIGNATURE = "signature";
        static final String BIRTH_DATE = "birthdate";

        static final String DISPLAY_PHOTO_SET = "display_set";
        static final String USERS_FOLLOWING = "users_following";
        static final String FOLLOWERS = "followers";

        static final String TIME_SPENT_ONLINE = "time_spent_online";
        static final String LAST_SEEN = "last_seen";
        static final String TWITTER = "twitter";
        static final String YIM = "yim";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                NAME + " TEXT NOT NULL UNIQUE , " +
                URL + " TEXT PRIMARY KEY, " +
                TIME_REGISTERED + " INT DEFAULT 0, " +
                LAST_SYNC_TIME + " TEXT, " +
                EMAIL + " TEXT, " +
                SIGNATURE + " TEXT, " +
                PERSONAL_TEXT + " TEXT, " +
                BIRTH_DATE + " TEXT, " +
                GENDER + " TEXT, " +
                TIME_SPENT_ONLINE + " TEXT, " +
                TWITTER + " TEXT, " +
                YIM + " TEXT, " +
                DISPLAY_PHOTO + " BLOB, " +
                LAST_SEEN + " INT, " +
                DISPLAY_PHOTO_SET + " INT, " +
                USERS_FOLLOWING + " TEXT, " +
                FOLLOWERS + " TEXT, " +
                LOCATION + " TEXT);";


        // For Migration
        static final String TABLE_TEMP = "temp_account";
        static final String CREATE_TABLE_TEMP = "CREATE TABLE " + TABLE_TEMP + " (" +
                NAME + " TEXT NOT NULL UNIQUE , " +
                URL + " TEXT PRIMARY KEY, " +
                TIME_REGISTERED + " INT DEFAULT 0, " +
                EMAIL + " TEXT, " +
                SIGNATURE + " TEXT, " +
                PERSONAL_TEXT + " TEXT, " +
                GENDER + " TEXT, " +
                TIME_SPENT_ONLINE + " TEXT, " +
                TWITTER + " TEXT, " +
                YIM + " TEXT, " +
                DISPLAY_PHOTO + " BLOB, " +
                LAST_SEEN + " INT, " +
                DISPLAY_PHOTO_SET + " INT, " +
                USERS_FOLLOWING + " TEXT, " +
                FOLLOWERS + " TEXT, " +
                LOCATION + " TEXT);";
        static final String SELECT_EXCEPT_LAST_SYNC_AND_BIRTHDATE = "SELECT " +
                NAME + ", " +
                URL + ", " +
                TIME_REGISTERED + ", " +
                EMAIL + ", " +
                SIGNATURE + ", " +
                PERSONAL_TEXT + ", " +
                GENDER + ", " +
                TIME_SPENT_ONLINE + ", " +
                TWITTER + ", " +
                YIM + ", " +
                DISPLAY_PHOTO + ", " +
                LAST_SEEN + ", " +
                DISPLAY_PHOTO_SET + ", " +
                USERS_FOLLOWING + ", " +
                FOLLOWERS + ", " +
                LOCATION + " FROM " + TABLE_NAME;

        static final String SELECT_USER = "SELECT * FROM " + TABLE_NAME + " WHERE " + URL + "=? COLLATE NOCASE;";
        static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
        static final String SELECT_USER_FRIENDS = "SELECT " + USERS_FOLLOWING + " FROM " + TABLE_NAME + " WHERE " + URL + "=? COLLATE NOCASE;";
    }

    static abstract class RecentSearch {
        static final String TABLE_NAME = "recent_search";

        static final String KEY = "search_key";
        static final String TIME = "timestamp";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                KEY + " TEXT PRIMARY KEY, " +
                TIME + " REAL NOT NULL " +
                ");";


        static final String SELECT_N_RECENT = "SELECT " + KEY + " FROM " + TABLE_NAME + " ORDER BY " + TIME + " DESC LIMIT ?";
        static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + TIME;

    }

    static abstract class RecentBoards {
        static final String TABLE_NAME = "recent_boards";

        static final String BOARD_URL = "board";
        static final String TIME = "timestamp";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                BOARD_URL + " TEXT PRIMARY KEY, " +
                TIME + " REAL NOT NULL, " +
                "FOREIGN KEY(" + BOARD_URL + ") REFERENCES " + BoardData.TABLE_NAME + "(" + BoardData.URL + ") " +
                ");";

        static final String SELECT_N_RECENT = "SELECT " + BoardData.TABLE_NAME + ".* FROM " +
                BoardData.TABLE_NAME + " JOIN " + TABLE_NAME +
                " ON " + TABLE_NAME + "." + BOARD_URL + "=" + BoardData.TABLE_NAME + "." + BoardData.URL +
                " ORDER BY " + TABLE_NAME + "." + TIME + " DESC LIMIT ?";
    }

    private static abstract class Posts {
        static final String TABLE_NAME = "posts";

        static final String ID = "id";
        static final String AUTHOR_NAME = "author";
        static final String URL = "url";
        static final String LIKES = "likes";
        static final String SHARES = "shares";
    }


    static abstract class Users {
        static final String TABLE_NAME = "users";

        static final String NAME = "name";
        static final String URL = "url";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                NAME + " TEXT UNIQUE, " +
                URL + " TEXT PRIMARY KEY );";

        static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }

    static abstract class Followers {
        static final String TABLE_NAME = "followers";

        static final String USER = "name";
        static final String FOLLOWER = "follower";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                USER + " TEXT NOT NULL, " +
                FOLLOWER + " TEXT NOT NULL);" +
                "FOREIGN KEY(" + USER + ") REFERENCES " + Account.TABLE_NAME + "(" + Account.URL + ") " +
                "FOREIGN KEY(" + FOLLOWER + ") REFERENCES " + Users.TABLE_NAME + "(" + Users.URL + ") " +
                "PRIMARY KEY ( " + USER + ", " + FOLLOWER + ")" +
                ");";
    }
}