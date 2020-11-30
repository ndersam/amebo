package com.amebo.core.migration.old;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Topic {

    private final
    List<RegularBoard> boards = new ArrayList<>();
    String id;
    String title;
    String url;
    int linkedPage;
    long timestamp; // time of most recent post or so
    int postCount;
    User author = null;
    boolean isClosed = false;
    int views;
    boolean hasNewPosts;
    RegularBoard mainBoard = null;

    boolean isFollowing = false;
    boolean hidden = false;

    public Topic() {

    }

    public Topic(String title, String id, String url, long timestamp) {
        this.title = title;
        this.id = id;
        this.url = url;
        this.timestamp = timestamp;
    }

    public Topic(String title, String id, String url) {
        this(title, id, url, 0);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getLinkedPage() {
        return linkedPage;
    }

    public Topic setLinkedPage(int page) {
        linkedPage = page;
        return this;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public String getUrl() {
        return url;
    }

    @NonNull
    @Override
    public String toString() {
        return "( " + this.title + ", " + this.url + " )";
    }

    public int getViewCount() {
        return views;
    }

    public void setViewCount(int viewCount) {
        this.views = viewCount;
    }

    public void addBoard(RegularBoard board) {
        boards.add(board);
    }

    public RegularBoard getMainBoard() {
        if (mainBoard == null && boards.size() > 0)
            mainBoard = boards.get(0);
        return mainBoard;
    }

    public void setMainBoard(RegularBoard board) {
        mainBoard = board;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int count) {
        postCount = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean equals(Topic another) {
        return id.equals(another.id);
    }

    public List<RegularBoard> getBoards() {
        return boards;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean hasNewPosts() {
        return hasNewPosts;
    }

    public void setHasNewPosts(boolean hasNewPosts) {
        this.hasNewPosts = hasNewPosts;
    }
}