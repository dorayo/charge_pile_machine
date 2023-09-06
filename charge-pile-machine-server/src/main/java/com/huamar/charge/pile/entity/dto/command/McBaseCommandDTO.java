package com.huamar.charge.pile.entity.dto.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.common.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 远程控制基础类
 * Day 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class McBaseCommandDTO extends BaseDTO {


    protected final Map<String, Object> headerProperties = new HashMap<>(16);

    /**
     * 命令消息流水号
     */
    private static final String MESSAGE_NUM = "MESSAGE_NUM";

    /**
     * 命令行发送结果
     */
    private static final String COMMAND_STATE = "COMMAND_STATE";

    /**
     * 设备唯一编码
     */
    public String idCode;

    /**
     * 控制命令类型
     */
    public short typeCode;

    /**
     * 控制命令数据长度
     */
    public byte fieldsByteLength = 0;

    /**
     * messageNum
     * @param messageNum messageNum
     */
    public void headMessageNum(Short messageNum){
        headerProperties.put(MESSAGE_NUM, messageNum);
    }

    /**
     * messageNum
     */
    public Short headMessageNum(){
        Object object = headerProperties.get(MESSAGE_NUM);
        return Convert.toShort(object);
    }

    /**
     * herderCommandState
     * @param state state
     */
    public void headCommandState(Boolean state){
        headerProperties.put(COMMAND_STATE, state);
    }

    /**
     * herderCommandState
     */
    public Boolean headCommandState(){
        Object object = headerProperties.get(COMMAND_STATE);
        Boolean bool = Convert.toBool(object);
        return Objects.isNull(bool) ? Boolean.FALSE : bool;
    }

}
