package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ByteUtil;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.entity.dto.command.YKCChargePrice;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 远程控制执行-电价下发
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McElectricityPriceCommandExecute implements McCommandExecute<McElectricityPriceCommandDTO> {

    private final RedissonClient redissonClient;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommandEnum getCode() {
        return McCommandEnum.ELECTRICITY_PRICE;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McElectricityPriceCommandDTO command) {
        SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(command.getIdCode());
        Assert.notNull(sessionChannel, "sessionChannel ctx is null");
        McTypeEnum type = sessionChannel.getType();
        ChannelHandlerContext ctx = sessionChannel.channel();
        Assert.notNull(ctx, "sessionChannel ctx is null");

        AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
        String sessionId = ctx.channel().attr(sessionKey).get();
        MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);

        //YKC 计费模型
        if (type == McTypeEnum.C) {
            log.info("YKC 充电计费 充电计费请求下发 0xA0 >>> ");
            byte[] timeStages = command.getTimeStage().toString().getBytes();
            for (int i = timeStages.length - 1; i >= 0; i--) {
                timeStages[i] = (byte) ((timeStages[i] - 0x30) % 4);
            }

            YKCChargePrice ykcChargePrice = getYkcChargePrice(command);

            log.info("YKC 充电计费信息  hex timeBucket{} hex newTimeBucket{}", HexExtUtil.encodeHexStr(timeStages), HexExtUtil.encodeHexStr(command.getPriceBucketJFPG()));

            // 会话存储电价信息
            AttributeKey<YKCChargePrice> ykcPriceAttr = AttributeKey.valueOf(ConstEnum.YKC_CHARGE_PRICE.getCode());
            ctx.channel().attr(ykcPriceAttr).set(ykcChargePrice);


            byte[] idBody = sessionChannel.channel().channel().attr(NAttrKeys.ID_BODY).get();
            byte[] serialNumber;
            ProtocolCPacket packetC = sessionChannel.channel().channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).get();
            if (Objects.isNull(packetC) || packetC.getOrderVBf() == null) {
                Short number = NAttrKeys.getSerialNumber(sessionChannel);
                serialNumber = ByteExtUtil.shortToBytes(number, ByteUtil.DEFAULT_ORDER);
            } else {
                serialNumber = packetC.getOrderVBf();
            }

            ByteBuf responseBody = ByteBufAllocator.DEFAULT.heapBuffer(59);
            responseBody.writeBytes(idBody);
            responseBody.writeByte(0x01);
            responseBody.writeByte(0x00);

            responseBody.writeIntLE(command.getJPrice() * 10);
            responseBody.writeIntLE(command.getJPriceS() * 10);
            responseBody.writeIntLE(command.getFPrice() * 10);
            responseBody.writeIntLE(command.getFPriceS() * 10);
            responseBody.writeIntLE(command.getPPrice() * 10);
            responseBody.writeIntLE(command.getPPriceS() * 10);
            responseBody.writeIntLE(command.getGPrice() * 10);
            responseBody.writeIntLE(command.getGPriceS() * 10);
            responseBody.writeByte(0x00);
            responseBody.writeBytes(command.getPriceBucketJFPG());

            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(responseBody), serialNumber, (byte) 0x0A);
            String hexString = BinaryViews.bfToHexStr(response);
            sessionChannel.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("YKC 充电计费信息 0x0A write success hex:{}", hexString);
                } else {
                    log.info("YKC 充电计费信息 0x0A write error hex:{} e:{}", hexString, ExceptionUtils.getMessage(f.cause()));
                }
            });
            return;
        }

        //默认协议
        DataPacket packet = this.packet(command);
        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("Electricity Price idCode:{} sendCommand:{} msgId:{} ", command.getIdCode(), sendCommand, packet.getMsgNumber());
    }


    /**
     * YKC 电价
     *
     * @param command command
     * @return YKCChargePrice
     */
    private YKCChargePrice getYkcChargePrice(McElectricityPriceCommandDTO command) {
        YKCChargePrice ykcChargePrice = new YKCChargePrice();
        ykcChargePrice.setJPrice(command.getJPrice());
        ykcChargePrice.setFPrice(command.getFPrice());
        ykcChargePrice.setPPrice(command.getPPrice());
        ykcChargePrice.setGPrice(command.getGPrice());

        ykcChargePrice.setJPriceS(command.getJPriceS());
        ykcChargePrice.setFPriceS(command.getFPriceS());
        ykcChargePrice.setPPriceS(command.getPPriceS());
        ykcChargePrice.setGPriceS(command.getGPriceS());
        ykcChargePrice.setPriceBucketJFPG(command.getPriceBucketJFPG());
        return ykcChargePrice;
    }


    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McElectricityPriceCommandDTO command) {
        McTypeEnum type = McTypeEnum.A;
        SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(command.getIdCode());
        if (sessionChannel != null) {
            type = sessionChannel.getType();
        }

        short typeCode = Convert.toShort(getCode().getCode());
        DataPacketWriter writer = new DataPacketWriter();
        //noinspection SwitchStatementWithTooFewBranches
        log.info("电价信息:{}", JSONParser.jsonString(command));
        if (Objects.requireNonNull(type) == McTypeEnum.B) {
            writer.write(command.getGunSort());
            writer.write((short) (command.getPrice1() / 100));
            writer.write((short) (command.getPrice2() / 100));
            writer.write((short) (command.getPrice3() / 100));
            writer.write((short) (command.getPrice4() / 100));
            writer.write(command.getTimeStage());
            writer.write((short) (command.getServicePrice1() / 100));
            CacheKeyEnum keyEnum = CacheKeyEnum.MACHINE_SERVICE_PRICE;
            String key = command.getIdCode();
            key = keyEnum.joinKey(key);
            RBucket<McElectricityPriceCommandDTO> bucket = redissonClient.getBucket(key);
            bucket.set(command, keyEnum.getDuration().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            writer.write(command.getGunSort());
            writer.write(command.getPrice1());
            writer.write(command.getPrice2());
            writer.write(command.getPrice3());
            writer.write(command.getPrice4());
            writer.write(command.getPrice5());
            writer.write(command.getPrice6());
            writer.write(command.getServicePrice1());
            writer.write(command.getServicePrice2());
            writer.write(command.getServicePrice3());
            writer.write(command.getServicePrice4());
            writer.write(command.getServicePrice5());
            writer.write(command.getServicePrice6());
            writer.write(command.getTimeStage());
        }
        McCommandDTO commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
        log.info("McCommandDTO:{}", JSONParser.jsonString(commandDTO));
        return commandDTO;
    }

}
