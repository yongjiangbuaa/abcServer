package com.geng.exception;

public class GameException extends Throwable {
    private String err;
    private String errMsg;


    public GameException(GameExceptionCode code, String str) {
        err = code.getValue();
        errMsg = str;
    }

    public GameException(String desc){
        err = GameExceptionCode.INVALID_OPT.getCode();
        errMsg = desc;
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
