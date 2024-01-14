package com.huamar.charge.pile.entity.dto;


import com.huamar.charge.common.common.codec.BCD;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

/**
 * 终端鉴权请求 通过ip地址查找指定充电桩
 * date 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MachineAuthenticationReqDTO extends BaseReqDTO {

	/**
	 * id code
	 */
	public String idCode;

	/**
	 * 登录流水号 按日自增长，由 1 到 65535(如超过65535 则再次从 1 开始)
	 */
	public int loginNumber;

	/**
	 * 终端时钟
	 */
	public BCD terminalTime;

	/**
	 * MAC地址
	 */
	public BCD macAddress;

	/**
	 * 板子个数
	 */
	public byte boardNum;

	/**
	 * 程序版本长度
	 */
	public byte programVersionLen;

	/**
	 * 程序版本号
	 */
	public HashMap<Integer, String> boardVersionMaps;

	/**
	 * 程序版本号
	 */
	public String programVersionNum;

	/**
	 * 设备经度
	 */
	public double longitude;

	/**
	 * 设备纬度
	 */
	public double latitude;

}
