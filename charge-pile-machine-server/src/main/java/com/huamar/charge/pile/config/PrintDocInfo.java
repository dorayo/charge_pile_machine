package com.huamar.charge.pile.config;

import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.pile.enums.LoggerEnum;
import de.vandermeer.asciitable.AsciiTable;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;

/**
 * 日志打印，系统启动信息
 */
public class PrintDocInfo {

    private static final Logger logger = LoggerFactory.getLogger(LoggerEnum.APPLICATION_MAIN_LOGGER.getCode());

    /**
     * 启动打印信息
     */
    @SneakyThrows
    public static void print(ApplicationContext applicationContext){
        Environment environment = applicationContext.getEnvironment();
        ServerApplicationProperties properties = applicationContext.getBean(ServerApplicationProperties.class);
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port");
        String path = environment.getProperty("server.servlet.context-path", StringPool.EMPTY);

        UriComponentsBuilder local = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path);

        UriComponentsBuilder external = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(ip)
                .port(port)
                .path(path);

        UriComponentsBuilder swagger = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(ip)
                .port(port)
                .path(path)
                .path("doc.html");

        AsciiTable at = new AsciiTable();
        at.addRule();

        at.addRow("Host :", "Port", "TimeOut", "SocketModel");
        at.addRule();

        at.addRow(properties.getHost(), properties.getPort(), properties.getTimeout().getSeconds() + "s", properties.getNetSocketModel());
        at.addRule();

        logger.info(System.getProperty("line.separator") + at.render());


        at = new AsciiTable();
        at.addRule();

        at.addRow("Application is running! Access URLs", "The format is wrong when using Chinese");
        at.addRule();

        at.addRow("Local:",local.build().toString());
        at.addRule();

        at.addRow("External:", external.build().toString());
        at.addRule();

        at.addRow("Swagger Api :", swagger.build().toString());
        at.addRule();

        logger.info(System.getProperty("line.separator") + at.render());
    }
}
