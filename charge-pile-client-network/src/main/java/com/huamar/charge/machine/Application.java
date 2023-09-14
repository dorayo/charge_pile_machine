package com.huamar.charge.machine;


import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.PacketBuilder;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.machine.client.handle.ClientProtocolCodec;
import com.huamar.charge.machine.client.protocol.TioPacket;
import com.huamar.charge.machine.client.starter.MachineClient;
import de.vandermeer.asciitable.AsciiTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.tio.client.ClientChannelContext;
import org.tio.core.Tio;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 服务端程序入口
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@EnableConfigurationProperties
@SpringBootApplication
@Slf4j
public class Application {

    private static ApplicationContext applicationContext;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        print();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void listen(ApplicationReadyEvent event) throws Exception {
        applicationContext = event.getApplicationContext();
        MachineClient machineClient = event.getApplicationContext().getBean(MachineClient.class);
        machineClient.connect();


        FileAlterationMonitor monitor = new FileAlterationMonitor(1000);
        String userDirectoryPath = FileUtils.getUserDirectoryPath();
        FileAlterationObserver observer = new FileAlterationObserver(Paths.get(new URI(userDirectoryPath)).resolve("command.txt").toString());
        observer.addListener(new FileListener(machineClient));
        monitor.addObserver(observer);

    }

//    @EventListener(ApplicationReadyEvent.class)
//    @Order(0)
//    public void nettyStart(ApplicationReadyEvent event) throws Exception {
//        applicationContext = event.getApplicationContext();
//        MachineNetClient client = event.getApplicationContext().getBean(MachineNetClient.class);
//        client.start();
//    }


    /**
     * 启动打印信息
     */
    @SneakyThrows
    private static void print() {
        Environment environment = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port");
        String path = environment.getProperty("server.servlet.context-path");
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Application is running! Access URLs", "");
        at.addRule();
        at.addRow("Local:", "http://localhost:" + port + path + "doc.html");
        at.addRule();
        at.addRow("External:", "http://" + ip + ":" + port + path + "doc.html");
        at.addRule();
        at.addRow("Swagger doc :", "http://" + ip + ":" + port + path + "doc.html");
        at.addRule();
        log.info(System.getProperty("line.separator") + at.render());
    }

    static class FileListener extends FileAlterationListenerAdaptor {

        private final MachineClient machineClient;

        public FileListener(MachineClient machineClient) {
            this.machineClient = machineClient;
        }

        @Override
        public void onFileCreate(File file) {
            String compressedPath = file.getAbsolutePath();
            System.out.println("新建：" + compressedPath);
            if (file.canRead()) {
                System.out.println("文件变更，进行处理");
            }
        }

        @SneakyThrows
        @Override
        public void onFileChange(File file) {
            List<String> stringList = Files.readAllLines(file.toPath());
            stringList.forEach(this::send);

        }

        @SuppressWarnings("DuplicatedCode")
        private void send(String body){
            ClientChannelContext channelContext = machineClient.getClientChannelContext();
            ClientProtocolCodec protocolCodec = new ClientProtocolCodec();

            // 转码翻译数据包
            byte[] decodeHex = HexExtUtil.decodeHex(StringUtils.deleteWhitespace(body));
            byte[] bytes = protocolCodec.transferEncode(decodeHex);
            DataPacketReader reader = new DataPacketReader(bytes);
            DataPacket decode = (DataPacket) protocolCodec.decode(reader.getBuffer());

            // 写入数据包
            DataPacketWriter writer = new DataPacketWriter();
            writer.write(decode.getMsgBody());
            DataPacket packet = PacketBuilder.builder()
                    .messageNumber(machineClient.getMessageNumber())
                    .messageId(HexExtUtil.encodeHexStr(decode.getMsgId()))
                    .idCode(new String(decode.getIdCode()))
                    .body(writer)
                    .build();
            Tio.send(channelContext, new TioPacket(packet));
        }
    }

}