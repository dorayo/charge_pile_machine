package com.huamar.charge.pile.enums;

import lombok.Getter;

/**
 * 充电枪状态
 * 0.脱机(枪未接车)
 * 1.空闲(枪接了车未开充充电)
 * 2.握手
 * 3.配置
 * 4.充电中
 * 5.结束
 * 6.故障
 * @author PhilShen
 *
 */
@Getter
@SuppressWarnings("unused")
public enum PileStateEnum {
	/**
	 * 未知
	 */
	UN_KNOW(-1, "未知"),
	/**
	 * 0.脱机(枪未接车)
	 */
	OFFLINE(0x00, "空闲"),
	/**
	 * 1.空闲(枪接了车未开充充电)
	 */
	FREE(0x01, "充电枪已连接"),
	/**
	 * 2.握手
	 */
	SHAKE_HAND(0x02, "充电中"),
	/**
	 * 3.配置
	 */
	CONFIG(0x03, "充电中"),
	/**
	 * 4.充电中
	 */
	CHARGING(0x04, "充电中"),
	/**
	 * 5.结束
	 */
	CHARGE_END(0x05, "充电中"),
	/**
	 * 6.故障
	 */
	FAULT(0x06, "急停故障"),
	/**
	 * 7.设备锁定
	 */
	GUN_LOCK(0x07, "设备锁定"),
	/**
	 * 8.网络异常
	 */
	NET_EXCEPTION(0x08, "网络异常"),
	/**
	 * 9.待检修
	 */
	WAIT_REPAIR(0x09, "待检修"),
	/**
	 * 100.连接中断
	 */
	NOT_CONNECT(100, "连接中断"),
	/**
	 * 101.等待开始充电
	 */
	WAITE_CHARGE(101, "等待开始充电");

	PileStateEnum(int code, String desc) {
		this.code = (byte) code;
		this.desc = desc;
	}

	private final byte code;
	
	private final String desc;

	public static PileStateEnum getType(int code) {
		for (PileStateEnum type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return null;
	}
}
