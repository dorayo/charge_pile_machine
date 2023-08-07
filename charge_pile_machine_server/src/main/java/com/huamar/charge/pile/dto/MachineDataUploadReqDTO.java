package com.huamar.charge.pile.dto;

import com.huamar.charge.pile.common.codec.BCD;
import lombok.Data;

import java.util.List;

/**
 * 数据汇报
 * @author wude
 *
 */
@Data
public class MachineDataUploadReqDTO {

	/**
	 * 数据采集时间  BCD[6]
	 */
	public BCD time;

	/**
	 * 数据单元列表长度
	 */
	public byte listLen;

	/**
	 * 数据单元列表
	 */
	public List<MachineDataUpItem> dataUnitList;

}
