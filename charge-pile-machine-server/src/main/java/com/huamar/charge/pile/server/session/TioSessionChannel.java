package com.huamar.charge.pile.server.session;

import com.huamar.charge.net.core.SessionChannel;
import lombok.Data;
import org.tio.core.ChannelContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * tio session channel
 * Date: 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
public class TioSessionChannel implements SessionChannel {

    private ChannelContext channel;


    /**
     * id
     *
     * @return Serializable
     */
    @Override
    public Serializable getId() {
        return null;
    }

    /**
     * @return Date
     */
    @Override
    public Date getStartTimestamp() {
        return null;
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
        return null;
    }

    @Override
    public Collection<Object> getAttributeKeys() {
        return null;
    }

    @Override
    public Object getAttribute(Object var1) {
        return null;
    }

    @Override
    public void setAttribute(Object var1, Object var2) {

    }

    @Override
    public Object removeAttribute(Object var1) {
        return null;
    }

    /**
     * 获取通道
     *
     * @return Object
     */
    @Override
    public ChannelContext channel() {
        return channel;
    }

    public TioSessionChannel(ChannelContext channel) {
        this.channel = channel;
    }
}
