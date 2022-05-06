package com.example.smb;

import android.graphics.drawable.Drawable;

public class Data {
    private String title;
    private String ip;
    private String id;
    private String pass;
    private int icon;

    public Data(String title,  String ip, String id, String pass, int icon) {
        this.title = title;
        this.ip = ip;
        this.id = id;
        this.pass = pass;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPass() {
        return pass;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

}
