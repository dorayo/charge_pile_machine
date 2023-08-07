package com.huamar.charge.pile.server.service.mc;

import com.huamar.charge.pile.dto.PileDTO;

/**
 * 设备端接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface MachineService {

    PileDTO getPile(String idCode);
}
