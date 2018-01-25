package com.geng.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class G {
    public static String toJson(Object o){
        return new Gson().toJson(o);
    }
    public static <T> T fromJson(String s,Type t){
        return new Gson().fromJson(s,t);
    }
}
