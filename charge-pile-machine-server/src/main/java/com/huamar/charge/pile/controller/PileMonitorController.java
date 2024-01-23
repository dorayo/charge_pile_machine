package com.huamar.charge.pile.controller;

import com.huamar.charge.pile.entity.dto.PileLoginLogDTO;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 命令测试，命令转义
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/pile/monitor")
public class PileMonitorController {

    private final RedissonClient redissonClient;


    /**
     * 查询所有连接数据
     *
     * @param param param {"ids":"电站id集合,分割"}
     * @return List<Map<String, PileLoginLogDTO>>
     */
    @SneakyThrows
    @PostMapping("/get")
    public List<Map<String, PileLoginLogDTO>> get(@RequestBody Map<String, String> param) {
        String ids = param.get("ids");
        String[] split = StringUtils.split(ids,",");
        List<Map<String, PileLoginLogDTO>> list = new ArrayList<>();
        Arrays.stream(split).forEach(element -> {
            String key = CacheKeyEnum.CHARGE_PILE_AUTH_LOG.joinKey(element);
            RMap<String, PileLoginLogDTO> map = redissonClient.getMap(key);
            Map<String, PileLoginLogDTO> allMap = map.readAllMap();
            list.add(allMap);
        });
        return list;
    }


    /**
     * 查询所有异常连接数据
     *
     * @param param param {"ids":"电站id集合,分割"}
     * @return List<Map<String, PileLoginLogDTO>>
     */
    @SneakyThrows
    @PostMapping("/invalidConnection/get")
    public Map<String, Integer> getInvalidConnection(@RequestBody Map<String, String> param) {
        log.info("getInvalidConnection param:{}", param);
        String key = CacheKeyEnum.CHARGE_PILE_INVALID_CONNECTION.joinKey("0");
        RMap<String, Integer> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

}
