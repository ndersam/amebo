package com.amebo.core.migration.old;

import androidx.annotation.NonNull;

import java.util.List;

public class RegularBoard extends Board implements Comparable<RegularBoard> {

    List<User> moderators = null;
    int guestsViewing;
    int usersViewing;
    int boardNo = -1;
    String relatedBoardsHTML = "";
    boolean hasRelatedBoards = false;
    boolean isFavourite = false;
    boolean isFollowing = false;
    List<User> usersViewingList = null;

    public RegularBoard() {

    }

    public RegularBoard(String name, String url) {
        super(name, url, TYPE.REGULAR);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    @Override
    public boolean isRegular() {
        return true;
    }

    public int getBoardNo() {
        return boardNo;
    }

    public int getGuestsViewing() {
        return guestsViewing;
    }

    public void setGuestsViewing(int guestsViewing) {
        this.guestsViewing = guestsViewing;
    }

    public int getUsersViewing() {
        return usersViewing;
    }

    public void setUsersViewing(int usersViewing) {
        this.usersViewing = usersViewing;
    }

    public List<User> getModerators() {
        return moderators;
    }

    public void setModerators(List<User> moderators) {
        this.moderators = moderators;
    }

    public void setRelatedBoardHTML(String html) {
        hasRelatedBoards = true;
        relatedBoardsHTML = html;
    }

    public String getRelatedBoardsHtml() {
        return relatedBoardsHTML;
    }

    public boolean hasRelatedBoards() {
        return hasRelatedBoards;
    }

    public RegularBoard withNumber(int boardNo) {
        this.boardNo = boardNo;
        return this;
    }

    @Override
    public int compareTo(@NonNull RegularBoard another) {
        return getName().compareToIgnoreCase(another.getName());
    }

    public List<User> getUsersViewingList() {
        return usersViewingList;
    }

    public void setUsersViewingList(List<User> usersViewingList) {
        this.usersViewingList = usersViewingList;
    }
}