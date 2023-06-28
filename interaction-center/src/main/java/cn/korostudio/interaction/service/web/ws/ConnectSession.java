package cn.korostudio.interaction.service.web.ws;

import cn.korostudio.interaction.base.service.Connect;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;

import java.io.IOException;

@Slf4j
public class ConnectSession implements Connect {
    Session session;

    ConnectSession(Session session) {
        this.session = session;
    }

    @Override
    public void sendMessage(byte[] data) {
        session.send(Message.wrap(data));
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭WS连接异常", e);
        }
    }
}
