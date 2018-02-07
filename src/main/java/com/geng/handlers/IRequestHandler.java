package com.geng.handlers;

import com.geng.exceptions.GameException;
import com.geng.puredb.model.UserProfile;

public interface IRequestHandler {

    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException;
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException;
}
