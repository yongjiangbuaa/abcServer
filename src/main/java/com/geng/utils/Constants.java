package com.geng.utils;

import com.geng.server.GameEngine;
import com.geng.utils.properties.PropertyFileReader;


public class Constants {
    /*
    *服务器配置信息
    * */
    public static final int SERVER_ID = GameEngine.ZONE_ID;
    public static final String SERVER_ID_STR = Integer.toString(GameEngine.ZONE_ID);
    public static final String BIG_ZONE_ID = "1";
    public static final int SERVER_ID_MAX = 6;
    public static final String LOG_SEPARATOR = "|";
    public static final String LOG_SEPARATOR_REPLACE = "vertical";
    public static final String XML_RESOUCE_PATH = "resource/";
    public static final String GAME_CONFIG_PATH = "gameconfig";
    /*
    *游戏信息
    * */
    public static final int MAX_RANK_NUM = 200;
    public static final String LOGIN_KEY = "unYibGaxd47SkLdASqXAnXnLpGXZ9KHq";
    public static final String MARCH_KEY = "4ilbHmO2Bl1HqKnc";
//    public static final String LOGIN_KEY = "a0bd01c976818c62b0570b5b7feedf12";
//    public static final String MARCH_KEY = "wNSc5Mm8YwiOhAab";
//    public static final String LOGIN_KEY = "xxx";
//    public static final String LOGIN_NEW_KEY = "xxx";
//    public static final String MARCH_KEY = "xxx";
    public static final int CONCURRENT_LOGIN_LOCK_TIME = PropertyFileReader.getIntItem("concurrent.login.lock.time", "0");
    public static final int CHAT_INTERVAL = 1;
    public static final int ROLE_BASE_VALUE = 100100;
    public static final int NOBILITY_BASE_VALUE = 100100000;
    public static final int NOBILITY_BASE_VALUE2 = 100200000;

    public static final String XML_ELEMENT_NAME = "ItemSpec";
    public static final String XML_GROUP_NAME = "Group";
    public static final String TEST_SERVER_REGISTER_COUNT_DAILY = "TEST_SERVER_REGISTER_COUNT_DAILY";
    public static final String TEST_SERVER_REGISTER_NORMAL = "TEST_SERVER_REGISTER_NORMAL";


    public static final String BIND_FIRST_GOOGLE_MAIL_ID = "";
    public static final String BIND_FIRST_APPSTORE_MAIL_ID = "";
    public static final String BIND_FIRST_VK_MAIL_ID = "";
}
