package com.geng.puredb.model;

import java.io.Serializable;

public class StatLoginKey implements Serializable {
    private Long time;

    private String uid;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }
}