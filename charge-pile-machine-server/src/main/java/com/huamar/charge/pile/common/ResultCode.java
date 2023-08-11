package com.huamar.charge.pile.common;


import lombok.Getter;

/**
 * 消息结果
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum ResultCode {
	/**
	 * 0x0000 表示命令执行成功
	 */
	SUCCESS(0,"命令执行成功"),

	/**
	 * 0x0001 表示命令执行失败
	 */
	FAIL(1,"命令执行失败"),

	/**
	 * 0xFFFF 表示无效
	 */
	INVALID(65535,"无效命令");

	ResultCode(int code, String desc) {
		this.code = (short) code;
		this.desc = desc;
	}
	
	private final short code;
	private final String desc;


	public static ResultCode getResult(short code) {
		for (ResultCode type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return ResultCode.INVALID;
	}
}
