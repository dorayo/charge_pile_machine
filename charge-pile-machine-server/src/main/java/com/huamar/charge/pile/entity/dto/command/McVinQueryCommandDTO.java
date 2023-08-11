package com.huamar.charge.pile.entity.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制-VIN白名单查询
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McVinQueryCommandDTO extends McBaseCommandDTO {

	/**
	 * 枪号
	 */
	private byte gunSort;

	/**
	 * 查询结果 0x01 白名单 0x02 非白名单
	 */
	private byte queryResult;

	/**
	 * 密码长度 只有查询结果为0x03时生效，其他结果不传
	 */
	public byte pwdLen;

	/**
	 * 密码
	 */
	public String pwd;

	public McVinQueryCommandDTO() {
		// 设置命令行字节长度
		this.fieldsByteLength = (byte) (1 + 1);
	}
}
