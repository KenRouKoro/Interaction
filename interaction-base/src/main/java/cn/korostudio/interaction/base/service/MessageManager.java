package cn.korostudio.interaction.base.service;

import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.korostudio.interaction.base.BaseClient;
import cn.korostudio.interaction.base.data.BaseMessage;
import cn.korostudio.interaction.base.event.ServiceMessageBus;
import cn.korostudio.interaction.base.util.KryoUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
public class MessageManager {
    public static final String ALL = "ALL";
    public static final String LOOP = "LOOP";
    @Getter
    protected static final KryoUtil<BaseMessage> serializable = new KryoUtil<>(BaseMessage.class);
    protected static final ExecutorService messageWorker = ThreadUtil.newFixedExecutor(8, "message-worker", true);
    @Getter
    protected static MessageManager platformMessage = null;

    public static void send(BaseMessage message) {
        if (platformMessage == null) {
            log.warn("未初始化任何送信通道，发送失败");
            return;
        }
        if (message.getTarget().equals(LOOP)) {
            getMessage(message);
            return;
        }
        if (message.getTarget().equals(ALL)) {
            ConnectManager.getServerMap().values().forEach(server -> {
                BaseMessage sendMessage = new BaseMessage();
                sendMessage.setForm(message.getForm());
                sendMessage.setService(message.getService());
                sendMessage.setMessage(message.getMessage());
                sendMessage.setTarget(server.getId());
                sendMessage(sendMessage);
            });
            return;
        }
        String target = message.getTarget();
        List<String> targetList = StrSplitter.split(target, ',', 0, true, true);//切割目标，逐一发送
        targetList.forEach(str -> {
            BaseMessage sendMessage = new BaseMessage();
            sendMessage.setForm(message.getForm());
            sendMessage.setService(message.getService());
            sendMessage.setMessage(message.getMessage());
            sendMessage.setTarget(str);
            sendMessage(sendMessage);
        });
    }

    public static void getMessage(BaseMessage message) {
        ServiceMessageBus.push(message);
    }

    public static void getMessage(byte[] message) {
        ServiceMessageBus.push(serializable.deserialize(message));
    }

    protected static void sendMessage(BaseMessage message) {
        String target = message.getTarget();
        message.setForm(BaseClient.getMine().getAddress());
        if (StrUtil.isBlankIfStr(target) || target.equals("NONE")) {
            log.warn("空发送对象,取消信息发送");
            return;
        }
        Connect session = ConnectManager.getConnectMap().get(target);
        if (session != null) {
            try {
                messageWorker.execute(() -> {
                    session.sendMessage(serializable.serialize(message));
                });
            } catch (Exception e) {
                log.error("送信执行线程异常", e);
            }
        }
    }
}
