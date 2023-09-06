package com.huamar.charge.pile.entity.dto.resp;

import com.huamar.charge.common.common.BaseResp;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.protocol.FixString;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权鉴响应
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McAuthResp extends BaseResp {

	/**
	 * 登录状态
	 * 1、 表示登录成功
	 * 2、此设备未注册，
	 * 3、系统无此设备信息，无法登录
	 * 4、其它原因
	 */
	public byte status;

	/**
	 * 服务器端时钟(6)
	 */
	public BCD time;

	/**
	 * 加解密类型
	 */
	public byte encryptionType;

	/**
	 * 加解密密钥长度
	 */
	public short secretKeyLength;

	/**
	 * 加解密密钥内容
	 */
	public FixString secretKey;

}
