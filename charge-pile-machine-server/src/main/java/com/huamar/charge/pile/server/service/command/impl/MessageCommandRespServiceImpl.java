package com.huamar.charge.pile.server.service.command.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.command.MessageCommandRespService;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * 消息控制命令响应结果
 * 先将命令发送信息放入缓存，等待设备通用应答返回，发送命令状态到请求端
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCommandRespServiceImpl implements MessageCommandRespService {


    private final RedissonClient redissonClient;

    /**
     * 消息生产者
     */
    private final PileMessageProduce pileMessageProduce;

    /**
     * 更新缓存信息
     * @param commonRespDTO commonRespDTO
     */
    @Override
    public void put(MessageCommonRespDTO commonRespDTO) {
        Assert.hasLength(commonRespDTO.getIdCode(), "id code is null");
        Assert.hasLength(commonRespDTO.getCommandId(), "request id is null");

        CacheKeyEnum keyEnum = CacheKeyEnum.MACHINE_COMMAND_ANSWER;
        String key = commonRespDTO.getIdCode() + "_" +commonRespDTO.getMsgNumber();
        key = keyEnum.joinKey(key);
        RBucket<MessageCommonRespDTO> bucket = redissonClient.getBucket(key);
        bucket.set(commonRespDTO, keyEnum.getDuration().toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取命令流水号
     *
     * @param idCode idCode
     * @param msgNum msgNum
     * @return MessageCommonRespDTO
     */
    @Override
    public MessageCommonRespDTO get(String idCode, Short msgNum){
        CacheKeyEnum keyEnum = CacheKeyEnum.MACHINE_COMMAND_ANSWER;
        String key = idCode + "_" +msgNum;
        key = keyEnum.joinKey(key);
        RBucket<MessageCommonRespDTO> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 发送远程命令响应
     * @param commonRespDTO commonRespDTO
     */
    @Override
    public void sendCommonResp(MessageCommonRespDTO commonRespDTO){
        log.info("消息控制命令响应 data:{}", JSONParser.jsonString(commonRespDTO));
        Snowflake snowflake = IdUtil.getSnowflake();
        String idStr = snowflake.nextIdStr();
        MessageData<Object> messageData = new MessageData<>(MessageCodeEnum.PILE_MESSAGE_COMMON_RESP, commonRespDTO);
        messageData.setMessageId(idStr);
        messageData.setRequestId(idStr);
        messageData.setBusinessId(commonRespDTO.getIdCode());
        pileMessageProduce.send(messageData);
    }
}
