package com.huamar.charge.pile.enums;

import lombok.Getter;

/**
 * 应答类型
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum MachineAuthStatus {

	 /**
	  * 1、 表示登录成功
	  */
	SUCCESS(1),
	/**
	 * 2、此设备未注册
	 */
	TERMINAL_NOT_REGISTER(2),
	/**
	 * 3、系统无此设备信息，无法登录
	 */
	TERMINAL_INFO_NOT_FOUND(3),
	/**
	 * 4、其它原因
	 */
	OTHER(4);

	private final byte code;

	MachineAuthStatus(int code) {
		this.code = (byte) code;
	}
}
