package com.huamar.charge.common.util.json;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.huamar.charge.common.common.codec.BCD;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * byte 十六进制序列化方式
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
public class BCDHexValueSerializer implements ObjectSerializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        BCD bcd = (BCD) object;
        serializer.write(bcd.toHex());
    }
}
