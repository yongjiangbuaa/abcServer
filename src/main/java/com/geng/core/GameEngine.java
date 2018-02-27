package com.geng.core;

import com.geng.core.data.ISFSObject;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.MyBatisSessionUtil;

public class GameEngine {
    private static GameEngine instance = new GameEngine();
    public static GameEngine getInstance() {
        return instance;
    }

    public UserProfile getPresentUserProfile(String senderUid) {
        return null;
    }

    public void pushMsg(String pushMail, ISFSObject pushInfo, UserProfile targetUser) {

    }

    public void pushMsgToRemoteUser(int serverId, String touser, String pushMail, String pushJson) {

    }

    public void addUserProfile(UserProfile userProfile) {

    }

    public boolean isTestServer() {
            return false;
    }
}
