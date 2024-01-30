package com.foxapplication.mc.interaction.base.service;

import com.foxapplication.embed.hutool.core.text.StrSplitter;
import com.foxapplication.embed.hutool.core.thread.ThreadUtil;
import com.foxapplication.embed.hutool.core.util.StrUtil;
import com.foxapplication.embed.hutool.log.Log;
import com.foxapplication.embed.hutool.log.LogFactory;
import com.foxapplication.mc.interaction.base.BaseClient;
import com.foxapplication.mc.interaction.base.data.BaseMessage;
import com.foxapplication.mc.interaction.base.event.ServiceMessageBus;
import com.foxapplication.mc.interaction.base.util.KryoUtil;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 消息管理器
 */
public class MessageManager {
    /**
     * 发送给所有目标
     */
    public static final String ALL = "ALL";
    /**
     * 循环发送
     */
    public static final String LOOP = "LOOP";
    /**
     * 不发送
     */
    public static final String NONE = "NONE";
    /**
     * 序列化工具
     */
    @Getter
    protected static final KryoUtil<BaseMessage> serializable = new KryoUtil<>(BaseMessage.class);
    /**
     * 消息处理线程池
     */
    protected static final ExecutorService messageWorker = ThreadUtil.newFixedExecutor(8, "message-worker", true);
    /**
     * 日志记录器
     */
    private static final Log log = LogFactory.get();
    /**
     * 平台消息管理器
     */
    @Getter
    protected static MessageManager platformMessage = null;

    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    public static void send(BaseMessage message) {
        if (platformMessage == null) {
            log.warn("未初始化任何送信通道，发送失败");
            return;
        }
        switch (message.getTarget()) {
            case LOOP:
                getMessage(message);
                break;
            case ALL:
                ConnectManager.getServerMap().values().forEach(server -> {
                    message.setTarget(server.getId());
                    sendMessageDirectly(message);
                });
                break;
            default:
                sendToMultipleTargets(message);
                break;
        }
    }

    /**
     * 发送消息给多个目标
     *
     * @param message 消息对象
     */
    private static void sendToMultipleTargets(BaseMessage message) {
        List<String> targetList = StrSplitter.split(message.getTarget(), ',', 0, true, true);
        targetList.forEach(target -> {
            message.setTarget(target);
            sendMessageDirectly(message);
        });
    }

    /**
     * 获取消息
     *
     * @param message 消息对象
     */
    public static void getMessage(BaseMessage message) {
        ServiceMessageBus.push(message);
    }

    /**
     * 获取消息
     *
     * @param message 消息字节数组
     */
    public static void getMessage(byte[] message) {
        ServiceMessageBus.push(serializable.deserialize(message));
    }

    /**
     * 直接发送消息
     *
     * @param message 消息对象
     */
    protected static void sendMessageDirectly(BaseMessage message) {
        String target = message.getTarget();
        message.setForm(BaseClient.getMine().getAddress());
        if (StrUtil.isBlank(target) || NONE.equals(target)) {
            log.warn("空发送对象,取消信息发送");
            return;
        }
        Connect session = ConnectManager.getConnectMap().get(target);
        if (session != null) {
            messageWorker.execute(() -> {
                try {
                    session.sendMessage(serializable.serialize(message));
                } catch (Exception e) {
                    log.error("送信执行线程异常", e);
                }
            });
        }
    }
}
