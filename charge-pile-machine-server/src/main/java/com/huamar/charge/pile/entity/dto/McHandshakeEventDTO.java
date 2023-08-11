package com.huamar.charge.pile.entity.dto;

import com.huamar.charge.pile.common.codec.BCD;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电握手事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McHandshakeEventDTO extends McEventBaseDTO {
	/**
	 * 充电枪编号
	 */
	public byte gunSort;
	/**
	 * 辨识结果  BYTE  1/bit， 偏移 0
	 */
	public byte distinguishResult;
	/**
	 * BMS 通信协议版本号  BYTE[3]
	 * 本标准规定当前版本号为 V1.0，表示为 ：Byte3，Byte2—0001H，Byte1—00H
	 */
	public String bmsProtocolVersion;
	/**
	 * 电池类型  BYTE 1 ： 锂电池 2 ： 超级电容
	 */
	public byte batteryType;
	/**
	 * 整车动力蓄电池系统额定容量  DWORD  单位：KW.h/bit，偏移 0
	 */
	public int carRatedCapacity;
	/**
	 * 整车动力蓄电池系统额定总电压  WORD  单位：0.1V/bit，偏移 0
	 */
	public short carRatedVoltage;
	
	//public String batteryFirmName;//改动 --单体最高允许充电电压0
	/**
	 * 单体最高允许充电电压  WORD  范围：1~65535， 偏移 0
	 */
	public short dantidianya;
	/**
	 * 最高允许充电总电流  WORD  范围：1~65535， 偏移 0
	 */
	public short zuigaodianliu;
	/**
	 * 最高允许充电总电压  WORD  范围：1~65535， 偏移 0
	 */
	public short batterySerialNumber;//改动 --最高允许充电总电流
	/**
	 * 电池组生产日期：年/月/日  BCD[3]
	 */
	public BCD batteryProductionDate;
	/**
	 * 电池组充电次数  DWORD 最小值 1， 偏移 0
	 */
	public int batteryChargeCount;
	/**
	 * 最高允许温度  BYTE 1/bit， 偏移 0
	 */
	public byte batteryTag;
	/**
	 * 车辆识别码  BYTE[17]默认 ASCII 码，不足 17 位，后面补’\0’
	 */
	public String carIdentificationCode;

}
