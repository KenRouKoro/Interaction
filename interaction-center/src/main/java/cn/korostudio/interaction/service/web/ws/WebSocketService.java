package cn.korostudio.interaction.service.web.ws;

import cn.hutool.core.util.StrUtil;
import cn.korostudio.interaction.App;
import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.base.data.BaseMessage;
import cn.korostudio.interaction.base.event.EventBus;
import cn.korostudio.interaction.base.event.connect.ConnectEvent;
import cn.korostudio.interaction.base.event.connect.ConnectStatus;
import cn.korostudio.interaction.base.service.PlatformConnect;
import cn.korostudio.interaction.base.service.PlatformMessage;
import cn.korostudio.interaction.base.util.KryoUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.ServerEndpoint;
import org.noear.solon.core.message.Listener;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(path = "/ws")
@Slf4j
public class WebSocketService implements Listener {
    private final KryoUtil<BaseMessage> serializable = new KryoUtil<>(BaseMessage.class);
    @Getter
    private final static ConcurrentHashMap<String,Session>sessionMap = new ConcurrentHashMap<>();
    @Override
    public void onOpen(Session session){
        String token = session.param("token");
        String id = session.param("id");
        if (StrUtil.isBlankIfStr(token)|| !Config.ConnectToken.equals(token)){
            try {
                session.send("错误的Token");
                session.close();
            } catch (IOException e) {
                log.error("ws握手关闭失败",e);
                return;
            }
            return;
        }
        sessionMap.put(id,session);
        PlatformConnect.getConnectMap().put(id,new ConnectSession(session));
        EventBus.push(new ConnectEvent(id, ConnectStatus.OnOpen));
        log.info("与{}的握手完成",id);
    }
    @Override
    public void onMessage(Session session, Message message){
        String id = session.param("id");
        PlatformMessage.getMessage(serializable.deserialize(message.body()));
        log.debug("收到{}的信息",id);

    }
    @Override
    public void onClose(Session session) {
        String id = session.param("id");
        sessionMap.remove(id);
        PlatformConnect.removeServer(id);
        log.info("与{}的连接关闭",id);
    }
    @Override
    public void onError(Session session, Throwable error) {
        String id = session.param("id");
        sessionMap.remove(id);
        PlatformConnect.removeServer(id);
        log.error("与{}连接发生错误",id,error);
    }

}
