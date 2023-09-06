package com.huamar.charge.pile.server.service.answer;

import com.huamar.charge.common.common.BaseResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.common.protocol.DataPacketWriter;
import org.tio.core.ChannelContext;

/**
 * 设备应答接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McAnswerExecute<T extends BaseResp> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    McAnswerEnum getCode();


    /**
     * 执行方法
     *
     * @param resp           resp
     * @param channelContext channelContext
     */
    void execute(T resp, ChannelContext channelContext);

    /**
     * 封装协议数据
     * @param command command
     * @return DataPacketWriter
     */
    default DataPacketWriter writer(T command){
        return null;
    }
}
