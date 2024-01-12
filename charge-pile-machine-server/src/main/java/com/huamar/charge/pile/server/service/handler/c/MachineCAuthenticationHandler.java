package com.huamar.charge.pile.server.service.handler.c;

import cn.hutool.core.date.StopWatch;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.service.machine.MachineService;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 终端鉴权
 * Date: 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCAuthenticationHandler {

    private final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());

    /**
     * 设备接口
     */
    private final MachineService machineService;

    /**
     * 消息生产者
     */
    private final PileMessageProduce pileMessageProduce;


    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private TaskExecutor taskExecutor;


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.AUTH;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    public void handler(ProtocolCPacket packet, SessionChannel sessionChannel, ChannelHandlerContext channelHandlerContext) {
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        byte[] idBody = channelHandlerContext.channel().attr(NAttrKeys.ID_BODY).get();
        String mId = channelHandlerContext.channel().attr(machineId).get();
        final int gunCount = packet.getBody()[8];

        log.info("YKC 终端鉴权 {}，loginNumber={} time={}", BCDUtils.bcdToStr(idBody) ,idBody, LocalDateTime.now());
        authLog.info("YKC 终端鉴权 {}，loginNumber={} time={}", BCDUtils.bcdToStr(idBody),idBody, LocalDateTime.now());

        ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
        bfB.writeBytes(idBody);
        bfB.writeByte(0x00);
        ByteBuf responseB = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), packet.getOrderVBf(), (byte) 0x02);
        log.info("YKC 终端鉴权 old writePacket:{}", BinaryViews.bfToHexStr(responseB));
        authLog.info("YKC 终端鉴权 old writePacket:{}", BinaryViews.bfToHexStr(responseB));

        responseB.markReaderIndex();
        log.info("YKC 终端鉴权 new writePacket:{}", ByteBufUtil.hexDump(responseB));
        authLog.info("YKC 终端鉴权 new writePacket:{}", ByteBufUtil.hexDump(responseB));
        responseB.resetReaderIndex();

        channelHandlerContext.channel().writeAndFlush(responseB).addListener((f) -> {
            if (f.isSuccess()) {
                sessionChannel.setAttribute("auth", "ok");
                log.info("YKC 终端鉴权 write msgId:{}, success", HexExtUtil.encodeHexStr((byte) 0x02));
                authLog.info("YKC 终端鉴权 write msgId:{}, success", HexExtUtil.encodeHexStr((byte) 0x02));
            }else {
                sessionChannel.close();
                log.error("YKC 终端鉴权 write msgId:{}, error:{}", HexExtUtil.encodeHexStr((byte) 0x02), ExceptionUtils.getMessage(f.cause()), f.cause());
                authLog.error("YKC 终端鉴权 write msgId:{}, error:{}", HexExtUtil.encodeHexStr((byte) 0x02), ExceptionUtils.getMessage(f.cause()), f.cause());
            }
        });

        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        taskExecutor.execute(() -> {
            try {
                MDC.setContextMap(mdcMap);
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.PILE_AUTH, mId));

                long startTime = System.currentTimeMillis();
                long maxWaitTime = Duration.ofSeconds(3).toMillis();

                PileDTO pile = null;
                StopWatch stopWatch = new StopWatch("Auth");
                stopWatch.start("wait pile");
                while (System.currentTimeMillis() - startTime < maxWaitTime) {
                    pile = machineService.getCache(mId);
                    boolean nonNull = Objects.nonNull(pile);
                    log.warn("YKC 终端鉴权 auth wait pile time await:{} success:{}", System.currentTimeMillis() - startTime, nonNull);
                    authLog.warn("YKC 终端鉴权 auth wait pile time await:{} success:{}", System.currentTimeMillis() - startTime, nonNull);
                    if (nonNull) {
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(200);
                }
                stopWatch.stop();
                log.info("YKC 终端鉴权 auth pile isSuccess:{}, time:{}", Optional.ofNullable(pile).isPresent(), stopWatch.getTotalTimeSeconds());
                authLog.info("YKC 终端鉴权 auth pile isSuccess:{}, time:{}", Optional.ofNullable(pile).isPresent(), stopWatch.getTotalTimeSeconds());

                // 多次鉴权并发问题
                Object auth = sessionChannel.getAttribute("auth");
                if (Objects.nonNull(auth)) {
                    log.info("YKC 终端鉴权 pile auth session attribute:{}", auth);
                    authLog.info("YKC 终端鉴权  pile auth session attribute:{}", auth);
                }

                if (Objects.isNull(pile)) {
                    log.info("YKC 终端鉴权 pile auth session attribute:{}", auth);
                    authLog.info("YKC 终端鉴权  pile auth session attribute:{}", auth);
                    SessionManager.close(sessionChannel);
                    return;
                }

                try {
                    channelHandlerContext.channel().attr(AttributeKey.valueOf(ConstEnum.STATION_ID.getCode())).set(pile.getStationId());
                    channelHandlerContext.channel().attr(AttributeKey.valueOf(ConstEnum.ELE_CHARG_TYPE.getCode())).set(Integer.parseInt(pile.getElectricType()));
                }catch (Exception ignored){

                }

                PileDTO update = new PileDTO();
                update.setId(pile.getId());
                sessionChannel.setAttribute("auth", "ok");
                update.setStationId(pile.getStationId());
                update.setPileCode(pile.getPileCode());
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
                this.sendQrCode(channelHandlerContext, gunCount);
                this.syncTime(channelHandlerContext);
            } catch (Exception e) {
                log.error("YKC 终端鉴权  auth execute error:{}", e.getMessage(), e);
            }
        });
    }


    /**
     * 二维码下发
     */
    private void sendQrCode(ChannelHandlerContext ctx, int gunCount) {
        byte type = (byte) 0x9c;
        String url = machineService.getQrCode();
        byte[] urlB = url.getBytes(StandardCharsets.US_ASCII);
        byte urlLen = (byte) urlB.length;
        for (int i = 0; i <= gunCount; i++) {
            Integer latestOrderV = ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
            if(Objects.isNull(latestOrderV)){
                latestOrderV = 0;
            }
            latestOrderV++;
            ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(latestOrderV);
            ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
            bfB.writeByte(i);
            bfB.writeByte(urlLen);
            bfB.writeBytes(urlB);
            ByteBuf responseB = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), latestOrderV, type);
            log.info("YKC sendQrCode write 0x9c {} ", BinaryViews.bfToHexStr(responseB));
            ctx.writeAndFlush(responseB).addListener((f) -> {
                if (!f.isSuccess()) {
                    log.error("YKC sendQrCode write error:{}", ExceptionUtils.getMessage(f.cause()), f.cause());
                }
            });
        }
    }


    /**
     * syncTime
     */
    private void syncTime(ChannelHandlerContext ctx) {
        byte type = (byte) 0x56;
        byte[] idBody = ctx.channel().attr(NAttrKeys.ID_BODY).get();
        Integer latestOrderV = ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
        latestOrderV++;
        ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(latestOrderV);
        ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
        bfB.writeBytes(idBody);
        bfB.writeBytes(Cp56Time2aUtil.dateToByte(new Date()));
        ByteBuf responseB = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), latestOrderV, type);
        log.info("YKC syncTime write 0x56 Hex {} ", BinaryViews.bfToHexStr(responseB));

        ctx.writeAndFlush(responseB).addListener((f) -> {
            if (!f.isSuccess()) {
                log.error("YKC syncTime write 0x56 error");
                log.error("YKC syncTime write error:{}", ExceptionUtils.getMessage(f.cause()), f.cause());
            }
        });
    }
}
