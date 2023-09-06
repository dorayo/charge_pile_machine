package com.huamar.charge.machine.client.controller;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.*;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.machine.client.handle.PacketProtocolCodec;
import com.huamar.charge.machine.client.starter.MachineClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tio.client.ClientChannelContext;
import org.tio.core.Tio;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命令测试
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Api(tags = "client")
@Slf4j
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ServerController {

    private final MachineClient machineClient;

    private final AtomicInteger messageNumber = new AtomicInteger(0);

    @ApiOperation(value="数据汇报-充电桩实时状态")
    @SneakyThrows
    @PostMapping("/sendPileOnlineStatus")
    public Object sendPileOnlineStatus(@RequestParam String id, @RequestParam String body) {
        ClientChannelContext channelContext = machineClient.getClientChannelContext();
        PacketProtocolCodec protocolCodec = new PacketProtocolCodec();

        // 转码翻译数据包
        byte[] decodeHex = HexExtUtil.decodeHex(StringUtils.deleteWhitespace(body));
        byte[] bytes = protocolCodec.transferEncode(decodeHex);
        DataPacketReader reader = new DataPacketReader(bytes);
        DataPacket decode = (DataPacket) protocolCodec.decode(reader.getBuffer());

        // 写入数据包
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(decode.getMsgBody());
        DataPacket packet = PacketBuilder.builder()
                .messageNumber(getMessageNumber())
                .messageId("34")
                .idCode(id)
                .body(writer)
                .build();


        Tio.send(channelContext, packet);
        return "ok";
    }


    /**
     * 获取消息流水号
     *
     * @return Short
     */
    private Short getMessageNumber() {
        int andIncrement = messageNumber.getAndIncrement();
        if (Objects.equals(andIncrement, 65535)) {
            messageNumber.set(0);
            return Convert.toShort(messageNumber.getAndIncrement());
        }
        return Convert.toShort(andIncrement);
    }

}
