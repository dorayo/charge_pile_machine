package com.huamar.charge.pile.server.session;

import cn.hutool.core.util.IdUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.LoggerEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    // 设备认证日志
    private static final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());

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


    /**
     * setMDCParam netty
     *
     * @param ctx ctx
     */
    @SuppressWarnings("DuplicatedCode")
    public static String setMDCParam(ChannelHandlerContext ctx){
        String idCode = null;
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            idCode = ctx.channel().attr(machineId).get();
            if (StringUtils.isNotBlank(idCode)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
            }

            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            String sessionId = ctx.channel().attr(sessionKey).get();
            if (StringUtils.isNotBlank(sessionId)) {
                MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);
            }
        }catch (Exception e){
            log.error("setMDCParam error:{}", ExceptionUtils.getMessage(e));
        }
        return idCode;
    }


    /**
     * 获取 sessionId
     * @param ctx ctx
     * @return String
     */
    public static String getSessionId(ChannelHandlerContext ctx){
        try {
            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            return ctx.channel().attr(sessionKey).get();
        }catch (Exception e){
            log.error("getSessionId error:{}", ExceptionUtils.getMessage(e));
        }
        return null;
    }


    /**
     * 获取 sessionId
     * @param ctx ctx
     * @return String
     */
    public static String getIdCode(ChannelHandlerContext ctx){
        try {
            AttributeKey<String> var = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            return ctx.channel().attr(var).get();
        }catch (Exception e){
            log.error("getIdCode error:{}", ExceptionUtils.getMessage(e));
        }
        return null;
    }


    /**
     * channelActive
     *
     * @param ctx ctx
     * @param type type
     */
    public static void channelActive(ChannelHandlerContext ctx, String type){
        try {
            MDC.clear();
            Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            String sessionId = ctx.channel().attr(sessionKey).get();
            if(Objects.isNull(sessionId)){
                sessionId = IdUtil.getSnowflakeNextIdStr();
                ctx.channel().attr(sessionKey).set(sessionId);
            }
            MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);
            log.info("{} {} channelActive 连上了服务器", type, ctx.channel().remoteAddress());
            authLog.info("{} {} channelActive 连上了服务器", type, ctx.channel().remoteAddress());
        }catch (Exception e){
            log.error("{} channelActive error:{}", type, ExceptionUtils.getMessage(e), e);
        }finally {
            MDC.clear();
        }
    }

    /**
     *
     * channelInactive
     *
     * @param ctx ctx
     * @param type type
     */
    public static void channelInactive(ChannelHandlerContext ctx, String type){
        try {
            Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
            String idCode = SessionManager.setMDCParam(ctx);
            log.warn("{} channelInactive 连接不活跃 idCode:{} remoteAddress:{}", type, idCode, ctx.channel().remoteAddress());
            authLog.warn("{} channelInactive 连接不活跃 idCode:{} remoteAddress:{}", type, idCode, ctx.channel().remoteAddress());
        }catch (Exception e){
            log.error("{} channelInactive error:{}", type, ExceptionUtils.getMessage(e), e);
            authLog.error("{} channelInactive error:{}", type, ExceptionUtils.getMessage(e), e);
        }finally {
            MDC.clear();
        }
    }


    /**
     *
     * @param ctx ctx
     * @param type type
     */
    public static void handlerRemoved(ChannelHandlerContext ctx, String type){
        try {
            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            String sessionId = ctx.channel().attr(sessionKey).get();
            if(Objects.nonNull(sessionId)){
                MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);
            }

            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            log.warn("{} handlerRemoved, idCode:{}, remoteAddress:{}", type, idCode, ctx.channel().remoteAddress());
            authLog.warn("{} handlerRemoved, idCode:{}, remoteAddress:{}", type, idCode, ctx.channel().remoteAddress());

            if (StringUtils.isNotBlank(idCode)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
                SessionManager.remove(idCode);
            }

        } catch (Exception cause) {
            log.error("{} handlerRemoved error, idCode:{}, remoteAddress:{}", type, ctx.channel().remoteAddress(), cause.getMessage(), cause);
            authLog.error("{} handlerRemoved error, idCode:{}, remoteAddress:{}", type, ctx.channel().remoteAddress(), cause.getMessage(), cause);
        } finally {
            // 防止session 关闭不执行，始终执行一次
            ctx.channel().close().addListener(future -> {
                authLog.error("{} SessionManager ctx channel close:{} ", type, future.isSuccess(), future.cause());
                log.error("{} SessionManager ctx channel close:{} ", type, future.isSuccess(), future.cause());
            });

            ctx.close().addListener(future -> {
                authLog.error("{} SessionManager ctx close:{} ", type, future.isSuccess(), future.cause());
                log.error("{} SessionManager ctx close:{} ", type, future.isSuccess(), future.cause());
            });
        }
    }


    /**
     * 关闭连接
     *
     * @param ctx ctx
     * @param type type
     */
    @SuppressWarnings("DuplicatedCode")
    public static void closeCtx(ChannelHandlerContext ctx, String type) {
        // 防止session 关闭不执行，始终执行一次
        ctx.channel().close().addListener(future -> {
            authLog.error("{} SessionManager ctx channel close:{} ", type, future.isSuccess(), future.cause());
            log.error("{} SessionManager ctx channel close:{} ", type, future.isSuccess(), future.cause());
        });

        ctx.close().addListener(future -> {
            authLog.error("{} SessionManager ctx close:{} ", type, future.isSuccess(), future.cause());
            log.error("{} SessionManager ctx close:{} ", type, future.isSuccess(), future.cause());
        });
    }


}
