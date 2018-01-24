package com.geng.handlers;

public interface IRequestHandler {

    public void handle(String deviceId,String uid,String data,StringBuilder sb);
}
