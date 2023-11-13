package com.huamar.charge.common.util.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class NUtils {
    static public ChannelFuture writeBfWithCb(ChannelHandlerContext ctx, ByteBuf bf) {
        return ctx.channel().writeAndFlush(bf);
    }

    static public byte[] nBFToBf(ByteBuf bf) {
        byte[] result = new byte[bf.readableBytes()];
        int i = 0;
        while (bf.isReadable()) {
            result[i++] = bf.readByte();
        }
        NUtils.releaseNBF(bf);
        return result;
    }

    static public void releaseNBF(ByteBuf bf) {
        if (bf.refCnt() > 0) {
            bf.release(bf.refCnt());
        }

    }
}
