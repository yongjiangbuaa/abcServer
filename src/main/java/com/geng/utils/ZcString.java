package com.geng.utils;

/**
 * Created by zhengcheng on 2015/8/31.
 */
public class ZcString implements Comparable<ZcString>{
    private String s;
    private int length;
    public ZcString(String s){
        this.s = s;
        length = s.length();
    }
    public String toString(){
        return s;
    }
    public int length(){
        return length;
    }
    @Override
    public int compareTo(ZcString zc) {
        if( length > zc.length()){
            return -1;
        }
        if( length < zc.length()){
            return 1;
        }
        return 0;
    }
}
