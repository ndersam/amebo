package com.amebo.core.migration.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.amebo.core.migration.old.Account;
import com.amebo.core.migration.old.RegularBoard;
import com.amebo.core.migration.old.User;
import com.amebo.core.migration.old.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amebo.core.migration.data.Schema.DATABASE_NAME;
import static com.amebo.core.migration.data.Schema.DATABASE_VERSION;

public class DatabaseManager extends SQLiteOpenHelper {

    private static DatabaseManager sInstance;

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DatabaseManager.class) {
                if (sInstance == null) {
                    sInstance = new DatabaseManager(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        boolean isNotMigration = db.getVersion() == DATABASE_VERSION;
        db.setForeignKeyConstraintsEnabled(isNotMigration);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Schema.Account.CREATE_TABLE);
        db.execSQL(Schema.BoardData.CREATE_TABLE);
        db.execSQL(Schema.Topics.CREATE_TABLE);
        db.execSQL(Schema.ViewedTopics.CREATE_TABLE);
        db.execSQL(Schema.FollowedBoards.CREATE_TABLE);
        db.execSQL(Schema.RecentBoards.CREATE_TABLE);
        db.execSQL(Schema.RecentSearch.CREATE_TABLE);
        db.execSQL(Schema.Users.CREATE_TABLE);
        db.execSQL(Schema.Followers.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            migrateOneToTwo(db);
        }
    }


    private void migrateOneToTwo(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL("PRAGMA foreign_keys=off;");
        db.execSQL(Schema.Account.CREATE_TABLE_TEMP);
        db.execSQL("INSERT INTO " + Schema.Account.TABLE_TEMP + " " + Schema.Account.SELECT_EXCEPT_LAST_SYNC_AND_BIRTHDATE + ";");
        db.execSQL("DROP TABLE " + Schema.Account.TABLE_NAME + ";");
        db.execSQL("ALTER TABLE " + Schema.Account.TABLE_TEMP + " RENAME TO " + Schema.Account.TABLE_NAME + ";");
        db.execSQL("ALTER TABLE " + Schema.Account.TABLE_NAME + " ADD " + Schema.Account.LAST_SYNC_TIME + " TEXT DEFAULT \"" + System.currentTimeMillis() + "\";");
        db.execSQL("ALTER TABLE " + Schema.Account.TABLE_NAME + " ADD " + Schema.Account.BIRTH_DATE + " TEXT DEFAULT \"-1\";");
        db.execSQL("PRAGMA foreign_keys=on;");
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void saveViewedTopics(String topicId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Schema.ViewedTopics.ID, topicId);
        db.beginTransaction();
        long rows = db.insert(Schema.ViewedTopics.TABLE_NAME,
                Schema.ViewedTopics.ID, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteViewedTopics() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(Schema.ViewedTopics.TABLE_NAME, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public Map<String, String> getViewedTopics() {
        Map<String, String> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.ViewedTopics.SELECT_ALL, null);
        while (c.moveToNext()) {
            int idIndex = c.getColumnIndex(Schema.ViewedTopics.ID);
            map.put(c.getString(idIndex), c.getString(idIndex));
        }
        c.close();
        return map;
    }

    public Account getAccount(String activeUser) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.Account.SELECT_USER, new String[]{activeUser.toLowerCase().trim()});
        if (c.moveToNext()) {
            return _getAccount(c);
        }
        return null;
    }

    private Account _getAccount(Cursor c) {

        int birthDateIdx = c.getColumnIndex(Schema.Account.BIRTH_DATE);
        int displayPhotoIdx = c.getColumnIndex(Schema.Account.DISPLAY_PHOTO);
        int emailIdx = c.getColumnIndex(Schema.Account.EMAIL);
        int genderIdx = c.getColumnIndex(Schema.Account.GENDER);
        int lastSeenIdx = c.getColumnIndex(Schema.Account.LAST_SEEN);
        int lastSyncTimeIdx = c.getColumnIndex(Schema.Account.LAST_SYNC_TIME);
        int locationIdx = c.getColumnIndex(Schema.Account.LOCATION);
        int nameIdx = c.getColumnIndex(Schema.Account.NAME);
        int personalTextIdx = c.getColumnIndex(Schema.Account.PERSONAL_TEXT);
        int signatureIdx = c.getColumnIndex(Schema.Account.SIGNATURE);
        int timeRegisteredIdx = c.getColumnIndex(Schema.Account.TIME_REGISTERED);
        int timeSpentOnlineIdx = c.getColumnIndex(Schema.Account.TIME_SPENT_ONLINE);
        int twitterIdx = c.getColumnIndex(Schema.Account.TWITTER);
        int urlIdx = c.getColumnIndex(Schema.Account.URL);
        int yimIdx = c.getColumnIndex(Schema.Account.YIM);
        int isDisplayPhotoSet = c.getColumnIndex(Schema.Account.DISPLAY_PHOTO_SET);
        int followingIdx = c.getColumnIndex(Schema.Account.USERS_FOLLOWING);
        int followersIdx = c.getColumnIndex(Schema.Account.FOLLOWERS);

        User user = new User(c.getString(nameIdx), c.getString(urlIdx));
        String birthDate = c.getString(birthDateIdx);
        user.setBirthDate(birthDate == null ? -1 : Long.parseLong(birthDate));
        user.setPhoto(c.getBlob(displayPhotoIdx));
        user.setEmail(c.getString(emailIdx));
        user.setGender(c.getString(genderIdx));
        user.setLastSeen(c.getInt(lastSeenIdx));
        user.setLocation(c.getString(locationIdx));
        user.setPersonalText(c.getString(personalTextIdx));
        user.setSignature(c.getString(signatureIdx));
        user.setTimeRegistered(c.getInt(timeRegisteredIdx));
        user.setTimeSpentOnline(c.getString(timeSpentOnlineIdx));
        user.setTwitter(c.getString(twitterIdx));
        user.setYim(c.getString(yimIdx));
        user.setFriendsList(Utils.userListFromString(c.getString(followingIdx)));

        Account account = new Account(user.getName());
        account.setUser(user);
        account.setLastSyncTime(Long.parseLong(c.getString(lastSyncTimeIdx)));
        account.setHasImage(isDisplayPhotoSet == 1);
        account.setFollowers(Utils.userListFromString(c.getString(followersIdx)));
        return account;
    }


    private User _getAccountMinimal(Cursor c) {
        int nameIdx = c.getColumnIndex(Schema.Account.NAME);
        int urlIdx = c.getColumnIndex(Schema.Account.URL);
        return new User(c.getString(nameIdx), c.getString(urlIdx));
    }


    /**
     * @param current String name of currently active user.
     * @return A {@link Pair} of currently active {@link Account} and {@link List<User>} of all
     * accounts added to app.
     */
    public Pair<Account, List<User>> fetchAccounts(String current) {
        Account account = null;
        List<User> all = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.Account.SELECT_USER, new String[]{current.toLowerCase()});
        if (c.moveToNext()) {
            account = _getAccount(c);
        }
        c = db.rawQuery(Schema.Account.SELECT_ALL, null);
        while (c.moveToNext()) {
            all.add(_getAccountMinimal(c));
        }
        return new Pair<>(account, all);
    }

    public List<User> fetchAccounts() {
        List<User> all = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.Account.SELECT_ALL, null);
        while (c.moveToNext()) {
            all.add(_getAccountMinimal(c));
        }
        return all;
    }

    public void saveAccount(Account account) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        _saveAccount(db, account);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void _saveAccount(SQLiteDatabase db, Account account) {
        ContentValues values = new ContentValues();
        User user = account.getUser();
        values.put(Schema.Account.BIRTH_DATE, user.getBirthDate());
        values.put(Schema.Account.DISPLAY_PHOTO, user.getPhoto());
        values.put(Schema.Account.EMAIL, user.getEmail());
        values.put(Schema.Account.GENDER, user.getGender());
        values.put(Schema.Account.LAST_SEEN, user.getLastSeen());
        values.put(Schema.Account.LAST_SYNC_TIME, String.valueOf(account.getLastSyncTime()));
        values.put(Schema.Account.LOCATION, user.getLocation());
        values.put(Schema.Account.NAME, user.getName());
        values.put(Schema.Account.PERSONAL_TEXT, user.getPersonalText());
        values.put(Schema.Account.SIGNATURE, user.getSignature());
        values.put(Schema.Account.TIME_REGISTERED, user.getTimeRegistered());
        values.put(Schema.Account.TIME_SPENT_ONLINE, user.getTimeSpentOnline());
        values.put(Schema.Account.TWITTER, user.getTwitter());
        values.put(Schema.Account.DISPLAY_PHOTO_SET, account.isHasImage() ? 1 : 0);
        values.put(Schema.Account.USERS_FOLLOWING, Utils.userListToString(user.getFriends()));
        values.put(Schema.Account.FOLLOWERS, Utils.userListToString(account.getFollowers()));

        String url = user.getUrl().toLowerCase().trim();
        int idx = url.lastIndexOf("/");
        if (idx != -1) {
            url = url.substring(idx + 1);
        }
        values.put(Schema.Account.URL, url);
        values.put(Schema.Account.YIM, user.getYim());

        db.insertWithOnConflict(Schema.Account.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void saveFollowedBoard(String username, RegularBoard board) {
        ContentValues values = new ContentValues();
        values.put(Schema.FollowedBoards.USER_URL, username.toLowerCase());
        String boardUrl = board.getUrl();
        if (boardUrl.lastIndexOf("/") != -1) {
            boardUrl = boardUrl.substring(boardUrl.lastIndexOf("/") + 1);
        }
        values.put(Schema.FollowedBoards.BOARD_URL, boardUrl);
        values.put(Schema.FollowedBoards.IS_FAVOURITE, board.isFavourite() ? 1 : 0);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.insertWithOnConflict(Schema.FollowedBoards.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void removeFollowedBoard(String username, RegularBoard board) {
        String boardUrl = board.getUrl();
        if (boardUrl.lastIndexOf("/") != -1) {
            boardUrl = boardUrl.substring(boardUrl.lastIndexOf("/") + 1);
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String whereClause = Schema.FollowedBoards.USER_URL + "= ? AND " + Schema.FollowedBoards.BOARD_URL + " = ?";
        db.delete(Schema.FollowedBoards.TABLE_NAME, whereClause, new String[]{username.toLowerCase(), boardUrl});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void saveFollowedBoards(String username, List<RegularBoard> boards) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        _saveFollowedBoards(db, username, boards);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void _saveFollowedBoards(SQLiteDatabase db, String username, List<RegularBoard> boards) {
        //////////////
        // Delete boards not is list
        //////////////////
        String[] args = new String[boards.size() + 1];
        args[0] = username.toLowerCase();
        int j = 1;
        StringBuilder urlHolder = new StringBuilder();
        for (RegularBoard board : boards) {
            String boardUrl = board.getUrl();
            if (boardUrl.lastIndexOf("/") != -1) {
                boardUrl = boardUrl.substring(boardUrl.lastIndexOf("/") + 1);
            }
            urlHolder.append("?");
            if (j < args.length - 1)
                urlHolder.append(", ");
            args[j++] = boardUrl;
        }
        String whereClause = Schema.FollowedBoards.USER_URL + "= ? AND " + Schema.FollowedBoards.BOARD_URL + " NOT IN (" + urlHolder.toString() + ")";
        db.delete(Schema.FollowedBoards.TABLE_NAME, whereClause, args);

        //////////////////////////
        // Insert new boards
        /////////////////////////
        ContentValues values = new ContentValues();
        for (int idx = 0; idx < boards.size(); idx++) {
            RegularBoard board = boards.get(idx);
            values.put(Schema.FollowedBoards.USER_URL, username.toLowerCase());
            String boardUrl = board.getUrl();
            if (boardUrl.lastIndexOf("/") != -1) {
                boardUrl = boardUrl.substring(boardUrl.lastIndexOf("/") + 1);
            }
            values.put(Schema.FollowedBoards.BOARD_URL, boardUrl);
            values.put(Schema.FollowedBoards.IS_FAVOURITE, board.isFavourite() ? 1 : 0);
            db.insertWithOnConflict(Schema.FollowedBoards.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public List<RegularBoard> getFollowedBoards(String username) {
        List<RegularBoard> boards = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.FollowedBoards.SELECT_BOARDS_FOLLOWED_BY, new String[]{username.toLowerCase()});
        while (c.moveToNext()) {
            int nameIndex = c.getColumnIndex(Schema.BoardData.NAME);
            int urlIndex = c.getColumnIndex(Schema.BoardData.URL);
            int boardNoIndex = c.getColumnIndex(Schema.BoardData.BOARD_NO);
            int favouriteIndex = c.getColumnIndex(Schema.FollowedBoards.IS_FAVOURITE);
            RegularBoard board = new RegularBoard(c.getString(nameIndex), c.getString(urlIndex)).withNumber(c.getInt(boardNoIndex));
            board.setFavourite(c.getInt(favouriteIndex) == 1);
            boards.add(board);
        }
        c.close();
        return boards;
    }

    public List<RegularBoard> getAllNairalandBoards() {
        List<RegularBoard> boards = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.BoardData.SELECT_ALL, null);
        while (c.moveToNext()) {
            int nameIndex = c.getColumnIndex(Schema.BoardData.NAME);
            int urlIndex = c.getColumnIndex(Schema.BoardData.URL);
            int boardNoIndex = c.getColumnIndex(Schema.BoardData.BOARD_NO);
            RegularBoard board = new RegularBoard(c.getString(nameIndex), c.getString(urlIndex)).withNumber(c.getInt(boardNoIndex));
            boards.add(board);
        }
        c.close();
        return boards;
    }

    public Cursor findNairalandBoardCursor(String query) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(Schema.BoardData.FIND_LIKE, new String[]{"%" + query + "%"});
    }

    public List<RegularBoard> findNairalandBoard(String query) {
        List<RegularBoard> boards = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Schema.BoardData.FIND_LIKE, new String[]{"%" + query + "%"});
        while (c.moveToNext()) {
            int nameIndex = c.getColumnIndex(Schema.BoardData.NAME);
            int urlIndex = c.getColumnIndex(Schema.BoardData.URL);
            int boardNoIndex = c.getColumnIndex(Schema.BoardData.BOARD_NO);
            RegularBoard board = new RegularBoard(c.getString(nameIndex), c.getString(urlIndex)).withNumber(c.getInt(boardNoIndex));
            boards.add(board);
        }
        c.close();
        return boards;
    }

    public void saveBoards(List<RegularBoard> boards) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        for (int idx = 0; idx < boards.size(); idx++) {
            RegularBoard board = boards.get(idx);
            values.put(Schema.BoardData.NAME, board.getName());
            String url = board.getUrl().trim().toLowerCase();
            int slash = url.lastIndexOf("/");
            if (slash != -1) {
                url = url.substring(slash + 1);
            }
            values.put(Schema.BoardData.URL, url);
            values.put(Schema.BoardData.BOARD_NO, board.getBoardNo());
            db.insert(Schema.BoardData.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteUserData(String userName) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(Schema.FollowedBoards.TABLE_NAME, Schema.FollowedBoards.USER_URL + "= ?", new String[]{userName.toLowerCase()});
        db.delete(Schema.Account.TABLE_NAME, Schema.Account.NAME + "= ?", new String[]{userName});
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void saveRecentSearch(String query) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(Schema.RecentSearch.KEY, query);
        values.put(Schema.RecentSearch.TIME, System.currentTimeMillis());
        db.insertWithOnConflict(Schema.RecentSearch.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<String> getMostRecentSearch(int count) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        Cursor c = db.rawQuery(Schema.RecentSearch.SELECT_N_RECENT, new String[]{count + ""});
        while (c.moveToNext()) {
            String query = c.getString(c.getColumnIndex(Schema.RecentSearch.KEY));
            list.add(query);
        }
        c.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return list;
    }

    public List<Pair<String, Double>> getRecentSearch() {
        List<Pair<String, Double>> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        Cursor c = db.rawQuery(Schema.RecentSearch.SELECT_ALL, null);
        while (c.moveToNext()) {
            String query = c.getString(c.getColumnIndex(Schema.RecentSearch.KEY));
            Double time = c.getDouble(c.getColumnIndex(Schema.RecentSearch.TIME));
            list.add(new Pair<>(query, time));
        }
        c.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return list;
    }

    public void clearRecentSearches() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(Schema.RecentSearch.TABLE_NAME, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void clearRecentSearch(String query) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(Schema.RecentSearch.TABLE_NAME, Schema.RecentSearch.KEY + "=?", new String[]{query});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void clearRecentBoards() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(Schema.RecentBoards.TABLE_NAME, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void saveRecentBoard(RegularBoard board) {
        ContentValues values = new ContentValues();
        String boardUrl = board.getUrl();
        if (boardUrl.lastIndexOf("/") != -1) {
            boardUrl = boardUrl.substring(boardUrl.lastIndexOf("/") + 1);
        }
        values.put(Schema.RecentBoards.BOARD_URL, boardUrl);
        values.put(Schema.RecentBoards.TIME, System.currentTimeMillis());

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
//        Logger.getLogger().d(values.toString());
        db.insertWithOnConflict(Schema.RecentBoards.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public List<RegularBoard> getMostRecentBoards(int count) {
        List<RegularBoard> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        Cursor c = db.rawQuery(Schema.RecentBoards.SELECT_N_RECENT, new String[]{count + ""});
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(Schema.BoardData.NAME));
            String url = c.getString(c.getColumnIndex(Schema.BoardData.URL));
            int board_no = c.getInt(c.getColumnIndex(Schema.BoardData.BOARD_NO));
            list.add(new RegularBoard(name, url).withNumber(board_no));
        }
        c.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return list;
    }

    private void _saveFollowers(String userName, List<User> followers) {
        userName = userName.toLowerCase();

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(Schema.Followers.TABLE_NAME, Schema.Followers.USER + "=?", new String[]{userName});

        for (User follower : followers) {
            String url = follower.getUrl().startsWith("/") ? follower.getUrl().substring(1) : follower.getUrl();

            ContentValues valuesNewUser = new ContentValues();
            valuesNewUser.put(Schema.Users.NAME, follower.getName());
            valuesNewUser.put(Schema.Users.URL, url);
            db.insertWithOnConflict(Schema.Users.TABLE_NAME, null, valuesNewUser, SQLiteDatabase.CONFLICT_IGNORE);

            ContentValues values = new ContentValues();
            values.put(Schema.Followers.USER, userName);
            values.put(Schema.Followers.FOLLOWER, url);
            db.insertWithOnConflict(Schema.Followers.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void updateAccount(Account account, List<RegularBoard> boards) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        _saveAccount(db, account);
        _saveFollowedBoards(db, account.getName(), boards);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void updateFriendsList(String userName, User user, boolean added) {
        userName = userName.toLowerCase().trim();
        List<User> following;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        Cursor c = db.rawQuery(Schema.Account.SELECT_USER_FRIENDS, new String[]{userName});
        if (c.moveToNext()) {
            following = Utils.userListFromString(c.getString(c.getColumnIndex(Schema.Account.USERS_FOLLOWING)));
        } else {
            throw new IllegalStateException("User wasn't found... strange!");
        }
        c.close();

        if (added) {
            following.add(user);
        } else {
            User del = null;
            for (User u : following) {
                if (user.equals(u)) {
                    del = u;
                    break;
                }
            }
            following.remove(del);
        }
        ContentValues values = new ContentValues();
        values.put(Schema.Account.USERS_FOLLOWING, Utils.userListToString(following));
        db.update(Schema.Account.TABLE_NAME, values, Schema.Account.URL + "=?", new String[]{userName});

        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public int fetchBoardNumber(RegularBoard board) {
        String url = board.getUrl();
        int idx = url.lastIndexOf("/");
        if (idx != -1) {
            url = url.substring(idx + 1);
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + Schema.BoardData.BOARD_NO + " FROM " + Schema.BoardData.TABLE_NAME + " WHERE " + Schema.BoardData.URL + "=" + url, null);
        if (c.moveToNext()) {
            // idx is being reused here
            idx = c.getInt(c.getColumnIndex(Schema.BoardData.BOARD_NO));
        } else {
            throw new IllegalArgumentException("Unable to find board number");
        }
        c.close();
        return idx;
    }
}