package com.geng.exceptions;

public enum GameExceptionCode {
    UID_NOT_EXIST("10001"),
    INVALID_OPT("E00000"),
    ITEM_NOT_ENOUGH("10003"),
    LIFE_NOT_ENOUGH("10004"),
    USERGOLD_IS_NOT_ENOUGH("10005"),
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
