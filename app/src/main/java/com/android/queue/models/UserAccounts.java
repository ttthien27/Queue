package com.android.queue.models;

public class UserAccounts {
    public String fullName;
    public String phone;
    public String password;
    public Long createDate;
    public Boolean isLogin;
    public Boolean isHost;
    public String currentRoomId;

    public UserAccounts() {
    }

    public UserAccounts(String fullName, String phone, String password) {
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.createDate = System.currentTimeMillis() / 1000;
        this.isLogin = false;
        this.isHost = false;
    }

    public UserAccounts(String fullName, String phone, String password
                        , Boolean isHost, String currentRoomId) {
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.createDate = System.currentTimeMillis() / 1000;
        this.isHost = isHost;
        this.currentRoomId = currentRoomId;
    }

    public Boolean getLogin() {
        return isLogin;
    }

    public void setLogin(Boolean login) {
        isLogin = login;
    }

    public Boolean getHost() {
        return isHost;
    }

    public void setHost(Boolean host) {
        isHost = host;
    }
}
