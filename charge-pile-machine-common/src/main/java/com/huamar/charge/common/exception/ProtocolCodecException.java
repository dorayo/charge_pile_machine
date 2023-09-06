package com.huamar.charge.common.exception;

/**
 * 解码不匹配-网络消息包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class ProtocolCodecException extends java.lang.Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8207465969738755041L;

    /**
     *
     *
     * @author tanyaowu
     *
     */
    public ProtocolCodecException() {
    }

    /**
     * @param message message
     *
     * @author tanyaowu
     *
     */
    public ProtocolCodecException(String message) {
        super(message);

    }

    /**
     * @param message message
     * @param cause cause
     *
     * @author tanyaowu
     *
     */
    public ProtocolCodecException(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * @param message message
     * @param cause cause
     * @param enableSuppression enableSuppression
     * @param writableStackTrace writableStackTrace
     *
     * @author tanyaowu
     *
     */
    public ProtocolCodecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }

    /**
     * @param cause cause
     *
     * @author tanyaowu
     *
     */
    public ProtocolCodecException(Throwable cause) {
        super(cause);

    }

}