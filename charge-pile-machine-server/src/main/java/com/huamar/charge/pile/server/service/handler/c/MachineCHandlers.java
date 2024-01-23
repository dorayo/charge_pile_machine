package com.huamar.charge.pile.server.service.handler.c;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.entity.dto.ChargeStageDataDTO;
import com.huamar.charge.pile.entity.dto.McChargerOnlineInfoDTO;
import com.huamar.charge.pile.entity.dto.charge.ChargeInfoDTO;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.entity.dto.command.YKCChargePrice;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeFinishEventPushDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.MessageCommonResultEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.service.charge.ChargeInfoService;
import com.huamar.charge.pile.server.service.command.MessageCommandRespService;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.huamar.charge.pile.enums.MessageCodeEnum.PILE_START_CHARGE;


/**
 * The type Machine c handlers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCHandlers {

    /**
     * 设备接口
     */
    private final MachineService machineService;

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;


    /**
     * 充电订单信息
     */
    private final ChargeInfoService chargeInfoService;

    /**
     * 消息应答处理
     */
    private final MessageCommandRespService messageCommandRespService;

    /**
     * Handler 0 x 05.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x05(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            int requestBodyLen = packet.getBody().length;
            byte requestBodyType = packet.getBodyType();
            byte responseBodyType = (byte) (requestBodyType + 1);
            byte[] responseBody = Arrays.copyOf(packet.getBody(), requestBodyLen + 1);
            responseBody[requestBodyLen] = 1;
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(responseBody, packet.getOrderVBf(), responseBodyType);
            log.info("充电计费 计费模型验证请求 0x05 ctx:{} >>> response {} type={} ", System.identityHashCode(ctx), BinaryViews.bfToHexStr(response), responseBodyType);

            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write {}  success", responseBodyType);
                } else {
                    log.error("write {}  error", responseBodyType);
                    log.error("handler0x05 error:{}", ExceptionUtils.getMessage(f.cause()), f.cause());
                }
            });
        } catch (Exception e) {
            log.error("handler0x05 error:{}", ExceptionUtils.getMessage(e), e);
        }
    }

    /**
     * Handler 0 x 09.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x09(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            log.info("充电计费 充电计费请求 ctx:{} 0x09 >>> ", System.identityHashCode(ctx));
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            ctx.channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).set(packet);
            PileDTO pile = machineService.getCache(idCode);

            PileDTO update = new PileDTO();
            update.setPileCode(idCode);
            update.setId(pile.getId());
            update.setStationId(pile.getStationId());
            update.setPileCode(pile.getPileCode());
            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));

        } catch (Exception e) {
            log.error("handler0x09 error:{}", ExceptionUtils.getStackFrames(e)[0]);
        }
    }

    /**
     * Handler 0 x 33.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x33(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        JSONObject dataJson = new JSONObject();
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        String idCode = ctx.channel().attr(machineId).get();
        try {
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            byte[] body = packet.getBody();
            int isSuccess = body[16 + 7 + 1];
            byte stopReason = body[16 + 7 + 1 + 1];

            // 流水号
            byte[] orderNumberBytes = new byte[16];
            System.arraycopy(body, 0, orderNumberBytes, 0, 16);
            String orderNumber = BCDUtils.bcdToStr(orderNumberBytes);

            dataJson.put("orderNumber", orderNumber);
            dataJson.put("isSuccess", isSuccess);
            dataJson.put("stopReason", stopReason);


            byte gunShort = body[16 + 7];
            dataJson.put("stopReason", stopReason);
            log.info("YKC start charge handler0x33 data:{}", dataJson);

            onlineInfoDto.setIdCode(packet.getId());
            onlineInfoDto.setGunSort(gunShort);
            onlineInfoDto.setStartTime(new BCD(new byte[]{0, 0, 0, 0, 0, 0}));
            if (isSuccess == 0) {
                MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_STOP_CHARGE, onlineInfoDto);
                onlineInfoDto.setGunState((byte) 0x05);
                messageData.setBusinessId(onlineInfoDto.getIdCode());
                messageData.setMessageId(IdUtil.simpleUUID());
                messageData.setRequestId(IdUtil.simpleUUID());
                log.error("YKC start charge failed reason is {}", body[16 + 7 + 2]);
                pileMessageProduce.send(messageData);

                // 启动充电回执
                int orderV = packet.getOrderV();
                MessageCommonRespDTO commonResp = new MessageCommonRespDTO();
                commonResp.setIdCode(idCode);
                commonResp.setCommandId(orderNumber);
                commonResp.setMsgResult(MessageCommonResultEnum.FAIL.getCode());
                commonResp.setMsgNumber(orderV);
                commonResp.setCommandTypeCode(PILE_START_CHARGE.getCode());
                messageCommandRespService.put(commonResp);
                messageCommandRespService.sendCommonResp(commonResp);
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
        }finally {
            log.info("启动充电结果 handler0x33 data:{}", dataJson);
        }
    }

    /**
     * Handler 0 x 35.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x35(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        JSONObject logJson = new JSONObject();
        try {
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            byte[] body = packet.getBody();
            byte gunShort = body[7];
            int isSuccess = body[8];
            int stopReason = body[9];
            onlineInfoDto.setIdCode(bsId);
            onlineInfoDto.setGunSort(gunShort);

            logJson.put("gunShort", gunShort);
            logJson.put("isSuccess", isSuccess);
            logJson.put("stopReason", stopReason);

            if (isSuccess == 0) {
                log.error("stop charge failed reason is {}", body[9]);
                return;
            }

            onlineInfoDto.setGunState((byte) 0x05);
            onlineInfoDto.setStartTime(new BCD(new byte[]{0, 0, 0, 0, 0, 0}));

            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDto);
            messageData.setBusinessId(bsId);
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }finally {
            if(log.isDebugEnabled()){
                log.debug("YKC 停止充电 handler0x35 data:{}", logJson);
            }
        }
    }

    /**
     * Handler 0 x 13.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x13(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        JSONObject infoData = new JSONObject();
        JSONObject infoDataNew = new JSONObject();
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
            byte[] body = packet.getBody();
            int bodyLen = body.length;
            byte gunShort = body[16 + 7];
            byte state = body[16 + 8];
            byte isCon = body[16 + 10];
            int cVoltage = (int) BinaryViews.shortViewLe(body, 16 + 10 + 1);
            infoData.put("cVoltage", cVoltage);
            infoData.put("gunShort", gunShort);

            short cStream = (short) ((short) 1600 - (short) BinaryViews.shortViewLe(body, 16 + 10 + 1 + 2));
            int currentMoney = BinaryViews.intViewLe(body, bodyLen - 6);
            infoData.put("currentMoney", currentMoney);

            int powerCount = BinaryViews.intViewLe(body, bodyLen - 14);
            infoData.put("powerCount", powerCount);
            infoDataNew.put("powerCount", powerCount);

            int resetTime = (int) BinaryViews.shortViewLe(body, bodyLen - 16);
            int useTime = (int) BinaryViews.shortViewLe(body, bodyLen - 18);
            BCD startTime = new BCD(new byte[]{0, 0, 0, 0, 0, 0});
            infoDataNew.put("useTime", useTime);
            if (powerCount > 0) {
                startTime = BCDUtils.timeToBCD(LocalDateTime.now().minusMinutes(useTime));
            }
            infoDataNew.put("startTime", startTime);

            if (currentMoney < 0) {
                log.info("error negative{}", currentMoney);
                return;
            }


            onlineInfoDto.setIdCode(ctx.channel().attr(machineId).get());
            onlineInfoDto.setGunSort(gunShort);
            onlineInfoDto.setGunState((byte) 0);
            onlineInfoDto.setCurMoney((currentMoney));
            onlineInfoDto.setCurChargeQuantity(powerCount);
            onlineInfoDto.setStartTime(startTime);


            ChargeStageDataDTO chargeStageDataDTO = new ChargeStageDataDTO();
            chargeStageDataDTO.setGunSort(gunShort);
            chargeStageDataDTO.setIdCode(onlineInfoDto.getIdCode());
            chargeStageDataDTO.setPileElectricityOutValue(cStream);
            chargeStageDataDTO.setPileVoltageOutValue((short) cVoltage);

            // 充电时长，预估时长
            onlineInfoDto.setCumulativeTime(useTime * 60);
            chargeStageDataDTO.setRemainChargeTime((short) (resetTime));


            //新版解析方式对比
            //index 27 电压
            infoDataNew.put("gunShort", gunShort);
            int chargeV = ByteExtUtil.bytesToShortUnsignedLE(new byte[]{body[27], body[28]});
            infoDataNew.put("chargeV", chargeV);

            byte soc = body[40];
            infoDataNew.put("soc", soc);

            int currentMoneyNew = ByteExtUtil.bytesToInt(new byte[]{body[54], body[55], body[56], body[57]});
            infoDataNew.put("currentMoney", currentMoneyNew);


            int remainChargeTime = ByteExtUtil.bytesToShortUnsignedLE(new byte[]{body[44], body[45]});
            infoDataNew.put("remainChargeTime", remainChargeTime);
            infoDataNew.put("resetTime", resetTime);
            chargeStageDataDTO.setRemainChargeTime((remainChargeTime));

            int fault = ByteExtUtil.bytesToShortUnsignedLE(new byte[]{body[58], body[59]});
            infoDataNew.put("fault", fault);
            infoDataNew.put("faultBinary", Integer.toBinaryString(fault));

            byte[] orderNumber = new byte[16];
            // 使用 System.arraycopy() 方法复制字节
            System.arraycopy(body, 0, orderNumber, 0, 16);
            String orderNumberStr = BCDUtils.bcdToStr(orderNumber);
            infoDataNew.put("orderNumberStr", orderNumberStr);

            switch (state) {

                case 0:
                    onlineInfoDto.setGunState((byte) -1);
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


            // 服务费服务端计算
            this.chargIngServicePrice(ctx, infoDataNew, onlineInfoDto);



            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDto);
            messageData.setBusinessId(onlineInfoDto.getIdCode());
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());


            chargeStageDataDTO.setElectricityState(soc);
            MessageData<ChargeStageDataDTO> messageData1 = new MessageData<>(MessageCodeEnum.PILE_CHARGE_STAGE, chargeStageDataDTO);
            messageData1.setBusinessId(onlineInfoDto.getIdCode());
            messageData1.setMessageId(IdUtil.simpleUUID());
            messageData1.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
            pileMessageProduce.send(messageData1);



        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("YKC 实时监测数据上传 handler 0x13 infoData   :{}", infoData);
                log.debug("YKC 实时监测数据上传 handler 0x13 infoDataNew:{}", infoDataNew);
            }
        }
    }


    /**
     * 服务费平台计算
     * @param ctx ctx
     * @param infoDataNew infoDataNew
     * @param onlineInfoDto onlineInfoDto
     */
    private void chargIngServicePrice(ChannelHandlerContext ctx, JSONObject infoDataNew, McChargerOnlineInfoDTO onlineInfoDto){
        String orderNumberStr = infoDataNew.getString("orderNumberStr");
        if(StringUtils.equals(orderNumberStr, "00000000000000000000000000000000")){
            return;
        }
        int powerCount = infoDataNew.getInteger("powerCount");
        int currentMoney = infoDataNew.getInteger("currentMoney");
        try {
            AttributeKey<YKCChargePrice> priceAttributeKey = AttributeKey.valueOf(ConstEnum.YKC_CHARGE_PRICE.getCode());
            YKCChargePrice price = null;
            // 自旋等待电价
            long startTimeGo = System.currentTimeMillis();
            long maxWaitTime = Duration.ofSeconds(1).toMillis();
            while (System.currentTimeMillis() - startTimeGo < maxWaitTime) {
                price = ctx.channel().attr(priceAttributeKey).get();
                boolean nonNull = Objects.nonNull(price);
                if (nonNull) {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
            if(Objects.isNull(price)){
                String station = (String) ctx.channel().attr(AttributeKey.valueOf(ConstEnum.STATION_ID.getCode())).get();
                Integer ele = (Integer) ctx.channel().attr(AttributeKey.valueOf(ConstEnum.ELE_CHARG_TYPE.getCode())).get();
                price = chargeInfoService.getPriceInfoForCache(Integer.valueOf(station), ele);
            }



            Assert.notNull(price, "YKCChargePrice is null");

            ChargeInfoDTO chargeInfo = chargeInfoService.get(orderNumberStr);
            if(Objects.isNull(chargeInfo)){
                chargeInfo = new ChargeInfoDTO();
            }

            // 获取当前时段的服务费价格
            LocalTime now = LocalTime.now();
            int index = now.toSecondOfDay() / 1800;
            int chargeS = price.getChargeS(price.getPriceBucketJFPG()[index]);

            infoDataNew.put("chargeS", chargeS);
            infoDataNew.put("chargeSIndex", index);

            // 当前节点的充电量
            int curChargeQuantity = chargeInfo.getCurChargeQuantity();
            int chargeStagePower = powerCount - curChargeQuantity;
            if(chargeStagePower <= 0){
                chargeStagePower = 0;
            }
            infoDataNew.put("powerCount", powerCount);
            infoDataNew.put("chargeStagePower", chargeStagePower);


            BigDecimal khUnit = BigDecimal.valueOf(10000);

            BigDecimal kwh = BigDecimal.valueOf(chargeStagePower)
                    .setScale(4, RoundingMode.HALF_UP);

            kwh = kwh.divide(khUnit, 4, RoundingMode.HALF_UP);
            BigDecimal servicePrice = kwh.multiply(BigDecimal.valueOf(chargeS));

            //订单服务费重新计算
            int servicePriceF = servicePrice.intValue();
            servicePriceF = servicePriceF + chargeInfo.getServiceMoney();

            int chargePriceF = currentMoney - servicePriceF;

            onlineInfoDto.setCurMoney(chargePriceF);
            onlineInfoDto.setServiceMoney(servicePriceF);
            infoDataNew.put("chargePriceF", chargePriceF);
            infoDataNew.put("servicePriceF", servicePriceF);

            // 本地缓存订单，二级缓存
            chargeInfo.setCurMoney(chargePriceF);
            chargeInfo.setServiceMoney(servicePriceF);
            chargeInfo.setOrderNumber(orderNumberStr);
            chargeInfo.setCurChargeQuantity(powerCount);
            chargeInfo.setCumulativeTime(onlineInfoDto.getCumulativeTime());
            chargeInfoService.put(chargeInfo);
        }catch (Exception e){
            log.error("YKC 0x13 服务端计算服务费失败 error:{}", ExceptionUtils.getMessage(e), e);
            // 异常不更新充电中数据，不设置充电时间
            onlineInfoDto.setStartTime(new BCD(new byte[]{0, 0, 0, 0, 0, 0}));
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
            log.info("QR Code ack handler 0x9B idCode:{} gun:{} ok:{} ctx:{}", packet.getId(), body[7], body[8], ctx.name());
        } catch (Exception e) {
            log.error("handler 0x9B error e:{}", e.getMessage(), e);
        }
    }

    /**
     * Handler 0xF1 二维码下发
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0xF1(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            byte[] body = packet.getBody();
            log.info("QR Code ack handler 0xF1 idCode:{} ok:{} ctx:{}", packet.getId(), body[7], ctx.name());
        } catch (Exception e) {
            log.error("QR Code ack handler0xF1 error e:{}", e.getMessage(), e);
        }
    }


    /**
     * Handler 0x59 二维码下发
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x59(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        try {
            byte[] body = packet.getBody();
            log.info("QR Code ack handler 0x59 idCode:{} gun:{} ok:{} ctx:{}", packet.getId(), body[7], body[8], ctx.name());
        } catch (Exception e) {
            log.error("QR Code ack handler0xF1 error e:{}", e.getMessage(), e);
        }
    }

    /**
     * Handler 0 x 3 b.
     *
     * @param packet the packet
     * @param ctx    the ctx
     */
    public void handler0x3b(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        JSONObject jsonLog = new JSONObject();
        try {

            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            byte[] oldBody = packet.getBody();
            int bodyLen = oldBody.length;
            int gunShort = oldBody[16 + 7];

            ByteBuf bodyBuf = ByteBufAllocator.DEFAULT.buffer(256);
            bodyBuf.writeBytes(oldBody);

            // 流水号
            byte[] orderNumberBytes = new byte[16];
            System.arraycopy(oldBody, 0, orderNumberBytes, 0, 16);
            String orderNumber = BCDUtils.bcdToStr(orderNumberBytes);
            jsonLog.put("orderNumber", orderNumber);

            long firstPrice = BinaryViews.intViewLe(oldBody, 50);
            jsonLog.put("priceJ", firstPrice);

            long secondPrice = BinaryViews.intViewLe(oldBody, 50 + 16);
            jsonLog.put("priceF", secondPrice);

            long thirdPrice = BinaryViews.intViewLe(oldBody, 50 + 16 * 2);
            jsonLog.put("priceP", thirdPrice);

            long forthPrice = BinaryViews.intViewLe(oldBody, 50 + 16 * 3);
            jsonLog.put("priceG", forthPrice);

            // 旧版本金额读取
            int totalPriceStartIndex = bodyLen - 8 - 1 - 7 - 1 - 17 - 4;
            long powerCount = BinaryViews.intViewLe(oldBody, totalPriceStartIndex - 8);
            long total = BinaryViews.intViewLe(oldBody, totalPriceStartIndex);

            // 新版本金额读取方法
            bodyBuf.readerIndex(102 + 10);
            long powerCountLE = bodyBuf.readUnsignedIntLE();
            bodyBuf.readerIndex(102 + 18);
            long totalLE = bodyBuf.readUnsignedIntLE();
            String vin = "";
            try {
                byte[] vinBytes = new byte[17];
                bodyBuf.readBytes(vinBytes);
                vin = new String(vinBytes, StandardCharsets.UTF_8);
            }catch (Exception ignored){

            }

            int endReason = oldBody[bodyLen - 9];

            jsonLog.put("total", total);
            jsonLog.put("totalLE", totalLE);
            jsonLog.put("powerCountLE", powerCountLE);
            jsonLog.put("powerCount", powerCount);
            jsonLog.put("endReason", endReason);


            //v2024/01/08 结束充电无效
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(256);
            byteBuf.writeBytes(oldBody, 0, 16);
            byteBuf.writeByte(0x00);

            jsonLog.put("orderNumber", BCDUtils.bcdToStr(ByteBufUtil.getBytes(byteBuf, 0, 16)));
            jsonLog.put("ok", 0x00);

            byte[] newBody = ByteBufUtil.getBytes(byteBuf);
            log.info("YKC YKC 充电订单上报 回执 response newBody {} type=0x40 readIndex:{}", BinaryViews.bfToHexStr(newBody), byteBuf.readerIndex());
            ByteBuf responseNew = BinaryBuilders.protocolCLeResponseBuilder(newBody, packet.getOrderVBf(), (byte) 0x40);

            // 旧响应值
            byte[] body = new byte[17];
            System.arraycopy(oldBody, 0, body, 0, 16);
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(body, packet.getOrderVBf(), (byte) 0x40);
            if(log.isDebugEnabled()){
                log.debug("YKC 充电订单上报 V1 response:{}", response);
            }

            ctx.channel().writeAndFlush(responseNew).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("YKC 充电订单上报 write success msgId:{}", "0x40");
                } else {
                    log.error("YKC 充电订单上报 write error msgId:{} error:{}", "0x40", ExceptionUtils.getMessage(f.cause()), f.cause());
                }
            });

            ChargeInfoDTO chargeInfo = chargeInfoService.get(orderNumber);
            if(Objects.isNull(chargeInfo)){
                chargeInfo = new ChargeInfoDTO();
            }

            // 开始结束时间
            byte[] startTimeBt = ByteBufUtil.getBytes(bodyBuf, 24, 7);
            byte[] endTimeBt = ByteBufUtil.getBytes(bodyBuf, 31, 7);
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
            eventPushDTO.setCumulativeChargeTime((int) Duration.between(startT, endT).getSeconds());
            eventPushDTO.setGunSort(gunShort);
            eventPushDTO.setOrderSerialNumber(orderNumber);
            eventPushDTO.setCarIdentificationCode(vin);

            if(chargeInfo.getCurChargeQuantity() != powerCountLE){
                if(log.isDebugEnabled()){
                    log.debug("YKC 充电订单结算(0x3b) 差异充电量服务费计算 充电中：{} kwh, 上报：{}", chargeInfo.getCurChargeQuantity(), powerCountLE);
                }
                try {
                    AttributeKey<YKCChargePrice> priceAttributeKey = AttributeKey.valueOf(ConstEnum.YKC_CHARGE_PRICE.getCode());
                    YKCChargePrice price = null;
                    // 自旋等待电价
                    long startTimeGo = System.currentTimeMillis();
                    long maxWaitTime = Duration.ofSeconds(1).toMillis();
                    while (System.currentTimeMillis() - startTimeGo < maxWaitTime) {
                        price = ctx.channel().attr(priceAttributeKey).get();
                        boolean nonNull = Objects.nonNull(price);
                        if (nonNull) {
                            break;
                        }
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                    if(Objects.isNull(price)){
                        String station = (String) ctx.channel().attr(AttributeKey.valueOf(ConstEnum.STATION_ID.getCode())).get();
                        Integer ele = (Integer) ctx.channel().attr(AttributeKey.valueOf(ConstEnum.ELE_CHARG_TYPE.getCode())).get();
                        price = chargeInfoService.getPriceInfoForCache(Integer.valueOf(station), ele);
                    }

                    Assert.notNull(price, "YKCChargePrice is null");

                    // 获取当前时段的服务费价格
                    LocalTime now = LocalTime.now();
                    int index = now.toSecondOfDay() / 1800;
                    int chargeS = price.getChargeS(price.getPriceBucketJFPG()[index]);

                    jsonLog.put("chargeS", chargeS);
                    jsonLog.put("chargeSIndex", index);

                    // 当前节点的充电量
                    int curChargeQuantity = chargeInfo.getCurChargeQuantity();
                    long chargeStagePower = powerCountLE - curChargeQuantity;
                    if(chargeStagePower <= 0){
                        chargeStagePower = 0;
                    }
                    jsonLog.put("powerCountLE", powerCountLE);
                    jsonLog.put("chargeStagePower", chargeStagePower);


                    BigDecimal khUnit = BigDecimal.valueOf(10000);
                    BigDecimal kwh = BigDecimal.valueOf(chargeStagePower)
                            .setScale(4, RoundingMode.HALF_UP);

                    kwh = kwh.divide(khUnit, 4, RoundingMode.HALF_UP);
                    BigDecimal servicePrice = kwh.multiply(BigDecimal.valueOf(chargeS));

                    //订单服务费重新计算
                    int servicePriceF = servicePrice.intValue();
                    servicePriceF = servicePriceF + chargeInfo.getServiceMoney();

                    long chargePriceF = totalLE - servicePriceF;

                    jsonLog.put("chargePriceF", chargePriceF);
                    jsonLog.put("servicePriceF", servicePriceF);

                    // 本地缓存订单，二级缓存
                    chargeInfo.setCurMoney((int) chargePriceF);
                    chargeInfo.setServiceMoney(servicePriceF);
                    chargeInfo.setCurChargeQuantity((int) powerCountLE);
                    chargeInfoService.put(chargeInfo);
                }catch (Exception e){
                    log.error("YKC 0x3B 订单结束服务费结算 error:{}", ExceptionUtils.getMessage(e), e);
                }

            }

            long chargeMoney = totalLE - chargeInfo.getServiceMoney();
            eventPushDTO.setServiceMoney(chargeInfo.getServiceMoney());
            eventPushDTO.setChargeMoney((int) chargeMoney);
            jsonLog.put("total", total);
            jsonLog.put("totalLe", totalLE);
            jsonLog.put("chargeMoney", chargeMoney);
            jsonLog.put("serviceMoney", chargeInfo.getServiceMoney());


            this.endReasonJson(eventPushDTO);

            MessageData<PileChargeFinishEventPushDTO> messageData = new MessageData<>(eventPushDTO);
            messageData.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
            messageData.setBusinessId(idCode);

            pileMessageProduce.send(messageData);
            log.info("YKC-订单结束事件：{}, data:{}", 0x3b, JSONParser.jsonString(eventPushDTO));

        } catch (Exception e) {
            log.error("YKC-订单结束事件 error：sendMessage send error e:{}", ExceptionUtils.getMessage(e), e);
        }finally {
            log.info("YKC-订单结束事件 params:{}", jsonLog);
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
            log.info("{} ctx:{} set time success", packet.getId(), ctx.name());
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }


    private final static Map<Byte, JSONObject> endReasonJson = new HashMap<>();

    static {
        endReasonJson.put((byte) 0x40, new JSONObject(ImmutableMap.of("code",0x40,"tag","YKC","text", "结束充电，APP 远程停止")));
        endReasonJson.put((byte) 0x41, new JSONObject(ImmutableMap.of("code",0x41,"tag","YKC","text", "结束充电，SOC 达到 100%")));
        endReasonJson.put((byte) 0x42, new JSONObject(ImmutableMap.of("code",0x42,"tag","YKC","text", "结束充电，充电电量满足设定条件")));
        endReasonJson.put((byte) 0x43, new JSONObject(ImmutableMap.of("code",0x43,"tag","YKC","text", "结束充电，充电金额满足设定条件")));
        endReasonJson.put((byte) 0x44, new JSONObject(ImmutableMap.of("code",0x44,"tag","YKC","text", "结束充电，充电时间满足设定条件")));
        endReasonJson.put((byte) 0x45, new JSONObject(ImmutableMap.of("code",0x45,"tag","YKC","text", "结束充电，手动停止充电")));
        endReasonJson.put((byte) 0x46, new JSONObject(ImmutableMap.of("code",0x46,"tag","YKC","text", "其他方式（预留）")));
        endReasonJson.put((byte) 0x47, new JSONObject(ImmutableMap.of("code",0x47,"tag","YKC","text", "其他方式（预留）")));
        endReasonJson.put((byte) 0x48, new JSONObject(ImmutableMap.of("code",0x48,"tag","YKC","text", "其他方式（预留）")));
        endReasonJson.put((byte) 0x49, new JSONObject(ImmutableMap.of("code",0x49,"tag","YKC","text", "其他方式（预留）")));


        endReasonJson.put((byte) 0x4A, new JSONObject(ImmutableMap.of("code",0x4A,"tag","YKC","text", "充电启动失败，充电桩控制系统故障(需要重启或自动恢复)")));
        endReasonJson.put((byte) 0x4B, new JSONObject(ImmutableMap.of("code",0x4B,"tag","YKC","text", "充电启动失败，控制导引断开")));
        endReasonJson.put((byte) 0x4C, new JSONObject(ImmutableMap.of("code",0x4C,"tag","YKC","text", "充电启动失败，断路器跳位")));
        endReasonJson.put((byte) 0x4D, new JSONObject(ImmutableMap.of("code",0x4D,"tag","YKC","text", "充电启动失败，电表通信中断")));
        endReasonJson.put((byte) 0x4E, new JSONObject(ImmutableMap.of("code",0x4E,"tag","YKC","text", "充电启动失败，余额不足")));
        endReasonJson.put((byte) 0x4F, new JSONObject(ImmutableMap.of("code",0x4F,"tag","YKC","text", "充电启动失败，充电模块故障")));
        endReasonJson.put((byte) 0x50, new JSONObject(ImmutableMap.of("code",0x50,"tag","YKC","text", "充电启动失败，急停开入")));
        endReasonJson.put((byte) 0x51, new JSONObject(ImmutableMap.of("code",0x51,"tag","YKC","text", "充电启动失败，防雷器异常")));
        endReasonJson.put((byte) 0x52, new JSONObject(ImmutableMap.of("code",0x52,"tag","YKC","text", "充电启动失败，BMS 未就绪")));
        endReasonJson.put((byte) 0x53, new JSONObject(ImmutableMap.of("code",0x53,"tag","YKC","text", "充电启动失败，温度异常")));
        endReasonJson.put((byte) 0x54, new JSONObject(ImmutableMap.of("code",0x54,"tag","YKC","text", "充电启动失败，电池反接故障")));
        endReasonJson.put((byte) 0x55, new JSONObject(ImmutableMap.of("code",0x55,"tag","YKC","text", "充电启动失败，电子锁异常")));
        endReasonJson.put((byte) 0x56, new JSONObject(ImmutableMap.of("code",0x56,"tag","YKC","text", "充电启动失败，合闸失败")));
        endReasonJson.put((byte) 0x57, new JSONObject(ImmutableMap.of("code",0x57,"tag","YKC","text", "充电启动失败，绝缘异常")));
        endReasonJson.put((byte) 0x58, new JSONObject(ImmutableMap.of("code",0x58,"tag","YKC","text", "预留")));
        endReasonJson.put((byte) 0x59, new JSONObject(ImmutableMap.of("code",0x59,"tag","YKC","text", "充电启动失败，接收 BMS 握手报文 BHM 超时")));
        endReasonJson.put((byte) 0x5A, new JSONObject(ImmutableMap.of("code",0x5A,"tag","YKC","text", "充电启动失败，接收 BMS 和车辆的辨识报文超时 BRM")));
        endReasonJson.put((byte) 0x5B, new JSONObject(ImmutableMap.of("code",0x5B,"tag","YKC","text", "充电启动失败，接收电池充电参数报文超时 BCP")));
        endReasonJson.put((byte) 0x5C, new JSONObject(ImmutableMap.of("code",0x5C,"tag","YKC","text", "充电启动失败，接收 BMS 完成充电准备报文超时 BRO AA")));
        endReasonJson.put((byte) 0x5D, new JSONObject(ImmutableMap.of("code",0x5D,"tag","YKC","text", "充电启动失败，接收电池充电总状态报文超时 BCS")));
        endReasonJson.put((byte) 0x5E, new JSONObject(ImmutableMap.of("code",0x5E,"tag","YKC","text", "充电启动失败，接收电池充电要求报文超时 BCL")));
        endReasonJson.put((byte) 0x5F, new JSONObject(ImmutableMap.of("code",0x5F,"tag","YKC","text", "充电启动失败，接收电池状态信息报文超时 BSM")));
        endReasonJson.put((byte) 0x60, new JSONObject(ImmutableMap.of("code",0x60,"tag","YKC","text", "充电启动失败，GB2015 电池在 BHM 阶段有电压不允许充电")));
        endReasonJson.put((byte) 0x61, new JSONObject(ImmutableMap.of("code",0x61,"tag","YKC","text", "充电启动失败，GB2015 辨识阶段在 BRO_AA 时候电池实际电压与BCP报文电池电压差距大于5%")));
        endReasonJson.put((byte) 0x62, new JSONObject(ImmutableMap.of("code",0x62,"tag","YKC","text", "充电启动失败，B2015 充电机在预充电阶段从 BRO_AA 变成BRO_00状态")));
        endReasonJson.put((byte) 0x63, new JSONObject(ImmutableMap.of("code",0x63,"tag","YKC","text", "充电启动失败，接收主机配置报文超时")));
        endReasonJson.put((byte) 0x64, new JSONObject(ImmutableMap.of("code",0x64,"tag","YKC","text", "充电启动失败，充电机未准备就绪,我们没有回 CRO AA，对应老国际")));
        endReasonJson.put((byte) 0x65, new JSONObject(ImmutableMap.of("code",0x65,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x66, new JSONObject(ImmutableMap.of("code",0x66,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x67, new JSONObject(ImmutableMap.of("code",0x67,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x68, new JSONObject(ImmutableMap.of("code",0x68,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x69, new JSONObject(ImmutableMap.of("code",0x69,"tag","YKC","text", "（其他原因）预留")));


        endReasonJson.put((byte) 0x6A, new JSONObject(ImmutableMap.of("code",0x6A,"tag","YKC","text", "充电异常中止，系统闭锁")));
        endReasonJson.put((byte) 0x6B, new JSONObject(ImmutableMap.of("code",0x6B,"tag","YKC","text", "充电异常中止，导引断开")));
        endReasonJson.put((byte) 0x6C, new JSONObject(ImmutableMap.of("code",0x6C,"tag","YKC","text", "充电异常中止，断路器跳位")));
        endReasonJson.put((byte) 0x6D, new JSONObject(ImmutableMap.of("code",0x6D,"tag","YKC","text", "充电异常中止，电表通信中断")));
        endReasonJson.put((byte) 0x6E, new JSONObject(ImmutableMap.of("code",0x6E,"tag","YKC","text", "充电异常中止，余额不足")));
        endReasonJson.put((byte) 0x6F, new JSONObject(ImmutableMap.of("code",0x6F,"tag","YKC","text", "充电异常中止，交流保护动作")));
        endReasonJson.put((byte) 0x70, new JSONObject(ImmutableMap.of("code",0x70,"tag","YKC","text", "充电异常中止，直流保护动作")));
        endReasonJson.put((byte) 0x71, new JSONObject(ImmutableMap.of("code",0x71,"tag","YKC","text", "充电异常中止，充电模块故障")));
        endReasonJson.put((byte) 0x72, new JSONObject(ImmutableMap.of("code",0x72,"tag","YKC","text", "充电异常中止，急停开入")));
        endReasonJson.put((byte) 0x73, new JSONObject(ImmutableMap.of("code",0x73,"tag","YKC","text", "充电异常中止，防雷器异常")));
        endReasonJson.put((byte) 0x74, new JSONObject(ImmutableMap.of("code",0x74,"tag","YKC","text", "充电异常中止，温度异常")));
        endReasonJson.put((byte) 0x75, new JSONObject(ImmutableMap.of("code",0x75,"tag","YKC","text", "充电异常中止，输出异常")));
        endReasonJson.put((byte) 0x76, new JSONObject(ImmutableMap.of("code",0x76,"tag","YKC","text", "充电异常中止，充电无流")));
        endReasonJson.put((byte) 0x77, new JSONObject(ImmutableMap.of("code",0x77,"tag","YKC","text", "充电异常中止，电子锁异常")));
        endReasonJson.put((byte) 0x78, new JSONObject(ImmutableMap.of("code",0x78,"tag","YKC","text", "预留")));
        endReasonJson.put((byte) 0x79, new JSONObject(ImmutableMap.of("code",0x79,"tag","YKC","text", "充电异常中止，总充电电压异常")));
        endReasonJson.put((byte) 0x7A, new JSONObject(ImmutableMap.of("code",0x7A,"tag","YKC","text", "充电异常中止，总充电电流异常")));
        endReasonJson.put((byte) 0x7B, new JSONObject(ImmutableMap.of("code",0x7B,"tag","YKC","text", "充电异常中止，单体充电电压异常")));
        endReasonJson.put((byte) 0x7C, new JSONObject(ImmutableMap.of("code",0x7C,"tag","YKC","text", "充电异常中止，电池组过温")));
        endReasonJson.put((byte) 0x7D, new JSONObject(ImmutableMap.of("code",0x7D,"tag","YKC","text", "充电异常中止，最高单体充电电压异常")));
        endReasonJson.put((byte) 0x7E, new JSONObject(ImmutableMap.of("code",0x7E,"tag","YKC","text", "充电异常中止，最高电池组过温")));
        endReasonJson.put((byte) 0x7F, new JSONObject(ImmutableMap.of("code",0x7F,"tag","YKC","text", "充电异常中止，BMV 单体充电电压异常")));
        endReasonJson.put((byte) 0x80, new JSONObject(ImmutableMap.of("code",0x80,"tag","YKC","text", "充电异常中止，BMT 电池组过温")));
        endReasonJson.put((byte) 0x81, new JSONObject(ImmutableMap.of("code",0x81,"tag","YKC","text", "充电异常中止，电池状态异常停止充电")));
        endReasonJson.put((byte) 0x82, new JSONObject(ImmutableMap.of("code",0x82,"tag","YKC","text", "充电异常中止，车辆发报文禁止充电")));
        endReasonJson.put((byte) 0x83, new JSONObject(ImmutableMap.of("code",0x83,"tag","YKC","text", "充电异常中止，充电桩断电")));
        endReasonJson.put((byte) 0x84, new JSONObject(ImmutableMap.of("code",0x84,"tag","YKC","text", "充电异常中止，接收电池充电总状态报文超时")));
        endReasonJson.put((byte) 0x85, new JSONObject(ImmutableMap.of("code",0x85,"tag","YKC","text", "充电异常中止，接收电池充电要求报文超时")));
        endReasonJson.put((byte) 0x86, new JSONObject(ImmutableMap.of("code",0x86,"tag","YKC","text", "充电异常中止，接收电池状态信息报文超时")));
        endReasonJson.put((byte) 0x87, new JSONObject(ImmutableMap.of("code",0x87,"tag","YKC","text", "充电异常中止，接收 BMS 中止充电报文超时")));
        endReasonJson.put((byte) 0x88, new JSONObject(ImmutableMap.of("code",0x88,"tag","YKC","text", "充电异常中止，接收 BMS 充电统计报文超时")));
        endReasonJson.put((byte) 0x89, new JSONObject(ImmutableMap.of("code",0x89,"tag","YKC","text", "充电异常中止，接收对侧 CCS 报文超时")));
        endReasonJson.put((byte) 0x8A, new JSONObject(ImmutableMap.of("code",0x8A,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x8B, new JSONObject(ImmutableMap.of("code",0x8B,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x8C, new JSONObject(ImmutableMap.of("code",0x8C,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x8D, new JSONObject(ImmutableMap.of("code",0x8D,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x8E, new JSONObject(ImmutableMap.of("code",0x8E,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x8F, new JSONObject(ImmutableMap.of("code",0x8F,"tag","YKC","text", "（其他原因）预留")));
        endReasonJson.put((byte) 0x90, new JSONObject(ImmutableMap.of("code",0x90,"tag","YKC","text", "未知原因停止")));
    }

    /**
     * 结束原因
     * @param eventPushDTO eventPushDTO
     */
    private void endReasonJson(PileChargeFinishEventPushDTO eventPushDTO){
        if(Objects.isNull(eventPushDTO.getEndReason())){
            eventPushDTO.setEndReason(-1);
        }
        JSONObject defaultJson = new JSONObject(ImmutableMap.of("code", eventPushDTO.getEndReason(), "tag", "YKC", "text", "结束充电不明原因：" + eventPushDTO.getEndReason()));
        JSONObject jsonObject = endReasonJson.getOrDefault((byte) eventPushDTO.getEndReason().intValue(), defaultJson);
        eventPushDTO.setEndReasonJson(jsonObject.toJSONString());
    }


}
