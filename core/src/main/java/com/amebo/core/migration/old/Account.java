package com.amebo.core.migration.old;


import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public class Account {

    private static Account guest;

    String name;
    User user;
    List<User> followers = Collections.emptyList();
    boolean hasImage;
    long lastSyncTime;
    List<SyncError> syncErrors = Collections.emptyList();

    boolean is_guest;


    public Account() {
        // Required for Parcel
    }

    public Account(String name) {
        this.name = name;
        this.is_guest = name.equalsIgnoreCase(Consts.DOLLAR);
    }

    public static Account guestInstance() {
        if (guest == null) {
            guest = new Account(Consts.DOLLAR);
            guest.is_guest = true;
            guest.user = new User(Consts.DOLLAR, Consts.DOLLAR);
        }
        return guest;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public String displayName() {
        return is_guest ? Consts.DEFAULT_USER : name;
    }

    public boolean isGuest() {
        return is_guest;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public boolean equals(@NonNull Account another) {
        return name.equalsIgnoreCase(another.name) && another.is_guest == is_guest;
    }
}