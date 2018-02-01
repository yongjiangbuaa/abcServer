package com.geng.utils;

import java.util.List;

public class MyHttpParam {
    int uid;
    int level;
    int gold;
    int star;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    String item;
    String num;
    List<Pair> items;


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public List<Pair> getItems() {
        return items;
    }

    public void setItems(List<Pair> items) {
        this.items = items;
    }

}


