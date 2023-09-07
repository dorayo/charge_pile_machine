package com.huamar.charge.pile.entity.dto.command;

import com.huamar.charge.pile.entity.dto.BaseReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息控制命令响应结果
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageCommonRespDTO extends BaseReqDTO {

	/**
	 * 命令请求ID
	 */
	private String commandId;

	/**
	 * 原消息序号
	 */
	private Integer msgNumber;

	/**
	 * 消息结果
	 *
	 */
	private String msgResult;

	/**
	 * 命令时间
	 */
	private String time;

	/**
	 * 命令类型编码
	 */
	private String commandTypeCode;

}
