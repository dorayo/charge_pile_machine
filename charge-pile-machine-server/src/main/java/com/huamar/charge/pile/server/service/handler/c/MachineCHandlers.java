package com.huamar.charge.pile.server.service.handler.c;

import cn.hutool.core.util.IdUtil;
import com.alibaba.druid.sql.visitor.functions.Bin;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.convert.PileChargeFinishEventConvert;
import com.huamar.charge.pile.entity.dto.McChargerOnlineInfoDTO;
import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeFinishEventPushDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileEventPushBaseDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCHandlers {
    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;

    public void handler0x05(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            int requestBodyLen = packet.getBody().length;
            byte requestBodyType = packet.getBodyType();
            byte responseBodyType = (byte) (requestBodyType + 1);
            byte[] responseBody = Arrays.copyOf(packet.getBody(), requestBodyLen + 1);
            responseBody[requestBodyLen] = 1;
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(responseBody, packet.getOrderVBf(), responseBodyType);
            log.info("response {} type={} ", BinaryViews.bfToHexStr(response), responseBodyType);

//            PileDTO update = new PileDTO();
//            update.setPileCode(packet.getId());
//            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write {}  success", responseBodyType);
                } else {
                    log.error("write {}  error", responseBodyType);
                    f.cause().printStackTrace();
                }
            });
        } catch (Exception e) {
        }
    }

    public void handler0x09(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            PileDTO update = new PileDTO();
            ctx.channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).set(packet);
            update.setPileCode(BinaryViews.bcdViewsLe(packet.getIdBody()));
            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
        } catch (Exception e) {
        }
    }

    public void handler0x33(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            byte[] body = packet.getBody();
            int isSuccess = body[16 + 7 + 1];
            byte gunShort = body[16 + 7];
            onlineInfoDto.setIdCode(packet.getId());
            onlineInfoDto.setGunSort(gunShort);
            if (isSuccess == 0) {
                MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_STOP_CHARGE, onlineInfoDto);
                onlineInfoDto.setGunState((byte) 0x05);
                messageData.setBusinessId(onlineInfoDto.getIdCode());
                messageData.setMessageId(IdUtil.simpleUUID());
                messageData.setRequestId(IdUtil.simpleUUID());
                log.error("start charge failed reason is {}", body[16 + 7 + 2]);
                pileMessageProduce.send(messageData);
                return;
            }
            onlineInfoDto.setGunState((byte) 0x04);
            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDto);
            messageData.setBusinessId(onlineInfoDto.getIdCode());
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    public void handler0x35(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            byte[] body = packet.getBody();
            byte gunShort = body[8];
            int isSuccess = body[9];
            onlineInfoDto.setIdCode(packet.getId());
            onlineInfoDto.setGunSort(gunShort);
            if (isSuccess == 0) {
                log.error("stop charge failed reason is {}", body[9]);
                return;
            }
            onlineInfoDto.setGunState((byte) 0x05);
            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDto);
            messageData.setBusinessId(onlineInfoDto.getIdCode());
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    public void handler0x13(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            byte[] body = packet.getBody();
            int bodyLen = body.length;
            byte gunShort = body[16 + 7];
            byte state = body[16 + 8];
            byte isCon = body[16 + 10];
            long currentMoney = BinaryViews.intViewLe(body, bodyLen - 6);
            onlineInfoDto.setIdCode(ctx.channel().attr(machineId).get());
            onlineInfoDto.setGunSort(gunShort);
            onlineInfoDto.setGunState((byte) 0);
            onlineInfoDto.setCurMoney((int) (currentMoney / 100));
            log.info("state={} isCon={} cMoney = {}", state, isCon, currentMoney);
            switch (state) {
                case 0:
                    break;
                case 1:
                    onlineInfoDto.setGunState((byte) 6);
                    break;
                case 2:
                    onlineInfoDto.setGunState((byte) 0);
                    break;
                case 3:
                    onlineInfoDto.setGunState((byte) 4);
                    break;
            }
            if (isCon == 1 && onlineInfoDto.getGunState() == 0) {
                onlineInfoDto.setGunState((byte) 1);
            }
            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDto);
            messageData.setBusinessId(onlineInfoDto.getIdCode());
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    public void handler0x9B(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            byte[] body = packet.getBody();
            log.info("{} set gun {}={}", packet.getId(), body[7], body[8]);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    public void handler0x3b(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            byte[] oldBody = packet.getBody();
            int gunShort = oldBody[16 + 7];
            byte[] body = new byte[17];
            byte[] chargeOrder = new byte[16];
            byte[] startTimeBt = new byte[7];
            byte[] endTimeBt = new byte[7];
            int priceStartIndex = 16 + 7 + 1 + 7 + 7 + 15 * 4 + 18 - 1;
            long total = BinaryViews.intViewLe(oldBody, priceStartIndex);
            for (int i = 0; i < 16; i++) {
                chargeOrder[i] = body[i] = oldBody[i];
            }
            for (int i = 0, oldIndex = 16 + 7; i < 7; i++) {
                startTimeBt[i] = oldBody[i + oldIndex];
            }
            for (int i = 0, oldIndex = 16 + 7 + 7; i < 7; i++) {
                endTimeBt[i] = oldBody[i + oldIndex];
            }
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(body, packet.getOrderVBf(), (byte) 0x40);
            log.info("事件汇报：0x3b  消费金额{}", total);
//            BCD startTime = BCDUtils.timeToBCD(LocalDateTime.ofInstant(Cp56Time2aUtil.toDate(startTimeBt).toInstant(), ZoneId.systemDefault()));
//            BCD endTime = BCDUtils.timeToBCD(LocalDateTime.ofInstant(Cp56Time2aUtil.toDate(endTimeBt).toInstant(), ZoneId.systemDefault()));
//            PileChargeFinishEventPushDTO eventPushDTO = new PileChargeFinishEventPushDTO();
//            eventPushDTO.setStartTime(startTime);
//            eventPushDTO.setIdCode(idCode);
//            eventPushDTO.setEventStartTime(startTime.toString());
//            eventPushDTO.setEventEndTime(endTime.toString());
//            eventPushDTO.setEventState(2);
//            eventPushDTO.setOrderSerialNumber(BinaryViews.bfToHexStr(chargeOrder));
//            eventPushDTO.setGunSort(gunShort);
//            eventPushDTO.setServiceMoney((int) total);
//            eventPushDTO.setChargeMoney((int) total);
//            MessageData<PileChargeFinishEventPushDTO> messageData = new MessageData<>(eventPushDTO);
//            messageData.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
//            messageData.setBusinessId(idCode);
//            pileMessageProduce.send(messageData);
//            PileChargeFinishEventDTO eventDTO = this.parse(reqDTO);
//            log.info("事件汇报：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(eventDTO));
//
//
//            PileChargeFinishEventPushDTO eventPushDTO = PileChargeFinishEventConvert.INSTANCE.convert(eventDTO);
//            PileChargeFinishEventConvert.INSTANCE.copyBaseField(eventPushDTO, reqDTO);
//
//            MessageData<PileChargeFinishEventPushDTO> messageData = new MessageData<>(eventPushDTO);
//            messageData.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
//            messageData.setBusinessId(reqDTO.getIdCode());
//            pileMessageProduce.send(messageData);

            log.info("response {} type={} ", BinaryViews.bfToHexStr(response), 0x40);
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write {}  success", 0x40);
                } else {
                    log.error("write {}  error", 0x40);
                    f.cause().printStackTrace();
                }
            });
            log.info("{} set time success", packet.getId());
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    public void handler0x55(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            log.info("{} set time success", packet.getId());
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }
}
