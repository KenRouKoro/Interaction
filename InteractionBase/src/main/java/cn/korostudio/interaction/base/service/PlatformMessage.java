package cn.korostudio.interaction.base.service;

import cn.hutool.core.text.StrSplitter;
import cn.korostudio.interaction.base.BaseClient;
import cn.korostudio.interaction.base.data.BaseMessage;
import cn.korostudio.interaction.base.event.ServiceMessageBus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class PlatformMessage {
    public static final String ALL = "ALL";
    public static final String LOOP = "LOOP";

    @Getter
    protected static PlatformMessage platformMessage=null;
    public static void send(BaseMessage message){
        if (platformMessage==null){
            log.warn("未初始化任何送信通道，发送失败");
            return;
        }
        if (message.getTarget().equals(LOOP)){
            getMessage(message);
            return;
        }
        if (message.getTarget().equals(ALL)){
            PlatformConnect.getServers().values().forEach(server->{
                BaseMessage sendMessage = new BaseMessage();
                sendMessage.setForm(message.getForm());
                sendMessage.setService(message.getService());
                sendMessage.setMessage(message.getMessage());
                sendMessage.setTarget(server.getId());
                platformMessage.sendMessage(sendMessage);
            });
            return;
        }
        String target = message.getTarget();
        List<String> targetList = StrSplitter.split(target, ',', 0, true, true);//切割目标，逐一发送
        targetList.forEach(str->{
            BaseMessage sendMessage = new BaseMessage();
            sendMessage.setForm(message.getForm());
            sendMessage.setService(message.getService());
            sendMessage.setMessage(message.getMessage());
            sendMessage.setTarget(str);
            platformMessage.sendMessage(sendMessage);
        });
    }
    public static void getMessage(BaseMessage message) {
        ServiceMessageBus.push(message);
    }

    public abstract void sendMessage(BaseMessage message);
}
