package com.example.smb;

public class UsbData {
    private String title;
    private String ip;
    private int icon;

    public UsbData(String title, String ip, int icon) {
        this.title = title;
        this.ip = ip;
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


    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

}
