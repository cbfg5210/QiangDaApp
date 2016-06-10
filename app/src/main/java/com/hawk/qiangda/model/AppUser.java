package com.hawk.qiangda.model;

import cn.bmob.v3.BmobObject;

/**
 * 在此写用途
 * Created by hawk on 2016/3/29.
 */
public class AppUser extends BmobObject{
    private String nickName;
    private String deviceId;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
