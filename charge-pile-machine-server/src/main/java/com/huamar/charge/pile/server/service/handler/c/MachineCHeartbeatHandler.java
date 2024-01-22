package com.huamar.charge.pile.server.service.handler.c;

import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * 设备心跳包
 * 2023/08/01
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MachineCHeartbeatHandler {

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;


    public void handler(ProtocolCPacket packet, SessionChannel sessionChannel, ChannelHandlerContext ctx) {
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        String idCode = ctx.channel().attr(machineId).get();
        try {
            String ip = sessionChannel.getIp();
            byte[] body = packet.getBody();
            log.info("YKC 设备心跳包，ip={}, idCode:{}, gun:{}, status:{}, msgNum:{}", ip, idCode, body[7], body[8], packet.getOrderV());

            packet.getBody()[8] = 0;
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(packet.getBody(), packet.getOrderVBf(), (byte) 0x04);
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("YKC 设备心跳包回执 write heartbeat success");
                } else {
                    log.error("YKC 设备心跳包回执 write heartbeat error：{}", ExceptionUtils.getMessage(f.cause()));
                }
            });

            //v2024/01/22 懒加载对时间,跨天订单结算后对时
            try {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime loginTime = SessionManager.getLoginTime(ctx);
                if(Objects.isNull(loginTime)){
                    SessionManager.setLoginTimeNow(ctx);
                    loginTime = LocalDateTime.now();
                }

                // 转换为 LocalDate 对象，并比较是否在同一天
                LocalDate nowDay = now.toLocalDate();
                LocalDate loginTimeDay = loginTime.toLocalDate();
                boolean isSameDay = nowDay.isEqual(loginTimeDay);
                if(!isSameDay){
                    this.syncTime(ctx);
                }
            }catch (Exception e){
                String[] stackFrames = ExceptionUtils.getStackFrames(e);
                log.warn("writePacket error:{}", ExceptionUtils.getMessage(e));
                log.warn("writePacket stackTrace:{}", StringUtils.join(stackFrames, ",", 0, Math.min(stackFrames.length, 3)));
            }
        } catch (Exception e) {
            log.info("YKC 设备心跳包 heartbeat Error:{}", ExceptionUtils.getMessage(e), e);
        }

        try {
            PileHeartbeatDTO pileHeartbeatDTO = new PileHeartbeatDTO();
            pileHeartbeatDTO.setIdCode(idCode);
            pileHeartbeatDTO.setDateTime(LocalDateTime.now());
            MessageData<PileHeartbeatDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_HEART_BEAT, pileHeartbeatDTO);
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("心跳包发送远程失败 mcMessageProduce send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * syncTime
     */
    @SuppressWarnings("DuplicatedCode")
    public void syncTime(ChannelHandlerContext ctx) {
        byte type = (byte) 0x56;
        String idCode = SessionManager.getIdCode(ctx);
        Assert.notNull(idCode, "syncTime idCode is null");
        Short serialNumber = SessionManager.getYKCSerialNumber(ctx);
        byte[] cp56Time = Cp56Time2aUtil.dateToByte(new Date());

        ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
        bfB.writeBytes(new String(idCode.getBytes(), StandardCharsets.US_ASCII).getBytes());
        bfB.writeBytes(cp56Time);
        ByteBuf responseB = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(bfB), ByteExtUtil.shortToBytes(serialNumber, ByteOrder.LITTLE_ENDIAN), type);
        String dateStr = DateFormatUtils.format(Cp56Time2aUtil.toDate(cp56Time), "yyyy-MM-dd'T'HH:mm:ss");
        log.info("YKC Heartbeat syncTime write 0x56 serialNumber:{} syncTime:{} Hex {} ", serialNumber, dateStr, BinaryViews.bfToHexStr(responseB));

        ctx.writeAndFlush(responseB).addListener((f) -> {
            if (!f.isSuccess()) {
                log.error("YKC Heartbeat syncTime write 0x56 error:{}", ExceptionUtils.getMessage(f.cause()), f.cause());
            }
        });
    }

}
