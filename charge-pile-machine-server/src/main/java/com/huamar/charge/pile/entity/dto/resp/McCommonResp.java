package com.huamar.charge.pile.entity.dto.resp;

import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.BaseResp;
import com.huamar.charge.common.common.ResultCode;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.protocol.DataPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用应答响应
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McCommonResp extends BaseResp {

	/**
	 * 消息ID
	 */
	private byte msgId;

	/**
	 * 原消息序号
	 */
	private int msgNumber;

	/**
	 * 消息结果
	 * 0x0000 表示命令执行成功，0x0001 表示命令执行失败， 0xFFFF 表示无效
	 */
	private short msgResult;

	// 服务器时间
	private BCD time;

	public McCommonResp(DataPacket packet, ResultCode msgResult) {
		this.msgId = packet.getMsgId();
		this.msgNumber = packet.getMsgNumber();
		this.msgResult = msgResult.getCode();
		this.time = BCDUtils.bcdTime();
		this.idCode = new String(packet.getIdCode());
	}

	/**
	 * 成功响应
	 *
	 * @param packet packet
	 * @return CommonResp
	 */
	public static McCommonResp ok(DataPacket packet){
		return new McCommonResp(packet, ResultCode.SUCCESS);
	}

	/**
	 * 成功响应
	 *
	 * @param packet packet
	 * @return CommonResp
	 */
	public static McCommonResp fail(DataPacket packet){
		return new McCommonResp(packet, ResultCode.FAIL);
	}

}
