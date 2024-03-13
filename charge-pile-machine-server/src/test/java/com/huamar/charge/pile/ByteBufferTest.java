package com.huamar.charge.pile;

import com.huamar.charge.common.util.HexExtUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ByteBufferTest {

    public static void main(String[] args) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer(256);
        byteBuf.writeBytes(HexExtUtil.decodeHex("74400000"));
        int readIntLE = byteBuf.readIntLE();
        System.out.println(readIntLE);

        System.out.println(0x02 == 2);

    }
}
