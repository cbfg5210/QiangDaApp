package com.hawk.qiangda.model;

import cn.bmob.v3.BmobObject;

/**
 * 在此写用途
 * Created by hawk on 2016/4/1.
 */
public class Room extends BmobObject{
    private String name;
    private boolean isStarted;
    private Integer currentNum;
    private boolean isDoing;
    private AppUser currentUser;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public Integer getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(Integer currentNum) {
        this.currentNum = currentNum;
    }

    public boolean isDoing() {
        return isDoing;
    }

    public void setDoing(boolean doing) {
        isDoing = doing;
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(AppUser currentUser) {
        this.currentUser = currentUser;
    }
}
