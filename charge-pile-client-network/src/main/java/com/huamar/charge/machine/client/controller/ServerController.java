package com.huamar.charge.machine.client.controller;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.PacketBuilder;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.machine.client.protocol.PacketCodec;
import com.huamar.charge.machine.client.protocol.TioPacket;
import com.huamar.charge.machine.client.MachineClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tio.client.ClientChannelContext;
import org.tio.core.Tio;

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

    private final TaskExecutor taskExecutor;


    @ApiOperation(value="数据汇报-充电桩实时状态")
    @SneakyThrows
    @PostMapping("/sendPacket")
    public Object sendPileOnlineStatus(
            @RequestParam String id,
            @RequestParam String body
    ) {
        ClientChannelContext channelContext = machineClient.getClientChannelContext();
        PacketCodec protocolCodec = new PacketCodec();

        // 转码翻译数据包
        byte[] decodeHex = HexExtUtil.decodeHex(StringUtils.deleteWhitespace(body));
        byte[] bytes = protocolCodec.transferEncode(decodeHex);
        DataPacketReader reader = new DataPacketReader(bytes);
        DataPacket dataPacket = (DataPacket) protocolCodec.decode(reader.getBuffer());

        // 写入数据包
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(dataPacket.getMsgBody());

        DataPacket packet = PacketBuilder.builder()
                .messageNumber(machineClient.getMessageNumber())
                .messageId("34")
                .idCode(id)
                .body(writer)
                .build();

        Tio.send(channelContext, new TioPacket(packet));
        return "ok";
    }

}
