package com.huamar.charge.pile.entity.dto.mq;

import com.huamar.charge.common.common.BaseDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MessageBody 消息队列
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageData<T> extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 幂等Id
     */
    private String messageId;

    /**
     * businessCode
     */
    private String businessCode;

    /**
     * businessId
     */
    private String businessId;

    /**
     * 数据时间
     */
    private LocalDateTime dateTime;

    /**
     * 32位id, 上下文跟踪ID
     */
    private String requestId;

    /**
     * 设备编码
     */
    private String idCode;

    /**
     * 元数据
     */
    private T data;

    public MessageData(MessageCodeEnum codeEnum, T data) {
        this.businessCode = codeEnum.getCode();
        this.data = data;
        this.dateTime = LocalDateTime.now();
    }

    public MessageData(T data) {
        this.data = data;
    }
}
