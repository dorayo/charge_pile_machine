package com.huamar.charge.pile.server.session;

import cn.hutool.core.convert.Convert;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * session 客户端管理
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class SessionManager implements ApplicationListener<ContextRefreshedEvent> {

    private final static Cache<String, SessionChannel> cache = Caffeine.newBuilder().initialCapacity(1000).build();

    private static MachineSessionContext sessionContext;

    private static RedissonClient redissonClient;

    /**
     * 根据业务Id获取Session
     * @param bsId bsId
     * @return SessionChannel
     */
    public static SessionChannel get(String bsId){
        return cache.getIfPresent(bsId);
    }

    /**
     * 清除会话缓存
     * @param bsId bsId
     */
    public static void put(String bsId, SessionChannel sessionChannel){
        cache.put(bsId, sessionChannel);
    }


    /**
     * 清除会话缓存
     * @param bsId bsId
     */
    public static void remove(String bsId){
        cache.invalidate(bsId);
    }


    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        redissonClient = applicationContext.getBean(RedissonClient.class);
        sessionContext = applicationContext.getBean(MachineSessionContext.class);
    }

    /**
     * 消息应答
     *
     * @param packet  packet
     * @param channel channel
     */
    public static boolean writePacket(DataPacket packet, SessionChannel channel) {
        return sessionContext.writePacket(packet, channel);
    }

    /**
     * 发送消息
     *
     * @param packet packet
     * @return boolean
     */
    public static boolean writePacket(DataPacket packet) {
        return sessionContext.writePacket(packet);
    }


    /**
     * 获取消息流水号
     *
     * @return Short
     */
    @SuppressWarnings("DuplicatedCode")
    public static Short getMessageNumber(String idCode){
        RAtomicLong atomicLong = redissonClient.getAtomicLong(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.joinKey(idCode));
        long incremented = atomicLong.incrementAndGet();
        if(Objects.equals(incremented, 1L)){
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }

        if(Objects.equals(incremented, 65535)){
            atomicLong.set(0L);
            incremented = 0;
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }
        return Convert.toShort(incremented);
    }
}
