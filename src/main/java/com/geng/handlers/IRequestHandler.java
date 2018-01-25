package com.geng.handlers;

import com.geng.exception.GameException;

public interface IRequestHandler {

    public void handle(String deviceId,String uid,String data,StringBuilder sb) throws GameException;
}
