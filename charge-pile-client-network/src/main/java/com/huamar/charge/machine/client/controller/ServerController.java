package com.huamar.charge.machine.client.controller;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.*;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.machine.client.handle.ClientProtocolCodec;
import com.huamar.charge.machine.client.protocol.TioPacket;
import com.huamar.charge.machine.client.starter.MachineClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
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


    @ApiOperation(value="数据汇报-充电桩实时状态")
    @SneakyThrows
    @PostMapping("/sendPileOnlineStatus")
    public Object sendPileOnlineStatus(@RequestParam String id, @RequestParam String body) {
        ClientChannelContext channelContext = machineClient.getClientChannelContext();
        ClientProtocolCodec protocolCodec = new ClientProtocolCodec();

        // 转码翻译数据包
        byte[] decodeHex = HexExtUtil.decodeHex(StringUtils.deleteWhitespace(body));
        byte[] bytes = protocolCodec.transferEncode(decodeHex);
        DataPacketReader reader = new DataPacketReader(bytes);
        DataPacket decode = (DataPacket) protocolCodec.decode(reader.getBuffer());

        // 写入数据包
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(decode.getMsgBody());
        DataPacket packet = PacketBuilder.builder()
                .messageNumber(machineClient.getMessageNumber())
                .messageId("34")
                .idCode(id)
                .body(writer)
                .build();

        Tio.send(channelContext, new TioPacket(packet));
        return "ok";
    }


    @ApiOperation(value="sendCommand")
    @SneakyThrows
    @PostMapping("/sendCommand")
    public Object sendCommand(@RequestParam String body) {
        ClientChannelContext channelContext = machineClient.getClientChannelContext();
        ClientProtocolCodec protocolCodec = new ClientProtocolCodec();

        // 转码翻译数据包
        byte[] decodeHex = HexExtUtil.decodeHex(StringUtils.deleteWhitespace(body));
        byte[] bytes = protocolCodec.transferEncode(decodeHex);
        DataPacketReader reader = new DataPacketReader(bytes);

        DataPacket packet = new DataPacket();
        packet.setTag(reader.readByte());
        packet.setMsgId(reader.readByte());
        packet.setMsgBodyAttr(reader.readByte());
        packet.setMsgBodyLen(reader.readShort());
        Boolean aBoolean = reader.readPacket(packet, bytes);
        Assert.isTrue(aBoolean, "readPacket error");

        Tio.send(channelContext, new TioPacket(packet));
        return "ok";
    }


    public static void main(String[] args) {
        String body = "23 38 01 25 00 0E 05 34 37 31 30 30 30 32 32 30 37 31 34 33 30 32 30 30 35 01 00 00 03 00 4D 6F 64 75 6C 65 20 31 20 6F 66 66 6C 69 6E 65 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 23";
        ClientProtocolCodec protocolCodec = new ClientProtocolCodec();
        byte[] decodeHex = HexExtUtil.decodeHex(StringUtils.deleteWhitespace(body));
        byte[] bytes = protocolCodec.transferEncode(decodeHex);
        DataPacketReader reader = new DataPacketReader(bytes);
    }

}
