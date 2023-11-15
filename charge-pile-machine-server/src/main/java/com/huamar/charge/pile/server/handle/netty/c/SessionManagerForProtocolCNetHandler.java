package com.huamar.charge.pile.server.handle.netty.c;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * 服务端监听器 ChannelInboundHandlerAdapter 区别，SimpleChannelInboundHandler.channelRead0 可是实现释放数据
 *
 * @author TiAmo
 */
@Slf4j
public class SessionManagerForProtocolCNetHandler extends SimpleChannelInboundHandler<ProtocolCPacket> {
    McTypeEnum type = McTypeEnum.C;

    public SessionManagerForProtocolCNetHandler(McTypeEnum type) {
        this.type = type;
    }

    public SessionManagerForProtocolCNetHandler() {
    }

    /**
     * 服务端上线的时候调用
     *
     * @param ctx ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        MDC.clear();
        log.info("{} active", ctx.channel().remoteAddress());
    }

    /**
     * 服务端掉线的时候调用
     *
     * @param ctx ctx
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        MDC.clear();
//        log.info("{} 断开了服务器", ctx.channel().remoteAddress());
//        try {
////            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
////            String bsId = ctx.channel().attr(machineId).get();
////            if (StringUtils.isNotBlank(bsId)) {
////                MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
////                SessionManager.remove(bsId);
////            }
//        } catch (Exception ignored) {
//            log.info("{} 断开了服务器 error", ctx.channel().remoteAddress());
//        } finally {
//            ctx.close();
//        }
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param packet                dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProtocolCPacket packet) {
        Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
        log.info("type=0x{}", Integer.toHexString(packet.getBodyType()));
        if (packet.getBodyType() == 0x01) {
            channelHandlerContext.channel().attr(NAttrKeys.ID_BODY).set(packet.getIdBody());
            ByteBuf body = ByteBufAllocator.DEFAULT.heapBuffer();
            body.writeBytes(packet.getBody());
            String id = BinaryViews.bcdViewsLe(NUtils.nBFToBf(body.readBytes(7)));
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            channelHandlerContext.channel().attr(machineId).set(id);
            SimpleSessionChannel sessionChannelNew = new SimpleSessionChannel(channelHandlerContext);
            sessionChannelNew.setId("4710" + id);
            sessionChannelNew.setType(type);
            SessionManager.put(id, sessionChannelNew);
        }
        channelHandlerContext.fireChannelRead(packet);
        MDC.clear();
    }

    /**
     * 异常发生时候调用
     *
     * @param ctx   ctx
     * @param cause cause
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MDC.clear();
        log.error("{} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            if (StringUtils.isNotBlank(bsId)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
                SessionManager.remove(bsId);
            }
        } catch (Exception ignored) {
            log.error("{} exceptionCaught error,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        } finally {
            ctx.close();
        }
    }

    /**
     * 心跳检测事件处理
     *
     * @param ctx ctx
     * @param evt evt
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 入站的消息就是 IdleStateEvent 具体的事件
        IdleStateEvent event = (IdleStateEvent) evt;
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        String bsId = ctx.channel().attr(machineId).get();
        if (StringUtils.isNotBlank(bsId)) {
            MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
        }
        switch (event.state()) {
            case READER_IDLE:
                log.info("读取数据空闲");
                break;
            case WRITER_IDLE:
                // 不处理
                break;
            case ALL_IDLE:
                log.warn("IdCode:{} ALL_IDLE 心跳链接超时，关闭连接", bsId);
                if (StringUtils.isNotBlank(bsId)) {
                    SessionManager.remove(bsId);
                }
                ctx.close();
                break;
        }
    }
}
