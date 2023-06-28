package cn.korostudio.interaction.base.service;

import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.base.event.EventBus;
import cn.korostudio.interaction.base.event.connect.ConnectEvent;
import cn.korostudio.interaction.base.event.connect.ConnectStatus;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.handler.WebSocketDefaultHandler;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.WebSocketRequestImpl;

import java.util.Arrays;
@Slf4j
public class WebSocketServer extends WebSocketDefaultHandler {

    @Override
    public void onHandShake(WebSocketRequest request, WebSocketResponse response) {
        try {
            String token = request.getParameters().get("token")[0];
            String id = request.getParameters().get("id")[0];
            if (!token.equals(Config.ConnectToken)) {
                log.error("握手失败,Token验证失败");
                return;
            }
            WebSocketServerConnect mine = new WebSocketServerConnect(response);
            PlatformConnect.getConnectMap().put(id, mine);
            EventBus.push(new ConnectEvent(id, ConnectStatus.OnOpen));
        }catch (Exception e){
            log.error("握手失败",e);
            response.close();
        }
    }

    @Override
    public void onClose(Request request) {
        String id = request.getParameter("id");
        log.info("连接{}关闭",id);
        PlatformConnect.removeServer(id);
    }

    @Override
    public void onError(WebSocketRequest request, Throwable throwable) {
        String id = request.getParameters().get("id")[0];
        log.error("与{}的连接异常",id,throwable);
        PlatformConnect.removeServer(id);
    }

    public static class WebSocketServerConnect implements Connect{
        WebSocketResponse webSocketResponse;
        public WebSocketServerConnect(WebSocketResponse webSocketResponse){
            this.webSocketResponse = webSocketResponse;
        }
        @Override
        public void sendMessage(byte[] data) {
            webSocketResponse.sendBinaryMessage(data);
            webSocketResponse.flush();
        }

        @Override
        public void close() {
            webSocketResponse.close();
        }
    }
}
