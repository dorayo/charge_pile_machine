package com.huamar.charge.net.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * 服务端Session接口
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface SessionChannel {

    /**
     * id
     * @return Serializable
     */
    Serializable getId();

    /**
     *
     * @return Date
     */
    Date getStartTimestamp();

    /**
     *
     * @return Date
     */
    Date getLastAccessTime();

    /**
     *
     * @return Sting Host
     */
    String getIp();

    Collection<Object> getAttributeKeys();

    Object getAttribute(Object var1);

    void setAttribute(Object var1, Object var2);

    Object removeAttribute(Object var1);

    /**
     * 获取通道
     * @return Object
     */
    Object channel();
}
