package com.huamar.charge.pile.controller;

import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.pile.server.handle.PacketProtocolCodec;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 命令测试，命令转义
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/codec")
public class CodecController {

    @SneakyThrows
    @PostMapping("/transferEncode")
    public Object transferEncode(@RequestParam("body") String body) {
        byte[] bytes = HexExtUtil.decodeHex(body);
        PacketProtocolCodec protocolCodec = new PacketProtocolCodec();
        bytes = protocolCodec.transferEncode(bytes);
        return HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE);
    }

}
