package com.huamar.charge.pile.dto;

import com.huamar.charge.pile.common.codec.BCD;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用应答请求
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McCommonReq extends BaseReqDTO {

	/**
	 * 消息ID
	 */
	private byte msgId;

	/**
	 * 原消息序号
	 */
	private short msgNumber;

	/**
	 * 消息结果
	 * 0x0000 表示命令执行成功，0x0001 表示命令执行失败， 0xFFFF 表示无效
	 */
	private short msgResult;

	/**
	 * 命令时间
	 */
	private BCD time;


}
