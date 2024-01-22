package com.huamar.charge.pile.server.service.handler;

import cn.hutool.core.date.StopWatch;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.BaseResp;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FixString;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.convert.MachineAuthenticationConvert;
import com.huamar.charge.pile.entity.dto.MachineAuthenticationReqDTO;
import com.huamar.charge.pile.entity.dto.command.McQrCodeCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.resp.McAuthResp;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.service.answer.McAnswerExecute;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.machine.MachineService;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 终端鉴权
 * Date: 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineAuthenticationHandler implements MachinePacketHandler<DataPacket> {

    // 设备认证日志
    private final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());

    /**
     * 设备接口
     */
    private final MachineService machineService;

    /**
     * 应答回复工厂
     */
    private final McAnswerFactory answerFactory;

    /**
     * 指令下发工厂
     */
    private final McCommandFactory commandFactory;

    /**
     * 消息生产者
     */
    private final PileMessageProduce pileMessageProduce;


    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private TaskExecutor taskExecutor;


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.AUTH;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    @Override
    public void handler(DataPacket packet, SessionChannel sessionChannel) {

        InetSocketAddress remoteAddress = sessionChannel.remoteAddress();
        MachineAuthenticationReqDTO reqDTO = this.reader(packet);
        String idCode = reqDTO.getIdCode();
        log.info("SLX 终端鉴权 idCode:{}, remoteAddress:{}, loginNumber={}, time={}", idCode, remoteAddress, reqDTO.getLoginNumber(), reqDTO.getTerminalTime());
        authLog.info("SLX 终端鉴权 idCode:{}, remoteAddress:{}, loginNumber={}, time={}", idCode, remoteAddress, reqDTO.getLoginNumber(), reqDTO.getTerminalTime());

        McAuthResp authResp = new McAuthResp();
        authResp.setTime(BCDUtils.bcdTime());
        authResp.setEncryptionType((byte) 0);
        authResp.setSecretKeyLength((short) 0);
        authResp.setIdCode(idCode);
        authResp.setSecretKey(new FixString(new byte[0]));
        authResp.setMsgNumber(packet.getMsgNumber());

        // 应答实现
        McAnswerExecute<BaseResp> answerExecute = answerFactory.getExecute(McAnswerEnum.AUTH);
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        taskExecutor.execute(() -> {
            try {
                MDC.setContextMap(mdcMap);
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.PILE_AUTH, idCode));

                // 多次鉴权并发问题，先返回成功，认证失败关闭连接
                // 私有加密逻辑
                this.encryptionSecretKey(reqDTO, authResp, 0);
                authResp.setStatus(MachineAuthStatus.SUCCESS.getCode());
                answerExecute.execute(authResp, sessionChannel);

                long startTime = System.currentTimeMillis();
                long maxWaitTime = Duration.ofSeconds(3).toMillis();

                PileDTO pile = null;
                StopWatch stopWatch = new StopWatch("Pile Auth");
                stopWatch.start("wait pile");
                while (System.currentTimeMillis() - startTime < maxWaitTime) {
                    TimeUnit.MILLISECONDS.sleep(300);
                    pile = machineService.getCache(idCode);
                    boolean nonNull = Objects.nonNull(pile);
                    log.warn("终端鉴权 （SLX） auth wait pile time await:{} success:{}", System.currentTimeMillis() - startTime, nonNull);
                    authLog.warn("终端鉴权 （SLX） auth wait pile time await:{} success:{}", System.currentTimeMillis() - startTime, nonNull);
                    if (nonNull) {
                        break;
                    }
                }
                stopWatch.stop();
                log.info("终端鉴权 （SLX）auth pile isSuccess:{}, time:{}", Optional.ofNullable(pile).isPresent(), stopWatch.getTotalTimeSeconds());
                authLog.info("终端鉴权 （SLX）auth pile isSuccess:{}, time:{}", Optional.ofNullable(pile).isPresent(), stopWatch.getTotalTimeSeconds());

                // 多次鉴权并发问题
                Object auth = sessionChannel.getAttribute("auth");
                if (Objects.nonNull(auth)) {
                    log.info("终端鉴权 （SLX） pile auth session attribute:{}", auth);
                    authLog.info("终端鉴权 （SLX） pile auth session attribute:{}", auth);
                }

                // 认证失败
                if (Objects.isNull(pile)) {
                    authResp.setStatus(MachineAuthStatus.TERMINAL_NOT_REGISTER.getCode());
                    answerExecute.execute(authResp, sessionChannel);
                    SessionManager.close(sessionChannel);
                    return;
                }

                authResp.setIdCode(pile.getPileCode());
                authResp.setStatus(MachineAuthStatus.SUCCESS.getCode());
                // 更新对象
                PileDTO update = new PileDTO();
                update.setId(pile.getId());
                if (StringUtils.isBlank(pile.getMacAddress())) {
                    update.setMacAddress(reqDTO.getMacAddress().toString());
                    pile.setMacAddress(reqDTO.getMacAddress().toString());
                }

                // mac已登录成功过
                if (!StringUtils.equalsAnyIgnoreCase(pile.getMacAddress(), reqDTO.getMacAddress().toString())) {
                    authResp.setStatus(MachineAuthStatus.TERMINAL_INFO_NOT_FOUND.getCode());
                    answerExecute.execute(authResp, sessionChannel);
                    return;
                }

                if (StringUtils.equals(pile.getPileVersion(), reqDTO.getProgramVersionNum())) {
                    pile.setPileVersion(reqDTO.getProgramVersionNum());
                }


                authResp.setStatus(MachineAuthStatus.SUCCESS.getCode());
                // 标记此连接鉴权成功
                sessionChannel.setAttribute("auth", "ok");
                //answerExecute.execute(authResp, sessionChannel);

                update.setStationId(pile.getStationId());
                update.setPileCode(pile.getPileCode());

                //v2024/01/22 记录登录日志
                try {
                    if(sessionChannel instanceof SimpleSessionChannel){
                        SimpleSessionChannel simpleSessionChannel = (SimpleSessionChannel) sessionChannel;
                        simpleSessionChannel.channel().channel().attr(AttributeKey.valueOf(ConstEnum.STATION_ID.getCode())).set(pile.getStationId().toString());
                        simpleSessionChannel.channel().channel().attr(AttributeKey.valueOf(ConstEnum.ELE_CHARG_TYPE.getCode())).set(Integer.parseInt(pile.getElectricType()));
                    }
                    //v2024/01/22 记录登录日志
                    SessionManager.pileAuthLogSum(sessionChannel);
                }catch (Exception e){
                    log.error("pileAuthLogSum error", e);
                }

                // 二维码下发
                this.sendQrCode(authResp);

                //电价更新
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));

                // 设备更新
                pileMessageProduce.send(new MessageData<>(MessageCodeEnum.PILE_UPDATE, update));

            } catch (Exception e) {
                log.error("终端鉴权 （SLX）auth execute error:{}", e.getMessage(), e);
                authLog.error("终端鉴权 （SLX）auth execute error:{}", e.getMessage(), e);
            }
        });
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public MachineAuthenticationReqDTO reader(DataPacket packet) {
        return MachineAuthenticationConvert.INSTANCE.convert(packet);
    }


    /**
     * 电桩加密封装
     *
     * @param reqDTO reqDTO
     */
    private void encryptionSecretKey(MachineAuthenticationReqDTO reqDTO, McAuthResp authResp, int encryption) {
        if ((reqDTO.getBoardNum() & 0xff) != 160) {
            log.info("SLX 终端权健 encryptionSecretKey isEncrypt :{}", false);
            authLog.info("SLX 终端权健 encryptionSecretKey isEncrypt :{}", false);
            return;
        }

        if(encryption == 0){
            return;
        }

        authResp.setEncryptionType((byte) 0);
        String src = reqDTO.getIdCode() + authResp.getTime() + reqDTO.getMacAddress().toString();
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, "VB6dQCFh2F9ZyNg7".getBytes());
        byte[] digest = mac.digest(src);
        String encodeHexStr = HexExtUtil.encodeHexStr(digest, false);
        log.info("SLX 终端权健 encryption :{}", encodeHexStr);
        authLog.info("SLX 终端权健 encryption :{}", encodeHexStr);
//        authResp.setSecretKey(new FixString(digest));
//        authResp.setSecretKeyLength((short) digest.length);

    }

    /**
     * 二维码下发
     *
     * @param authResp authResp
     */
    private void sendQrCode(McAuthResp authResp) {
        McQrCodeCommandDTO qrCodeCommand = new McQrCodeCommandDTO();
        qrCodeCommand.setIdCode(authResp.getIdCode());
        qrCodeCommand.setUrl(machineService.getQrCode());
        qrCodeCommand.setUrlLength((byte) qrCodeCommand.getUrl().length());
        commandFactory.getExecute(McCommandEnum.QR_CODE).execute(qrCodeCommand);
        log.info("QrCodeCommand idCode:{} qrCode:{} ", authResp.getIdCode(), machineService.getQrCode());
        authLog.info("QrCodeCommand idCode:{} qrCode:{} ", authResp.getIdCode(), machineService.getQrCode());
    }
}
