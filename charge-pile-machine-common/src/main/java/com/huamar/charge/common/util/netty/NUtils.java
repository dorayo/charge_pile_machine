package com.huamar.charge.common.util.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class NUtils {
    static public ChannelFuture writeBfWithCb(ChannelHandlerContext ctx, ByteBuf bf) {
        return ctx.channel().writeAndFlush(bf);
    }
}
