package com.geng.exception;

public class GameException extends Throwable {
    private String err;
    private String errMsg;

    public GameException(GameExceptionCode code, String str) {
        err = code.getValue();
        errMsg = str;
    }

    public enum GameExceptionCode {
        UID_NOT_EXIST("10001"),
        INVALID_OPTION("E00000"),
        ITEM_NOT_ENOUGH("10003"),
        LIFE_NOT_ENOUGH("10004"),
        ACCESS_CONFIG_FILE_ERROR("999999"),
        ;

        public String getValue() {
            return value;
        }

        private String value;
        GameExceptionCode(String i) {
            this.value = i;
        }
    }

    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"err\":\"")
                .append(err)
                .append("\",\"errMsg\":\"")
                .append(errMsg)
                .append("\"}");
        return sb.toString();
    }
}
