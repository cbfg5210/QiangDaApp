package com.hawk.qiangda.model;

/**
 * 在此写用途
 * Created by hawk on 2016/4/5.
 */
public class UserStatus {
    private String nickName;
    private int status;
    private String objectId;

    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
