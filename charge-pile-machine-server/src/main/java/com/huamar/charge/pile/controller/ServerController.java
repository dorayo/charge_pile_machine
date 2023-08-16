package com.huamar.charge.pile.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 命令测试
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@RestController
@RequestMapping("/server")
public class ServerController {

    @SneakyThrows
    @PostMapping("/send")
    public Object send(@RequestParam("id") String id, @RequestParam("body") String body) {
        return "ok";
    }


    @SneakyThrows
    @PostMapping("/sendByToken")
    public Object sendByToken(@RequestParam("token") String id, @RequestParam("body") String body) {
        return "ok";
    }
}
