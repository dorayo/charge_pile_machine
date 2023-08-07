package com.huamar.charge.pile.server.service.fault;

import com.huamar.charge.pile.enums.McFaultPutEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketWriter;

/**
 * 设备端数据汇报接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McFaultPutExecute<T> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    McFaultPutEnum getCode();

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    void execute(T reqDTO);

    /**
     * 读取参数
     * @param packet packet
     * @return McBaseParameterDTO
     */
    default T reader(DataPacket packet){
        return null;
    }

    /**
     * 封装协议数据
     * @param command command
     * @return DataPacketWriter
     */
    default DataPacketWriter writer(T command){
        return null;
    }

}
