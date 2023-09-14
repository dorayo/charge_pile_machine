package com.huamar.charge.machine.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * MachineHandler
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class MachineHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = Unpooled.copiedBuffer("hello server".getBytes(StandardCharsets.UTF_8));
        ctx.write(byteBuf);
        ctx.flush();
    }
}
