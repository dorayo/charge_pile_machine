package com.huamar.charge.pile.entity.dto.charge;

import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.pile.entity.dto.BaseReqDTO;
import com.huamar.charge.pile.enums.PileStateEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电机实时状态信息
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChargeInfoDTO extends BaseReqDTO {

	/**
	 * 充电订单号
	 */
	private String orderNumber;

	/**
	 * 充电枪编号 充电桩/补电车中充电枪对应编号 范围 1-254，0xff 表示无效
	 */
	private byte gunSort;


	/**
	 *
	 */
	private byte gunState = PileStateEnum.UN_KNOW.getCode();

	/**
	 * 充电开始时间
	 */
	private BCD startTime;

	/**
	 * 累计充电时间 单位 1Min/bit，有效值 1-65535，偏移0
	 */
	private int cumulativeTime = 0;

	/**
	 * 当前已充电的金额,电费，四位小数元/bit，0xFFFFFFFF表示无效
	 */
	private int curMoney = 0;

	/**
	 * 服务费，四位小数/bit，0xFFFFFFFF表示无效
	 */
	private int serviceMoney = 0;

	/**
	 * 实际充电量 单位：四位小数Kw.h/bit，偏移 0
	 */
	private int curChargeQuantity = 0;


}
