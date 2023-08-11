package com.huamar.charge.pile.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据上报导单元列
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineDataUpItem {

	private byte unitId;

	private short dataLen;

	private byte[] data;

}
