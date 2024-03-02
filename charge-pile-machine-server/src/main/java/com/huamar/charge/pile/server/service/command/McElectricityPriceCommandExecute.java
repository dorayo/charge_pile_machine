package com.huamar.charge.pile.server.service.command;

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
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Objects;


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


        //YKC 计费模型
        if (type == McTypeEnum.C) {
            log.info("YKC 充电计费 充电计费请求下发 0xA0 >>> ");
            byte[] timeStages = command.getTimeStage().toString().getBytes();
            for (int i = timeStages.length - 1; i >= 0; i--) {
                timeStages[i] = (byte) ((timeStages[i] - 0x30) % 4);
            }

            YKCChargePrice ykcChargePrice = getYkcChargePrice(command);

            log.info("YKC 充电计费信息  hex time:{} hex newTime:{}", HexExtUtil.encodeHexStr(timeStages), HexExtUtil.encodeHexStr(command.getPriceBucketJFPG()));

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
        if(Objects.equals(type, McTypeEnum.B)){
            Short messageNum = command.headMessageNum();
            messageNum = Objects.isNull(messageNum) ? SessionManager.getMessageNumber(command.getIdCode()) : messageNum;
            command.headMessageNum(messageNum);
            packet.setMsgNumber(messageNum);
        }

        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("Electricity Price idCode:{} sendCommand:{} msgNumber:{} ", command.getIdCode(), sendCommand, packet.getMsgNumber());
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

        McCommandEnum commandEnum = getCode();
        short typeCode = Short.parseShort(commandEnum.getCode());
        DataPacketWriter writer = new DataPacketWriter();
        // 2024/02/19 修改电价服务费协议，直流旧协议，交流新协议 developer TiAmo
        if (Objects.requireNonNull(type) == McTypeEnum.B) {
            Assert.notNull(sessionChannel, "Electric price convert sessionChannel is null");
            Assert.notNull(sessionChannel.channel(), "Electric price convert sessionChannel channel is null");
            Channel channel = sessionChannel.channel().channel();

            AttributeKey<Integer> elePriceType = AttributeKey.valueOf(ConstEnum.ELE_CHARG_TYPE.getCode());
            Integer priceType = channel.attr(elePriceType).get();
            if(Objects.isNull(priceType)){
                log.warn("Electric price convert priceType is null");
                SessionManager.close(sessionChannel);
                throw new RuntimeException("Electric price convert priceType is null");
            }

            if(Objects.equals(priceType, 1)){
                writer.write(command.getGunSort());
                writer.write((short) (command.getSlxChargePrice()[0] / 100));
                writer.write((short) (command.getSlxChargePrice()[1] / 100));
                writer.write((short) (command.getSlxChargePrice()[2] / 100));
                writer.write((short) (command.getSlxChargePrice()[3] / 100));
                writer.write(command.getPriceStage().getBytes(StandardCharsets.US_ASCII));

                writer.write((short) (command.getSlxServicePrice()[0] / 100));


//                CacheKeyEnum keyEnum = CacheKeyEnum.MACHINE_SERVICE_PRICE;
//                String key = command.getIdCode();
//                key = keyEnum.joinKey(key);
//                RBucket<McElectricityPriceCommandDTO> bucket = redissonClient.getBucket(key);
//                bucket.set(command, keyEnum.getDuration().toMillis(), TimeUnit.MILLISECONDS);

                // 会话存储电价信息
                AttributeKey<McElectricityPriceCommandDTO> priceAttr = AttributeKey.valueOf(ConstEnum.COMMON_CHARGE_PRICE.getCode());
                channel.attr(priceAttr).set(command);
                McCommandDTO commandDTO = new McCommandDTO(typeCode, (byte) (1 + 10 + 48), writer.toByteArray());
                log.info("GH Type B priceType:{} ElectricityPriceCommand:{}", priceType, JSONParser.jsonString(commandDTO));
                return commandDTO;
            }

            if(Objects.equals(priceType, 2)){
                writer.write((byte) 0);
                writer.write((short) (command.getSlxChargePrice()[0] / 100));
                writer.write((short) (command.getSlxChargePrice()[1] / 100));
                writer.write((short) (command.getSlxChargePrice()[2] / 100));
                writer.write((short) (command.getSlxChargePrice()[3] / 100));
                writer.write((short) (command.getSlxChargePrice()[4] / 100));
                writer.write((short) (command.getSlxChargePrice()[5] / 100));


                writer.write((short) (command.getSlxServicePrice()[0] / 100));
                writer.write((short) (command.getSlxServicePrice()[1] / 100));
                writer.write((short) (command.getSlxServicePrice()[2] / 100));
                writer.write((short) (command.getSlxServicePrice()[3] / 100));
                writer.write((short) (command.getSlxServicePrice()[4] / 100));
                writer.write((short) (command.getSlxServicePrice()[5] / 100));
                writer.write(command.getPriceStage().getBytes(StandardCharsets.US_ASCII));

                AttributeKey<McElectricityPriceCommandDTO> priceAttr = AttributeKey.valueOf(ConstEnum.COMMON_CHARGE_PRICE.getCode());
                channel.attr(priceAttr).set(command);

                McCommandDTO commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
                log.info("GH Type B priceType:{} ElectricityPriceCommand:{}", priceType, JSONParser.jsonString(commandDTO));
                return commandDTO;
            }

            log.warn("Electric price convert priceType is invalid");
            SessionManager.close(sessionChannel);
            throw new RuntimeException("Electric price convert priceType is invalid");
        }

        if (Objects.requireNonNull(type) == McTypeEnum.A) {
            writer.write(command.getGunSort());
            writer.write((short) command.getSlxChargePrice()[0]);
            writer.write((short) command.getSlxChargePrice()[1]);
            writer.write((short) command.getSlxChargePrice()[2]);
            writer.write((short) command.getSlxChargePrice()[3]);
            writer.write((short) command.getSlxChargePrice()[4]);
            writer.write((short) command.getSlxChargePrice()[5]);


            writer.write((short) command.getSlxServicePrice()[0]);
            writer.write((short) command.getSlxServicePrice()[1]);
            writer.write((short) command.getSlxServicePrice()[2]);
            writer.write((short) command.getSlxServicePrice()[3]);
            writer.write((short) command.getSlxServicePrice()[4]);
            writer.write((short) command.getSlxServicePrice()[5]);
            writer.write(command.getPriceStage().getBytes(StandardCharsets.UTF_8));

            McCommandDTO commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
            log.info("SLX Type:A ElectricityPriceCommandDTO:{}", JSONParser.jsonString(commandDTO));
            return commandDTO;
        }

        McCommandDTO commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
        log.info("YKC ElectricityPriceCommandDTO:{} type:{}", JSONParser.jsonString(commandDTO), type);
        return commandDTO;
    }

}
