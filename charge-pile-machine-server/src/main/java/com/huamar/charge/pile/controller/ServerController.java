package com.huamar.charge.pile.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.parameter.PileParamItemDTO;
import com.huamar.charge.pile.entity.dto.parameter.PileParameterDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令测试
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/server")
public class ServerController {



//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private RabbitAdmin admin;


//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @SneakyThrows
//    @PostMapping("/hello")
//    public Object send() {
//        return "word";
//    }
//
//    @SuppressWarnings("DuplicatedCode")
//    @SneakyThrows
//    @PostMapping("/admin/electronicLock")
//    public Object electronicLock(String pileCode, String command) {
//
//        PileParameterDTO pileParameterDTO = new PileParameterDTO();
//        pileParameterDTO.setParamNumber((byte) 1);
//        pileParameterDTO.setIdCode(pileCode);
//        List<PileParamItemDTO> list = new ArrayList<>();
//
//        PileParamItemDTO paramItemDTO = new PileParamItemDTO();
//        paramItemDTO.setId((short) 8);
//        paramItemDTO.setParamData(command);
//        list.add(paramItemDTO);
//        pileParameterDTO.setList(list);
//
//
//        MessageData<PileParameterDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_PARAMETER_UPDATE, pileParameterDTO);
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setMessageId(IdUtil.randomUUID());
//        rabbitTemplate.send("pile.machine.control.queue", new Message(JSON.toJSONString(messageData).getBytes(), messageProperties));
//        return "word";
//    }
//
//
//    @SuppressWarnings("DuplicatedCode")
//    @SneakyThrows
//    @PostMapping("/admin/reboot")
//    public Object reboot(String pileCode) {
//
//        PileParameterDTO pileParameterDTO = new PileParameterDTO();
//        pileParameterDTO.setParamNumber((byte) 1);
//        pileParameterDTO.setIdCode(pileCode);
//        List<PileParamItemDTO> list = new ArrayList<>();
//
//        PileParamItemDTO paramItemDTO = new PileParamItemDTO();
//        paramItemDTO.setId((short) 10);
//        paramItemDTO.setParamData("reboot");
//        list.add(paramItemDTO);
//        pileParameterDTO.setList(list);
//
//
//        MessageData<PileParameterDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_PARAMETER_UPDATE, pileParameterDTO);
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setMessageId(IdUtil.randomUUID());
//        rabbitTemplate.send("pile.machine.control.queue", new Message(JSON.toJSONString(messageData).getBytes(), messageProperties));
//        return "word";
//    }
//
//    @SuppressWarnings("DuplicatedCode")
//    @SneakyThrows
//    @PostMapping("/admin/lock")
//    public Object lock(String pileCode, String command) {
//
//        PileParameterDTO pileParameterDTO = new PileParameterDTO();
//        pileParameterDTO.setParamNumber((byte) 1);
//        pileParameterDTO.setIdCode(pileCode);
//        List<PileParamItemDTO> list = new ArrayList<>();
//
//        PileParamItemDTO paramItemDTO = new PileParamItemDTO();
//        paramItemDTO.setId((short) 9);
//        paramItemDTO.setParamData(command);
//        list.add(paramItemDTO);
//        pileParameterDTO.setList(list);
//
//
//        MessageData<PileParameterDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_PARAMETER_UPDATE, pileParameterDTO);
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setMessageId(IdUtil.randomUUID());
//        rabbitTemplate.send("pile.machine.control.queue", new Message(JSON.toJSONString(messageData).getBytes(), messageProperties));
//        return "word";
//    }

}
