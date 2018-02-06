package com.geng.exception;

public enum GameExceptionCode {
    UID_NOT_EXIST("10001"),
    INVALID_OPT("E00000"),
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

    public String getCode() {
        return value;
    }
}
