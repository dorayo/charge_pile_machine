package com.huamar.charge.pile.server.session;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.PileLoginLogDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


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
     * 异步线程池
     */
    private static ThreadPoolTaskExecutor taskExecutor;

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
        taskExecutor = applicationContext.getBean(ThreadPoolTaskExecutor.class);
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

        if(incremented >= 65500){
            atomicLong.set(0L);
            incremented = 0;
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }
        //noinspection VulnerableCodeUsages
        return Convert.toShort(incremented);
    }

    /**
     * 获取流水号
     * @param ctx ctx
     * @return Short
     */
    public static Short getYKCSerialNumber(ChannelHandlerContext ctx){
        AtomicInteger orderV = ctx.channel().attr(NAttrKeys.SERIAL_NUMBER).get();
        if(Objects.isNull(orderV)){
            orderV = new AtomicInteger(0);
            ctx.channel().attr(NAttrKeys.SERIAL_NUMBER).set(orderV);
        }
        int incrementAndGet = orderV.incrementAndGet();
        if(incrementAndGet >= Short.MAX_VALUE){
            orderV.set(0);
            incrementAndGet = orderV.incrementAndGet();
        }
        return (short) incrementAndGet;
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
     * 获取登录时间
     * @param ctx ctx
     * @return String
     */
    public static LocalDateTime getLoginTime(ChannelHandlerContext ctx){
        try {
            AttributeKey<LocalDateTime> sessionKey = AttributeKey.valueOf(ConstEnum.X_LOGIN_TIME.getCode());
            return ctx.channel().attr(sessionKey).get();
        }catch (Exception e){
            log.error("setLoginTimeNow error:{}", ExceptionUtils.getMessage(e));
        }
        return null;
    }

    /**
     * 设置登录时间
     * @param ctx ctx
     */
    public static void setLoginTimeNow(ChannelHandlerContext ctx){
        try {
            AttributeKey<LocalDateTime> sessionKey = AttributeKey.valueOf(ConstEnum.X_LOGIN_TIME.getCode());
            ctx.channel().attr(sessionKey).set(LocalDateTime.now());
        }catch (Exception e){
            log.error("setLoginTimeNow error:{}", ExceptionUtils.getMessage(e));
        }
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
     * 获取 sessionId
     * @param ctx ctx
     * @return String
     */
    public static String getStationId(ChannelHandlerContext ctx){
        try {
            AttributeKey<String> var = AttributeKey.valueOf(ConstEnum.STATION_ID.getCode());
            return ctx.channel().attr(var).get();
        }catch (Exception e){
            log.error("getStationId error:{}", ExceptionUtils.getMessage(e));
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
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            log.info("{} IP:{} channelActive 连上了服务器", type, remoteAddress);
            authLog.info("{} IP:{} channelActive 连上了服务器", type, remoteAddress);

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
            SessionManager.pileOffLineLogSum(idCode, ctx, type);
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
        authLog.error("{} SessionManager closeCtx channel close", type);
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


    /**
     * 登陆历史记录
     * @param pileLoginLogDTO pileLoginLogDTO
     */
    public static void pileAuthLogSum(PileLoginLogDTO pileLoginLogDTO){
        if(Objects.isNull(pileLoginLogDTO)){
            log.warn("pileAuthLogSum is null");
            return;
        }

        if(Objects.isNull(pileLoginLogDTO.getStationId())){
            pileLoginLogDTO.setStationId(-1);
        }

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 获取下一个凌晨的时间（00:00:00）
        LocalDateTime nextMidnight = currentTime.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT);
        // 计算当前时间到下一个凌晨的时间差（以秒为单位）
        long secondsUntilNextMidnight = currentTime.until(nextMidnight, java.time.temporal.ChronoUnit.SECONDS);

        String key = CacheKeyEnum.CHARGE_PILE_AUTH_LOG.joinKey(String.valueOf(pileLoginLogDTO.getStationId()));

        RMap<String, PileLoginLogDTO> map = redissonClient.getMap(key);

        PileLoginLogDTO loginLogDTO = map.get(pileLoginLogDTO.getIdCode());
        if(Objects.isNull(loginLogDTO)){
            loginLogDTO = pileLoginLogDTO;
        }

        loginLogDTO.setConCount(loginLogDTO.getConCount()+1);

        Set<String> ipAddrList = pileLoginLogDTO.getIpAddrList();
        if(CollectionUtils.isEmpty(ipAddrList)){
            ipAddrList = new HashSet<>();
        }

        String ipAddr = loginLogDTO.getLastConIpAddr();
        if(StringUtils.isNotBlank(ipAddr)){
            ipAddrList.add(ipAddr);
        }

        loginLogDTO.setIpAddrList(ipAddrList);
        loginLogDTO.setAuthStatus(1);
        loginLogDTO.setStatus(1);
        map.put(pileLoginLogDTO.getIdCode(), loginLogDTO);

        // 每天清空数据，只保留一天
        long expireTime = map.remainTimeToLive();
        if(expireTime < 0){
            map.expire(Duration.ofSeconds(secondsUntilNextMidnight));
        }
    }


    /**
     * 记录设备登录日志
     *
     * @param sessionChannel sessionChannel
     */
    public static void pileAuthLogSum(SessionChannel sessionChannel){
        try {
            if(sessionChannel instanceof SimpleSessionChannel){
                ChannelHandlerContext ctx = (ChannelHandlerContext) sessionChannel.channel();
                String idCode = SessionManager.getIdCode(ctx);
                String stationId = SessionManager.getStationId(ctx);
                String hostAddress = sessionChannel.remoteAddress().getAddress().getHostAddress();
                taskExecutor.execute(() -> {
                    PileLoginLogDTO log = new PileLoginLogDTO();
                    log.setStationId(Objects.isNull(stationId) ? -1 : Integer.parseInt(stationId));
                    log.setType(((SimpleSessionChannel) sessionChannel).getType().name());
                    log.setIdCode(idCode);
                    log.setLastConTime(LocalDateTime.now());
                    log.setLastConIpAddr(hostAddress);
                    SessionManager.pileAuthLogSum(log);
                });
            }
        }catch (Exception e){
            log.error("pileAuthLogSum error", e);
        }
    }

    /**
     *
     * 设备离线记录
     *
     * @param idCode idCode
     * @param ctx ctx
     * @param type type
     */
    public static void pileOffLineLogSum(String idCode, ChannelHandlerContext ctx, String type){
        try {
            // 获取当前时间
            LocalDateTime currentTime = LocalDateTime.now();
            // 获取下一个凌晨的时间（00:00:00）
            LocalDateTime nextMidnight = currentTime.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT);
            // 计算当前时间到下一个凌晨的时间差（以秒为单位）
            long secondsUntilNextMidnight = currentTime.until(nextMidnight, java.time.temporal.ChronoUnit.SECONDS);

            if(StringUtils.isBlank(idCode)){
                String key = CacheKeyEnum.CHARGE_PILE_INVALID_CONNECTION.joinKey("0");
                RMap<String, Integer> map = redissonClient.getMap(key);
                InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                String hostAddress = inetSocketAddress.getAddress().getHostAddress();
                if (StringUtils.isBlank(hostAddress)){
                    hostAddress = "blank";
                }
                Integer conCount = map.get(hostAddress);
                if(Objects.isNull(conCount)){
                    conCount = 0;
                }else {
                    conCount = conCount + 1;
                }
                map.put(hostAddress, conCount);

                // 每天清空数据，只保留一天 !!! 最后执行
                long expireTime = map.remainTimeToLive();
                if(expireTime < 0){
                    map.expire(Duration.ofSeconds(secondsUntilNextMidnight));
                }
                authLog.warn("{} channelInactive 连接不活跃,无效链接 idCode:{} remoteAddress:{}", type, idCode, ctx.channel().remoteAddress());
                return;
            }


            // 进入认证
            String stationId = SessionManager.getStationId(ctx);
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();
            if(StringUtils.isBlank(stationId)){
                stationId = "-1";
            }

            String key = CacheKeyEnum.CHARGE_PILE_AUTH_LOG.joinKey(stationId);
            RMap<String, PileLoginLogDTO> map = redissonClient.getMap(key);
            PileLoginLogDTO loginLogDTO = map.get(idCode);
            if(Objects.isNull(loginLogDTO)){
                loginLogDTO = new PileLoginLogDTO();
                loginLogDTO.setIdCode(idCode);
                loginLogDTO.setLastConIpAddr(hostAddress);
                loginLogDTO.setType(type);
                loginLogDTO.setStationId(Integer.valueOf(stationId));
                loginLogDTO.setAuthStatus(0);
            }
            loginLogDTO.setStatus(0);
            map.put(idCode, loginLogDTO);
        }catch (Exception e){
            log.error("pileOffLineLogSum error:{}", ExceptionUtils.getMessage(e), e);
        }
    }
}
