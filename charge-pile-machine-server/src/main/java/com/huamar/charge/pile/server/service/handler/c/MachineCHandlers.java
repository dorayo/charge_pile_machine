package com.huamar.charge.pile.server.service.handler.c;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.entity.dto.ChargeStageDataDTO;
import com.huamar.charge.pile.entity.dto.McChargerOnlineInfoDTO;
import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeFinishEventPushDTO;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.huamar.charge.pile.enums.NAttrKeys.SERVICE_PRICE_RATIOS;

/**
 * The type Machine c handlers.
 */
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

    /**
     * Handler 0 x 05.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
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

    /**
     * Handler 0 x 09.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x09(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();

            PileDTO update = new PileDTO();

            ctx.channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).set(packet);
            update.setPileCode(bsId);
            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
        } catch (Exception e) {
        }
    }

    /**
     * Handler 0 x 33.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
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

    /**
     * Handler 0 x 35.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x35(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            byte[] body = packet.getBody();
            byte gunShort = body[8];
            int isSuccess = body[9];
            onlineInfoDto.setIdCode(bsId);
            onlineInfoDto.setGunSort(gunShort);
            if (isSuccess == 0) {
                log.error("stop charge failed reason is {}", body[9]);
                return;
            }
            onlineInfoDto.setGunState((byte) 0x05);
            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDto);
            messageData.setBusinessId(bsId);
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * Handler 0 x 13.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x13(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            byte[] body = packet.getBody();
            int bodyLen = body.length;
            byte gunShort = body[16 + 7];
            byte state = body[16 + 8];
            byte isCon = body[16 + 10];
            int cVoltage = (int) BinaryViews.shortViewLe(body, 16 + 10 + 1);
            short cStream = (short) ((short) 1600 - (short) BinaryViews.shortViewLe(body, 16 + 10 + 1 + 2));
            int currentMoney = BinaryViews.intViewLe(body, bodyLen - 6);
            int powerCount = BinaryViews.intViewLe(body, bodyLen - 14);
            int resetTime = (int) BinaryViews.shortViewLe(body, bodyLen - 16);
            int useTime = (int) BinaryViews.shortViewLe(body, bodyLen - 18);

            if (currentMoney < 0) {
                log.info("error negative{}", currentMoney);
                return;
            }
            onlineInfoDto.setIdCode(ctx.channel().attr(machineId).get());
            onlineInfoDto.setGunSort(gunShort);
            onlineInfoDto.setGunState((byte) 0);
            onlineInfoDto.setCurMoney((currentMoney));
            onlineInfoDto.setCumulativeTime(useTime * 60);
            onlineInfoDto.setCurChargeQuantity(powerCount);
            if (useTime != 0) {
                onlineInfoDto.setStartTime(BCDUtils.timeToBCD(LocalDateTime.now().minus(useTime, ChronoUnit.MINUTES)));
            }
            ChargeStageDataDTO chargeStageDataDTO = new ChargeStageDataDTO();
            chargeStageDataDTO.setGunSort(gunShort);
            chargeStageDataDTO.setIdCode(onlineInfoDto.getIdCode());
            chargeStageDataDTO.setRemainChargeTime((short) (resetTime * 60));
            chargeStageDataDTO.setPileElectricityOutValue(cStream);
            chargeStageDataDTO.setPileVoltageOutValue((short) cVoltage);
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

            MessageData<ChargeStageDataDTO> messageData1 = new MessageData<>(MessageCodeEnum.PILE_CHARGE_STAGE, chargeStageDataDTO);
            messageData1.setBusinessId(onlineInfoDto.getIdCode());
            messageData1.setMessageId(IdUtil.simpleUUID());
            messageData1.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
            pileMessageProduce.send(messageData1);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * Handler 0 x 9 b.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x9B(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            byte[] body = packet.getBody();
            log.info("{} set gun {}={}", packet.getId(), body[7], body[8]);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * Handler 0 x 3 b.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x3b(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        Map<String, Object> params = new HashMap<>();
        try {
            float[] ratios = ctx.channel().attr(SERVICE_PRICE_RATIOS).get();
            if (ratios == null) {
                return;
            }
            int currenServiceRatioI = 0;
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            byte[] oldBody = packet.getBody();
            int bodyLen = oldBody.length;
            int priceStartIndex = 16 + 7 + 14;

            byte[] orderNumberBytes = new byte[16];
            System.arraycopy(oldBody, 0, orderNumberBytes, 0, 16);
            String orderNumber = cn.hutool.core.codec.BCD.bcdToStr(orderNumberBytes);
            params.put("orderNumber", orderNumber);

            long firstPrice = BinaryViews.intViewLe(oldBody, priceStartIndex + 12);
            params.put("firstPrice", firstPrice);

            long secondPrice = BinaryViews.intViewLe(oldBody, priceStartIndex + 12 + 16);
            params.put("secondPrice", secondPrice);

            long thirdPrice = BinaryViews.intViewLe(oldBody, priceStartIndex + 12 + 16 * 2);
            params.put("thirdPrice", thirdPrice);


            long forthPrice = BinaryViews.intViewLe(oldBody, priceStartIndex + 12 + 16 * 3);
            params.put("forthPrice", forthPrice);

//            if (firstPrice != 0) {
//                currenServiceRatioI = 0;
//            }
            if (secondPrice != 0) {
                currenServiceRatioI = 1;
            }
            if (thirdPrice != 0) {
                currenServiceRatioI = 2;
            }
            if (forthPrice != 0) {
                currenServiceRatioI = 3;
            }
            int totalPriceStartIndex = bodyLen - 8 - 1 - 7 - 1 - 17 - 4;
            int gunShort = oldBody[16 + 7];
            byte[] body = new byte[17];
            byte[] startTimeBt = new byte[7];
            byte[] endTimeBt = new byte[7];
            System.arraycopy(oldBody, 0, body, 0, 16);
            long powerCount = BinaryViews.intViewLe(oldBody, totalPriceStartIndex - 8);
            long total = BinaryViews.intViewLe(oldBody, totalPriceStartIndex);
            int endReason = oldBody[bodyLen - 9];
            System.arraycopy(oldBody, 16 + 7, startTimeBt, 0, 7);
            System.arraycopy(oldBody, 16 + 7 + 7, endTimeBt, 0, 7);
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(body, packet.getOrderVBf(), (byte) 0x40);
            log.info("response {} type=0x40 ", BinaryViews.bfToHexStr(response));
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write {}  success", "0x40");
                } else {
                    log.error("write {}  error", "0x40", f.cause());
                }
            });
            log.info("事件汇报：0x3b  消费金额{}", total);
            Date startTD = Cp56Time2aUtil.toDate(startTimeBt);
            Date endTD = Cp56Time2aUtil.toDate(endTimeBt);
            LocalDateTime startT = LocalDateTime.ofInstant(startTD.toInstant(), ZoneId.systemDefault());
            LocalDateTime endT = LocalDateTime.ofInstant(endTD.toInstant(), ZoneId.systemDefault());
            BCD startTime = BCDUtils.timeToBCD(startT);
            BCD endTime = BCDUtils.timeToBCD(endT);
            PileChargeFinishEventPushDTO eventPushDTO = new PileChargeFinishEventPushDTO();
            eventPushDTO.setStartTime(startTime);
            eventPushDTO.setIdCode(idCode);
            eventPushDTO.setEventStartTime(startTime.toString());
            eventPushDTO.setEventEndTime(endTime.toString());
            eventPushDTO.setEventState(2);
            eventPushDTO.setOutPower((int) (powerCount));
            eventPushDTO.setEndReason(endReason);
            eventPushDTO.setCumulativeChargeTime((int) (endTD.getTime() / 60000 - startTD.getTime() / 60000));
            eventPushDTO.setGunSort(gunShort);
            eventPushDTO.setServiceMoney((int) (total * (ratios[currenServiceRatioI])));
            eventPushDTO.setChargeMoney((int) (total - eventPushDTO.getServiceMoney()));
            eventPushDTO.setOrderSerialNumber(orderNumber);
            MessageData<PileChargeFinishEventPushDTO> messageData = new MessageData<>(eventPushDTO);
            messageData.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
            messageData.setBusinessId(idCode);
            log.info("{}", eventPushDTO);
            pileMessageProduce.send(messageData);
//            PileChargeFinishEventDTO eventDTO = this.parse(reqDTO);
//            log.info("事件汇报：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(eventDTO));
//            PileChargeFinishEventPushDTO eventPushDTO = PileChargeFinishEventConvert.INSTANCE.convert(eventDTO);
//            PileChargeFinishEventConvert.INSTANCE.copyBaseField(eventPushDTO, reqDTO);
//            MessageData<PileChargeFinishEventPushDTO> messageData1 = new MessageData<>(eventPushDTO);
//            messageData1.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
//            messageData1.setBusinessId(idCode);
//            pileMessageProduce.send(messageData1);

        } catch (Exception e) {
            log.error("sendMessage send error e:{}", ExceptionUtils.getMessage(e), e);
        }finally {
            log.info("YKC-订单结束事件 params:{}", params);
        }
    }

    /**
     * Handler 0 x 55.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x55(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            log.info("{} set time success", packet.getId());
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }
}
