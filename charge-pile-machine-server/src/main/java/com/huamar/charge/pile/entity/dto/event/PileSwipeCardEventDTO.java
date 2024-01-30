package com.huamar.charge.pile.entity.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 刷卡查询事件
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PileSwipeCardEventDTO extends PileEventBaseDTO {


	/**
	 * idCode
	 */
	protected String idCode;

	/**
	 * 卡号长度 BYTE 长度 1-255
	 */
	public byte cardNumberLen;
	/**
	 * N 卡号 STRING [n]
	 */
	public String cardNumber;
	/**
	 * 充电枪编号 BYTE 充电桩/补电车中充电枪对应编号 范围 1-254，0xff 表示无效
	 */
	public byte gunSort;
	/**
	 * 密码长度
	 */
	public byte passwordLen;
	/**
	 * 登录密码
	 */
	public String password;
}
