package com.huamar.charge.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.util.json.BCDHexValueSerializer;
import com.huamar.charge.common.util.json.ByteHexValueSerializer;

import java.util.List;
import java.util.Map;

/**
 * json工具类
 * :date 2023/07
 *
 * @author TiAmo(TiAmolikecode @ gmail.com)
 */
@SuppressWarnings("unused")
public class JSONParser {

    private static final SerializerFeature[] FEATURES = {
            // 输出空置字段
            SerializerFeature.WriteMapNullValue,
            // list字段如果为null，输出为[]，而不是null
            SerializerFeature.WriteNullListAsEmpty,
            // 数值字段如果为null，输出为0，而不是null
            SerializerFeature.WriteNullNumberAsZero,
            // Boolean字段如果为null，输出为false，而不是null
            SerializerFeature.WriteNullBooleanAsFalse,
            // 字符类型字段如果为null，输出为""，而不是null
            SerializerFeature.WriteNullStringAsEmpty
    };

    private static final SerializeConfig config = new SerializeConfig();

    static {
        config.put(BCD.class, new BCDHexValueSerializer());
        config.put(byte[].class, new ByteHexValueSerializer());
    }


    /**
     * 格式化json时间
     *
     * @param object      object
     * @return String
     */
    public static String jsonString(Object object) {
        return JSON.toJSONString(object, config);
    }


    /**
     * JSON字符串转换Object
     *
     * @param string string
     * @return Object
     */
    public static Object parseObject(String string) {
        return JSON.parse(string);
    }

    /**
     * JSON字符串转换对象
     *
     * @param string string
     * @param clazz  clazz
     * @param <T>    T
     * @return T
     */
    public static <T> T parseObject(String string, Class<T> clazz) {
        return JSON.parseObject(string, clazz);
    }

    /**
     * JSON转换Object[]
     *
     * @param string string
     * @param clazz  clazz
     * @param <T>    T
     * @return Object[]
     */
    public static <T> Object[] toArray(String string, Class<T> clazz) {
        return JSON.parseArray(string, clazz).toArray();
    }

    /**
     * JSON字符串转换 List
     *
     * @param string string
     * @param clazz  clazz
     * @param <T>    T
     * @return T
     */
    public static <T> List<T> toList(String string, Class<T> clazz) {
        return JSON.parseArray(string, clazz);
    }

    /**
     * json字符串转化为map
     *
     * @param string string
     * @return Map
     */
    public static <K, V> Map<K, V> jsonToMap(String string) {
        @SuppressWarnings("unchecked") Map<K, V> m = (Map<K, V>) JSON.parseObject(string);
        return m;
    }

    /**
     * json字符串转化为JSONArray
     *
     * @param string string
     * @return JSONArray
     */
    public static JSONArray toJsonArray(String string) {
        return JSON.parseArray(string);
    }

}
