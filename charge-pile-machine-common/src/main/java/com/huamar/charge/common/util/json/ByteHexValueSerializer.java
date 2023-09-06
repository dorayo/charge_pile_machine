package com.huamar.charge.common.util.json;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.huamar.charge.common.util.HexExtUtil;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * byte 十六进制序列化方式
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
public class ByteHexValueSerializer implements ObjectSerializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        byte[] b = (byte[]) object;
        String text = HexExtUtil.encodeHexStr(b);
        serializer.write(text);
    }
}
