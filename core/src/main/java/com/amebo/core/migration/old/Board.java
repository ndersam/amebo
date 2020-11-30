package com.amebo.core.migration.old;

import androidx.annotation.NonNull;

public abstract class Board {

    String name;
    String url;
    TYPE type;
    String info = "";


    Board() {

    }


    Board(String name, String url, TYPE type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public static Board.SORT[] regularBoardSortOptions() {
        return new Board.SORT[]{
                SORT.NEW,
                SORT.POSTS,
                SORT.UPDATED,
                SORT.VIEWS
        };
    }

    public boolean isFollowedBoards() {
        return false;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public TYPE getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean equals(Board another) {
        return url.equalsIgnoreCase(another.url);
    }

    public boolean isRegular() {
        return false;
    }

    public boolean isTrending() {
        return false;
    }

    public boolean isFeatured() {
        return false;
    }

    public boolean isNew() {
        return false;
    }

    public boolean isUserTopicList() {
        return false;
    }

    public boolean isFollowedTopics() {
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " " + url;
    }

    public enum TYPE {TRENDING, NEW, USER_TOPICS, REGULAR, FEATURED, FOLLOWED_BOARDS, FOLLOWED_TOPICS}


    public enum SORT {

        NEW("new", "New"), POSTS("posts", "Posts"), UPDATED("", "Updated"), VIEWS("views", "Views"),
        CREATION_TIME("creationtime", "Creation Time"), UPDATE_TIME("updatetime", "Update Time");

        private final String urlPath;
        private final String name;

        SORT(String urlPath, String name) {
            this.urlPath = urlPath;
            this.name = name;
        }

        public static Board.SORT parse(String string) {
            return valueOf(string.toUpperCase());
        }

        public String getName() {
            return name;
        }

        @NonNull
        @Override
        public String toString() {
            return name + " <" + urlPath + ">";
        }

        public String getValue() {
            return urlPath;
        }
    }
}