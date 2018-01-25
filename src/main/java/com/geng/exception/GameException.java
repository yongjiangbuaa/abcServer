package com.geng.exception;

import com.google.gson.GsonBuilder;

public class GameException extends Throwable {
    private int err;
    private String errMsg;

    public GameException(GameExceptionCode code, String str) {
        err = code.getValue();
        errMsg = str;
    }

    public enum GameExceptionCode {
        UID_NOT_EXIST(1),
        ACCESS_CONFIG_FILE_ERROR(999999),
        ;

        public int getValue() {
            return value;
        }

        private int value;
        GameExceptionCode(int i) {
            this.value = i;
        }
    }

    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"err\":")
                .append(err)
                .append(",\"errMsg\":\"")
                .append(errMsg)
                .append("\"}");
        return sb.toString();
    }
}
