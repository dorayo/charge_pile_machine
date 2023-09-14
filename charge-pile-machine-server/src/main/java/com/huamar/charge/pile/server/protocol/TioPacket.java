package com.huamar.charge.pile.server.protocol;

import com.huamar.charge.common.protocol.BasePacket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.tio.core.intf.Packet;

/**
 * 解码成功协议包(tio 框架包)
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TioPacket extends Packet {


    private BasePacket basePacket;
}
