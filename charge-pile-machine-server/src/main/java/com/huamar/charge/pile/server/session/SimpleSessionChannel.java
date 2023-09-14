package com.huamar.charge.pile.server.session;

import com.huamar.charge.net.core.SessionChannel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的 SessionChannel
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class SimpleSessionChannel implements SessionChannel {

    @Setter
    private String id;

    private final Date startDate = new Date();


    private final ConcurrentHashMap<Object, Object> attribute = new ConcurrentHashMap<>();

    /**
     * 通道
     */
    private final ChannelHandlerContext channelContext;

    public SimpleSessionChannel(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    /**
     * id
     *
     * @return Serializable
     */
    @Override
    public Serializable getId() {
        return id;
    }

    /**
     * @return Date
     */
    @Override
    public Date getStartTimestamp() {
        return startDate;
    }

    /**
     * @return Date
     */
    @Override
    public Date getLastAccessTime() {
        return null;
    }

    /**
     * @return Sting Host
     */
    @Override
    public String getIp() {
        InetSocketAddress remoteAddress = (InetSocketAddress) channelContext.channel().remoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    @Override
    public Collection<Object> getAttributeKeys() {
        return attribute.keySet();
    }

    @Override
    public Object getAttribute(Object var1) {
        return attribute.get(var1);
    }

    @Override
    public void setAttribute(Object key, Object value) {
        this.attribute.put(key, value);
    }

    @Override
    public Object removeAttribute(Object key) {
        return this.attribute.remove(key);
    }

    /**
     * 获取通道
     *
     * @return Object
     */
    @Override
    public ChannelHandlerContext channel() {
        return channelContext;
    }
}
