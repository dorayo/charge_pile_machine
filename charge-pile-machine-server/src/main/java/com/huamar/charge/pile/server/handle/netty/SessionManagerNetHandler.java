package com.huamar.charge.pile.server.handle.netty;

import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * 服务端监听器 ChannelInboundHandlerAdapter 区别，SimpleChannelInboundHandler.channelRead0 可是实现释放数据
 *
 * @author TiAmo
 */
@Slf4j
public class SessionManagerNetHandler extends SimpleChannelInboundHandler<BasePacket> {

    /**
     * 服务端上线的时候调用
     *
     * @param ctx ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("{} 连上了服务器", ctx.channel().remoteAddress());
    }

    /**
     * 服务端掉线的时候调用
     *
     * @param ctx ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("{} 断开了服务器", ctx.channel().remoteAddress());
        ctx.fireChannelInactive();
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param packet                dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BasePacket packet) {
        if(packet instanceof DataPacket){
            DataPacket dataPacket = (DataPacket) packet;
            String bsId = new String((dataPacket).getIdCode());
            MDC.put(ConstEnum.ID_CODE.getCode(), new String((dataPacket).getIdCode()));
            SessionChannel session = SessionManager.get(bsId);
            if(Objects.isNull(session)){
                SimpleSessionChannel sessionChannelNew = new SimpleSessionChannel(channelHandlerContext);
                sessionChannelNew.setId(bsId);
                SessionManager.put(bsId, sessionChannelNew);
            }

            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            channelHandlerContext.channel().attr(machineId).set(bsId);
        }

        if (packet instanceof FailMathPacket) {
            FailMathPacket dataPacket = (FailMathPacket) packet;
            log.info("FailMathPacket data:{}", HexExtUtil.encodeHexStrFormat(dataPacket.getBody(), StringPool.SPACE));
            return;
        }
        channelHandlerContext.fireChannelRead(packet);
    }

    /**
     * 异常发生时候调用
     *
     * @param ctx   ctx
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("{} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        ctx.close();
    }
}
