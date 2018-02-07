/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月30日 下午3:39:40
 */
package com.geng.exceptions;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;

/**
 * 游戏异常
 */
public class COKException extends GameException {

    private static final long serialVersionUID = -3464320023735291595L;

    private GameExceptionCode exceptionCode;
    private ISFSObject retObj;

    public COKException() {
        super("Unknown Error");
        exceptionCode = GameExceptionCode.INVALID_OPT;
    }

    public COKException(GameExceptionCode exceptionCode) {
        super("Unknown Error");
        this.exceptionCode = exceptionCode;
    }

    public COKException(GameExceptionCode exceptionCode, String exceptionDesc) {
        super(exceptionDesc);
        this.exceptionCode = exceptionCode;
    }

    public COKException(GameExceptionCode exceptionCode, ISFSObject retObj, String exceptionDesc) {
        super(exceptionDesc);
        this.retObj = retObj;
        this.exceptionCode = exceptionCode;
    }

    /**
     * @return the exceptionCode
     */
    public GameExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    /**
     * @return the retObj
     */
    public ISFSObject getRetObj() {
        return retObj;
    }

    public void setRetObj(ISFSObject retObj) {
        this.retObj = retObj;
    }

    @Override
    public String toString() {
        ISFSObject obj = new SFSObject();
        obj.putUtfString("errorCode", exceptionCode.getCode());
        obj.putUtfString("errorMsg", getMessage());
        if (retObj != null && retObj.size() > 0) {
            obj.putSFSObject("errorData", retObj);
        }
        return obj.toJson();
    }
}
