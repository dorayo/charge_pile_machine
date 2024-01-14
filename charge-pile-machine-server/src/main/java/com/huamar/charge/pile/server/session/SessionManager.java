package com.huamar.charge.pile.server.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManager implements ApplicationListener<ContextRefreshedEvent> {

    private final static Cache<String, SessionChannel> cache = Caffeine.newBuilder().initialCapacity(4096).build();

    private static MachineSessionContext sessionContext;

    private static RedissonClient redissonClient;

    /**
     * 消息投递
     */
    private static PileMessageProduce pileMessageProduce;

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
     * @param idCode idCode
     */
    public static void remove(String idCode){
        try {

            if(Objects.isNull(idCode)){
                return;
            }

            SessionChannel sessionChannel = cache.getIfPresent(idCode);
            if(Objects.nonNull(sessionChannel)){
                sessionChannel.close();
            }

            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.PILE_OFFLINE, idCode));
        }catch (Exception e){
            log.error("remove error:{}", e.getMessage(), e);
        }
        cache.invalidate(idCode);
    }


    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        redissonClient = applicationContext.getBean(RedissonClient.class);
        sessionContext = applicationContext.getBean(MachineSessionContext.class);
        pileMessageProduce = applicationContext.getBean(PileMessageProduce.class);
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
    public static Integer getMessageNumber(String idCode){
        RAtomicLong atomicLong = redissonClient.getAtomicLong(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.joinKey(idCode));
        long incremented = atomicLong.incrementAndGet();
        if(Objects.equals(incremented, 1L)){
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }

        if(incremented >= 65500){
            atomicLong.set(0L);
            incremented = 0;
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }
        return (int) (incremented);
    }

    /**
     * 关闭客户端连接
     *
     * @param sessionChannel sessionChannel
     */
    public static void close(SessionChannel sessionChannel){
        sessionChannel.close();
    }
}
