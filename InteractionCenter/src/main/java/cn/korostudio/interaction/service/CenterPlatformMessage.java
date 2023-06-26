package cn.korostudio.interaction.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.korostudio.interaction.App;
import cn.korostudio.interaction.base.data.BaseMessage;
import cn.korostudio.interaction.base.service.PlatformMessage;
import cn.korostudio.interaction.base.util.KryoUtil;
import cn.korostudio.interaction.service.web.ws.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;

import java.util.concurrent.ExecutorService;

@Slf4j                                                                                                                                                                                                                     
public class CenterPlatformMessage extends PlatformMessage {
    static{
        PlatformMessage.platformMessage = new CenterPlatformMessage();
    }
    private final ExecutorService messageWorker = ThreadUtil.newFixedExecutor(8,"message-worker",true);

    private final KryoUtil<BaseMessage> serializable = new KryoUtil<>(BaseMessage.class);
    @Override
    public void sendMessage(BaseMessage message) {
        String target = message.getTarget();
        if (StrUtil.isBlankIfStr(target)||target.equals("NONE")){
            log.warn("空发送对象,取消信息发送");
            return;
        }
        message.setForm(App.server.getId());
        Session session = WebSocketService.getSessionMap().get(target);
        if (session!=null){
            try {
                messageWorker.execute(()->{
                    session.send(Message.wrap(serializable.serialize(message)));
                });
            }catch (Exception e){
                log.error("送信执行线程异常",e);
            }
        }
    }
}
