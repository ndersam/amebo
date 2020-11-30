package com.amebo.core.migration.old;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class User implements Comparable<User> {

    String userId;
    String name;
    String url;
    String email = "";
    String gender = "";
    String location = "";
    String personalText = "";
    String signature = "";
    long birthDate = 0;
    long lastSeen = 0;
    String timeSpentOnline = "";
    long timeRegistered = 0;
    String twitter = "";
    String yim = "";
    int topicsCount = 0;
    int postsCount = 0;
    boolean isFollowed;
    String photoUrl = null;
    int imageLikes = 0;
    byte[] photo = null;
    boolean photoIsLiked;
    private
    List<RegularBoard> forumsAsModerator = new ArrayList<>();
    private
    List<RegularBoard> sectionsMostActiveIn = new ArrayList<>();
    private
    List<User> friends = new ArrayList<>();
    private
    List<Topic> latestTopics = new ArrayList<>();

    public User() {
        // Required for Parcel
    }

    public User(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public static Comparator<User> comparator() {
        return (a, b) -> a.name.compareToIgnoreCase(b.name);
    }

    public static Comparator<User> comparatorReverse() {
        return (a, b) -> b.name.compareToIgnoreCase(a.name);
    }

    public User copy() {
        return new User(this.name, this.url);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Topic> getLatestTopics() {
        return latestTopics;
    }

    public void setLatestTopics(List<Topic> latestTopics) {
        this.latestTopics = latestTopics;
    }

    public List<RegularBoard> getBoardModeratesIn() {
        return forumsAsModerator;
    }

    public int getImageLikes() {
        return imageLikes;
    }

    public void setImageLikes(int imageLikes) {
        this.imageLikes = imageLikes;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void boardsModeratesIn(List<RegularBoard> boardList) {
        forumsAsModerator = boardList;
    }

    public List<RegularBoard> getSectionsMostActiveIn() {
        return sectionsMostActiveIn;
    }

    public void setSectionsMostActiveIn(List<RegularBoard> board) {
        sectionsMostActiveIn = board;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimeRegistered() {
        return timeRegistered;
    }

    public void setTimeRegistered(long timeRegistered) {
        this.timeRegistered = timeRegistered;
    }

    public String getTimeSpentOnline() {
        return timeSpentOnline;
    }

    public void setTimeSpentOnline(String timeSpentOnline) {
        this.timeSpentOnline = timeSpentOnline;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getPersonalText() {
        return personalText;
    }

    public void setPersonalText(String personalText) {
        this.personalText = personalText;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getYim() {
        return yim;
    }

    public void setYim(String yim) {
        this.yim = yim;
    }

    public void setFriendsList(List<User> friends) {
        this.friends = friends;
    }

    public int getTopicsCount() {
        return topicsCount;
    }

    public void setTopicsCount(int topicsCount) {
        this.topicsCount = topicsCount;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(long birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isPhotoIsLiked() {
        return photoIsLiked;
    }

    public void setPhotoIsLiked(boolean photoIsLiked) {
        this.photoIsLiked = photoIsLiked;
    }

    public List<User> getFriends() {
        return friends;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof User)
            return name.equalsIgnoreCase(((User) obj).name);
        return false;
    }

    @Override
    public int compareTo(@NonNull User another) {
        return name.compareTo(another.name);
    }

    @NonNull
    @Override
    public String toString() {
        return name + "<" + url + ">";
    }

    public void clearAll() {
        email = "";
        gender = "";
        location = "";
        personalText = "";
        signature = "";
        birthDate = 0;
        lastSeen = 0;
        timeSpentOnline = "";
        timeRegistered = 0;
        twitter = "";
        yim = "";
        forumsAsModerator.clear();
        sectionsMostActiveIn.clear();
        friends.clear();
        topicsCount = 0;
        postsCount = 0;
        latestTopics.clear();
        isFollowed = false;
        photoUrl = null;
        imageLikes = 0;
        photo = null;
        photoIsLiked = false;
    }

}