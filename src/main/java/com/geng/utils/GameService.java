package com.geng.utils;

import java.util.UUID;

public final class GameService {
    /**
     * 生成全局唯一的UID
     */
    public static String getGUID() {
        String guid = UUID.randomUUID().toString();
        StringBuilder buff=new StringBuilder(guid.length());
        buff.append(guid.substring(0, 8));
        buff.append(guid.substring(9, 13));
        buff.append(guid.substring(14, 18));
        buff.append(guid.substring(19, 23));
        buff.append(guid.substring(24));
        return buff.toString();
    }
}
