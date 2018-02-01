package com.geng.puredb.model;

public class UserItem {
    private String uuid;

    private String ownerid;

    private String itemid;

    private Integer count;

    private Integer value;

    private Long vanishtime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public String getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(String ownerid) {
        this.ownerid = ownerid == null ? null : ownerid.trim();
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid == null ? null : itemid.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Long getVanishtime() {
        return vanishtime;
    }

    public void setVanishtime(Long vanishtime) {
        this.vanishtime = vanishtime;
    }
}