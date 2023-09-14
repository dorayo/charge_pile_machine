package com.huamar.charge.pile.server.service.handler;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.BaseResp;
import com.huamar.charge.pile.convert.MachineAuthenticationConvert;
import com.huamar.charge.pile.entity.dto.MachineAuthenticationReqDTO;
import com.huamar.charge.pile.entity.dto.command.McQrCodeCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.resp.McAuthResp;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FixString;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.answer.McAnswerExecute;
import com.huamar.charge.pile.server.service.machine.MachineService;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 终端鉴权
 * Date: 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineAuthenticationHandler implements MachinePacketHandler<DataPacket> {


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
        MachineAuthenticationReqDTO reqDTO = this.reader(packet);
        log.info("终端鉴权，loginNumber={} time={} ip={}", reqDTO.getLoginNumber(), reqDTO.getTerminalTime(), sessionChannel.getIp());
        McAuthResp authResp = new McAuthResp();
        authResp.setTime(BCDUtils.bcdTime());
        authResp.setEncryptionType((byte) 0);
        authResp.setSecretKeyLength((short) 0);
        authResp.setSecretKey(new FixString(new byte[0]));
        // 应答实现
        McAnswerExecute<BaseResp> answerExecute = answerFactory.getExecute(McAnswerEnum.AUTH);
        // TODO 验证充电桩 业务逻辑
        PileDTO pile = machineService.getPile(reqDTO.getIdCode());
        if (Objects.isNull(pile)) {
            authResp.setStatus(MachineAuthStatus.TERMINAL_NOT_REGISTER.getCode());
            answerExecute.execute(authResp, sessionChannel);
            return;
        }

        authResp.setIdCode(pile.getPileCode());
        authResp.setStatus(MachineAuthStatus.SUCCESS.getCode());
        // 更新对象
        PileDTO update = new PileDTO();
        update.setId(pile.getId());
        if (StringUtils.isBlank(pile.getMacAddress())) {
            update.setMacAddress(reqDTO.getMacAddress().toString());
        }

        // mac已登录成功过
        if (!StringUtils.equalsAnyIgnoreCase(pile.getMacAddress(), reqDTO.getMacAddress().toString())) {
            authResp.setStatus(MachineAuthStatus.TERMINAL_INFO_NOT_FOUND.getCode());
            answerExecute.execute(authResp, sessionChannel);
            return;
        }

        if(StringUtils.equals(pile.getPileVersion(), reqDTO.getProgramVersionNum())){
            pile.setPileVersion(reqDTO.getProgramVersionNum());
        }

        // 私有加密逻辑
        this.encryptionSecretKey(reqDTO, authResp);
        authResp.setStatus(MachineAuthStatus.SUCCESS.getCode());
        answerExecute.execute(authResp, sessionChannel);

        // 二维码下发
        this.sendQrCode(authResp);
        // 设备更新
        pileMessageProduce.send(pileMessageProduce.getPileMachineProperties().getPileMessageQueue(), new MessageData<>(MessageCodeEnum.PILE_UPDATE, update));

        //电价更新
        pileMessageProduce.send(pileMessageProduce.getPileMachineProperties().getPileMessageQueue(), new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));

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
    private void encryptionSecretKey(MachineAuthenticationReqDTO reqDTO, McAuthResp authResp) {
        if ((reqDTO.getBoardNum() & 0xff) != 160) {
            return;
        }
        authResp.setEncryptionType((byte) 1);
        String src = reqDTO.getIdCode() + authResp.getTime() + reqDTO.getMacAddress().toString();
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, "VB6dQCFh2F9ZyNg7".getBytes());
        byte[] digest = mac.digest(src);
        authResp.setSecretKey(new FixString(digest));
        authResp.setSecretKeyLength((short) digest.length);
        String encodeHexStr = HexExtUtil.encodeHexStr(digest, false);
        log.info("encryption :{}", encodeHexStr);
    }

    /**
     * 二维码下发
     * @param authResp authResp
     */
    private void sendQrCode(McAuthResp authResp){
        McQrCodeCommandDTO qrCodeCommand = new McQrCodeCommandDTO();
        qrCodeCommand.setIdCode(authResp.getIdCode());
        qrCodeCommand.setUrl(machineService.getQrCode());
        commandFactory.getExecute(McCommandEnum.QR_CODE).execute(qrCodeCommand);
    }
}
