package com.huamar.charge.pile.server;

import lombok.SneakyThrows;

/**
 * net 服务端启动类
 */
public interface NetServer {

    @SneakyThrows
    void start();

    void close();
}
