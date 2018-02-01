package com.geng.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class G {
    public static String toJson(Object o){
        return new Gson().toJson(o);
    }
    public static <T> T fromJson(String s,Type t){
        return new Gson().fromJson(s,t);
    }


}
