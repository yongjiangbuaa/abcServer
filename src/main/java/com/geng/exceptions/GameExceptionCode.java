package com.geng.exceptions;

public enum GameExceptionCode {
    UID_NOT_EXIST("10001"),//uid不存在
    INVALID_OPT("E00000"),//无效操作 通常是参数错误
    ITEM_NOT_ENOUGH("10003"),//物品不足
    LIFE_NOT_ENOUGH("10004"),//heart不足
    USERGOLD_IS_NOT_ENOUGH("10005"),//金币不足
    STAR_NOT_ENOUGH("10006"),//星星不足
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
