package com.foxapplication.mc.interaction.base.service;

import com.foxapplication.embed.hutool.log.Log;
import com.foxapplication.embed.hutool.log.LogFactory;
import com.foxapplication.mc.interaction.base.BaseClient;
import com.foxapplication.mc.interaction.base.config.InteractionBaseConfig;
import com.foxapplication.mc.interaction.base.event.EventBus;
import com.foxapplication.mc.interaction.base.event.connect.ConnectEvent;
import com.foxapplication.mc.interaction.base.event.connect.ConnectStatus;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.handler.WebSocketDefaultHandler;
import org.smartboot.http.server.impl.Request;

import java.util.Optional;

/**
 * WebSocket服务器
 */
public class WebSocketServer extends WebSocketDefaultHandler {

    private static final Log log = LogFactory.get();

    /**
     * 握手处理
     *
     * @param request  WebSocket请求
     * @param response WebSocket响应
     */
    @Override
    public void onHandShake(WebSocketRequest request, WebSocketResponse response) {
        try {
            String token = request.getParameters().get("token")[0];
            String id = request.getParameters().get("id")[0];
            if (!token.equals(BaseClient.getConfig().ConnectToken)) {
                log.error("握手失败,Token验证失败");
                return;
            }
            WebSocketServerConnect mine = new WebSocketServerConnect(response);
            ConnectManager.getConnectMap().put(id, mine);
            EventBus.push(new ConnectEvent(id, ConnectStatus.OnOpen));
        } catch (Exception e) {
            log.error("握手失败", e);
            response.close();
        }
    }

    /**
     * 连接关闭处理
     *
     * @param request 请求
     */
    @Override
    public void onClose(Request request) {
        String id = request.getParameter("id");
        log.info("连接{}关闭", id);
        ConnectManager.removeServer(id);
    }

    /**
     * 处理二进制消息
     *
     * @param request  WebSocket请求
     * @param response WebSocket响应
     * @param data     消息数据
     */
    @Override
    public void handleBinaryMessage(WebSocketRequest request, WebSocketResponse response, byte[] data) {
        byte[] messageSource = request.getPayload();
        MessageManager.getMessage(messageSource);
    }

    /**
     * 处理文本消息
     *
     * @param request  WebSocket请求
     * @param response WebSocket响应
     * @param data     消息数据
     */
    @Override
    public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
        byte[] messageSource = request.getPayload();
        MessageManager.getMessage(messageSource);
    }

    /**
     * 错误处理
     *
     * @param request   WebSocket请求
     * @param throwable 异常
     */
    @Override
    public void onError(WebSocketRequest request, Throwable throwable) {
        // 使用Optional来避免潜在的NullPointerException
        String id = Optional.ofNullable(request.getParameters().get("id")).map(params -> params[0]).orElse("");
        log.error("与{}的连接异常", id, throwable);
        ConnectManager.removeServer(id);
    }

    /**
     * WebSocket服务器连接
     */
    public static class WebSocketServerConnect implements Connect {
        WebSocketResponse webSocketResponse;

        public WebSocketServerConnect(WebSocketResponse webSocketResponse) {
            this.webSocketResponse = webSocketResponse;
        }

        /**
         * 发送消息
         *
         * @param data 消息数据
         */
        @Override
        public void sendMessage(byte[] data) {
            webSocketResponse.sendBinaryMessage(data);
            webSocketResponse.flush();
        }

        /**
         * 关闭连接
         */
        @Override
        public void close() {
            webSocketResponse.close();
        }
    }
}
