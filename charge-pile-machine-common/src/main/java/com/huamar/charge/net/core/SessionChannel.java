package com.huamar.charge.net.core;

import java.io.Serializable;
import java.net.InetSocketAddress;
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

    /**
     * 获取客户端地址
     * @return InetSocketAddress
     */
    InetSocketAddress remoteAddress();

    /**
     * getAttributeKeys
     *
     * @return Collection<Object>
     */
    Collection<Object> getAttributeKeys();

    /**
     * getAttribute
     *
     * @param key key
     * @return Object
     */
    Object getAttribute(Object key);

    /**
     * setAttribute
     *
     * @param key key
     * @param value value
     */
    void setAttribute(Object key, Object value);

    /**
     *
     * @param key key
     * @return Object
     */
    Object removeAttribute(Object key);

    /**
     * 获取通道
     * @return Object
     */
    Object channel();

    /**
     * 关闭连接
     */
    void close();
}
