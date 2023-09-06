package com.huamar.charge.pile.entity.dto.fault;

import com.alibaba.fastjson.annotation.JSONField;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.util.json.BCDHexValueSerializer;
import com.huamar.charge.pile.entity.dto.BaseReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电桩心跳
 * date 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class McHeartbeatReqDTO extends BaseReqDTO {

	/**
	 * 服务器时间
	 */
	@JSONField(serializeUsing = BCDHexValueSerializer.class)
	public BCD time;

	/**
	 * 平台协议号
	 */
	public byte protocolNumber;

	/**
	 * 预留字段
	 */
	public byte retain1;

	/**
	 * 预留字段
	 */
	public byte retain2;

	/**
	 * 预留字段
	 */
	public byte retain3;

}
