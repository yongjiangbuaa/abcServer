/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年10月31日 下午2:26:00
 */
package com.geng.exception;

/**
 *XML配置运行时异常
 */
public class IllegalXMLConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public IllegalXMLConfigException() {
        super();
    }
	
	public IllegalXMLConfigException(String s) {
        super(s);
    }
	
	public IllegalXMLConfigException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public IllegalXMLConfigException(Throwable cause) {
        super(cause);
    }
}
