package com.huamar.charge.pile.server.service.handler.c;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.BaseResp;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FixString;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.convert.MachineAuthenticationConvert;
import com.huamar.charge.pile.entity.dto.MachineAuthenticationReqDTO;
import com.huamar.charge.pile.entity.dto.command.McQrCodeCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.resp.McAuthResp;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.service.answer.McAnswerExecute;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.service.machine.MachineService;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static sun.security.util.KnownOIDs.Data;

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


    /**
     * 设备接口
     */
    private final MachineService machineService;

    /**
     * 应答回复工厂
     */
    private final McAnswerFactory answerFactory;

    /**
     * 指令下发工厂
     */
    private final McCommandFactory commandFactory;

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
        final String id = channelHandlerContext.channel().attr(machineId).get();
        Assert.notNull(id, "id ");
        final int gunCount = packet.getBody()[8];
        log.info("终端鉴权，loginNumber={} time={}", id, LocalDateTime.now());
        ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
        bfB.writeBytes(BinaryViews.bcdStringToByte(id));
        bfB.writeByte(0x00);
        ByteBuf responseB = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), packet.getOrderVBf(), (byte) 0x02);
        log.info(BinaryViews.bfToHexStr(responseB));
        //        log.info("write {}", BinaryViews.bfToHexStr(BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), packet.getOrderVBf(), (byte) 0x02)));
        channelHandlerContext.channel().writeAndFlush(responseB).addListener((f) -> {
            if (f.isSuccess()) {
                sessionChannel.setAttribute("auth", "ok");
                log.info("0x0{}write success", 0x02);
            } else {
                sessionChannel.close();
                f.cause().printStackTrace();
            }
        });
        taskExecutor.execute(() -> {
            try {
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.PILE_AUTH, id));
//                pileMessageProduce.send(new MessageData<>(MessageCodeEnum., id));
                long startTime = System.currentTimeMillis();
                long maxWaitTime = Duration.ofSeconds(3).toMillis();
                PileDTO pile = null;
                StopWatch stopWatch = new StopWatch("Auth");
                stopWatch.start("wait pile");
                while (System.currentTimeMillis() - startTime < maxWaitTime) {
                    pile = machineService.getCache(id);
                    if (Objects.isNull(pile)) {
                        continue;
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                }
                stopWatch.stop();
                log.info("auth pile isSuccess:{}", Optional.ofNullable(pile).isPresent());
                log.info("auth auth task run time:{} prettyPrint:{}"
                        , stopWatch.getTotalTimeSeconds()
                        , stopWatch.prettyPrint(TimeUnit.MILLISECONDS));

                if (Objects.isNull(pile)) {
                    SessionManager.close(sessionChannel);
                    return;
                }
                PileDTO update = new PileDTO();
                update.setId(pile.getId());
                sessionChannel.setAttribute("auth", "ok");
                update.setStationId(pile.getStationId());
                update.setPileCode(pile.getPileCode());
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
                sendQrCode(packet, channelHandlerContext, gunCount);

                // 多次鉴权并发问题，先返回成功，认证失败关闭连接
//                authResp.setStatus(MachineAuthStatus.SUCCESS.getCode());
                //             answerExecute.execute(authResp, sessionChannel);

//                long startTime = System.currentTimeMillis();
//                long maxWaitTime = Duration.ofSeconds(3).toMillis();

                // 多次鉴权并发问题

                // 更新对象
//                PileDTO update = new PileDTO();
//                update.setPileCode(id);

                // 标记此连接鉴权成功


                // 二维码下发
//                this.sendQrCode(authResp);
                // 设备更新
//                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.PILE_UPDATE, update));

                //电价更新
//                update.setStationId(pile.getStationId());
//                update.setPileCode(pile.getPileCode());
//                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));

            } catch (Exception e) {
                log.error("auth execute error:{}", e.getMessage(), e);
            } finally {
                machineService.removeCache(id);
            }
        });
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    public MachineAuthenticationReqDTO reader(DataPacket packet) {
        return MachineAuthenticationConvert.INSTANCE.convert(packet);
    }


    /**
     * 电桩加密封装
     *
     * @param reqDTO reqDTO
     */
    private void encryptionSecretKey(MachineAuthenticationReqDTO reqDTO, McAuthResp authResp) {
        if ((reqDTO.getBoardNum() & 0xff) != 160) {
            return;
        }
        authResp.setEncryptionType((byte) 1);
        String src = reqDTO.getIdCode() + authResp.getTime() + reqDTO.getMacAddress().toString();
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, "VB6dQCFh2F9ZyNg7".getBytes());
        byte[] digest = mac.digest(src);
        authResp.setSecretKey(new FixString(digest));
        authResp.setSecretKeyLength((short) digest.length);
        String encodeHexStr = HexExtUtil.encodeHexStr(digest, false);
        log.info("encryption :{}", encodeHexStr);
    }

    /**
     * 二维码下发
     */
    private void sendQrCode(ProtocolCPacket packet, ChannelHandlerContext ctx, int gunCount) {
        byte type = (byte) 0x9c;
        String url = machineService.getQrCode();
        byte[] urlB = url.getBytes(StandardCharsets.US_ASCII);
        byte urlLen = (byte) urlB.length;
        for (int i = 0; i < gunCount; i++) {
            Integer latestOrderV = ctx.attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
            latestOrderV++;
            ctx.attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(latestOrderV);
            ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
            bfB.writeByte(i);
            bfB.writeByte(urlLen);
            bfB.writeBytes(urlB);
            ByteBuf responseB = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), latestOrderV, type);
            log.info("write  0x9c {} ", BinaryViews.bfToHexStr(responseB));
            ctx.writeAndFlush(responseB).addListener((f) -> {
                if (!f.isSuccess()) {
                    log.error("write 0x9c error");
                    f.cause().printStackTrace();
                }
            });
        }

    }
}
