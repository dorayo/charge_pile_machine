package com.huamar.charge.pile.entity.dto.mq;

import com.huamar.charge.pile.common.BaseDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
     * businessCode
     */
    private String businessCode;

    private T data;

    public MessageData(MessageCodeEnum codeEnum, T data) {
        this.businessCode = codeEnum.getCode();
        this.data = data;
    }
}
