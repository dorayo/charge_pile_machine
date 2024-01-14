package com.huamar.charge.pile.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 数据上报导单元列
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineDataUpItem extends BaseReqDTO {

	private byte unitId;

	private int dataLen;

	private byte[] data;

}
