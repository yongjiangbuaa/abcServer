package com.geng.exceptions;

public enum GameExceptionCode {
    UID_NOT_EXIST("10001"),
    INVALID_OPT("E00000"),
    ITEM_NOT_ENOUGH("10003"),
    LIFE_NOT_ENOUGH("10004"),
    PARAM_ILLEGAL("10005"),
    FB_NOT_BIND("10006"), NETWORK_UNORMAL("10007"),
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
