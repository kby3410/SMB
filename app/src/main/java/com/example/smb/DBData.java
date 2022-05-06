package com.example.smb;

public class DBData {
    private String ip;
    private String id;
    private String pass;

    public DBData(String ip, String id, String pass) {
        this.ip = ip;
        this.id = id;
        this.pass = pass;
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


}
