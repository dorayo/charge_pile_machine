package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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


    private final  RedissonClient redissonClient;

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
        DataPacket packet = this.packet(command);
        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("Electricity Price idCode:{} sendCommand:{} msgId:{} ", command.getIdCode(), sendCommand,packet.getMsgNumber());
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
        switch (type) {
            case B:
                writer.write(command.getGunSort());
                writer.write((short)(command.getPrice1()/100));
                writer.write((short)(command.getPrice2()/100));
                writer.write((short)(command.getPrice3()/100));
                writer.write((short)(command.getPrice4()/100));
                writer.write(command.getTimeStage());
                writer.write((short) (command.getServicePrice1()/100));
                CacheKeyEnum keyEnum = CacheKeyEnum.MACHINE_SERVICE_PRICE;
                String key = command.getIdCode();
                key = keyEnum.joinKey(key);
                RBucket<McElectricityPriceCommandDTO> bucket = redissonClient.getBucket(key);
                bucket.set(command, keyEnum.getDuration().toMillis(), TimeUnit.MILLISECONDS);
                break;
            default:
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
