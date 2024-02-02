package com.foxapplication.mc.interaction.base.service;

import com.foxapplication.mc.interaction.base.data.Server;
import com.foxapplication.mc.interaction.base.event.EventBus;
import com.foxapplication.mc.interaction.base.event.connect.ConnectEvent;
import com.foxapplication.mc.interaction.base.event.connect.ConnectStatus;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * WebSocketClient类是一个WebSocket客户端，继承自org.java_websocket.client.WebSocketClient类，并实现了Connect接口。
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient implements Connect {
    private final Server server;

    /**
     * 构造方法，创建一个WebSocketClient对象。
     *
     * @param serverUri 服务器URI
     * @param server    服务器对象
     */
    public WebSocketClient(URI serverUri, Server server) {
        super(serverUri);
        this.server = server;
    }

    /**
     * 当WebSocket连接打开时调用的方法。
     *
     * @param handshakedata 握手数据
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        ConnectManager.getConnectMap().put(server.getId(), this);
        ConnectManager.getServerMap().put(server.getId(), server);
        EventBus.push(new ConnectEvent(server.getId(), ConnectStatus.OnOpen));
    }

    /**
     * 当接收到文本消息时调用的方法。
     *
     * @param message 接收到的文本消息
     */
    @Override
    public void onMessage(String message) {
        //Not Use
    }

    /**
     * 当接收到二进制消息时调用的方法。
     *
     * @param bytes 接收到的二进制消息
     */
    @Override
    public void onMessage(ByteBuffer bytes) {
        MessageManager.getMessage(bytes.array());
    }

    /**
     * 当WebSocket连接关闭时调用的方法。
     *
     * @param code   关闭代码
     * @param reason 关闭原因
     * @param remote 是否是远程关闭
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        ConnectManager.removeServer(server);
        EventBus.push(new ConnectEvent(server.getId(), ConnectStatus.OnClose));
    }

    /**
     * 当发生错误时调用的方法。
     *
     * @param ex 异常对象
     */
    @Override
    public void onError(Exception ex) {
        ConnectManager.removeServer(server);
        EventBus.push(new ConnectEvent(server.getId(), ConnectStatus.OnClose));
    }

    /**
     * 发送消息的方法。
     *
     * @param data 要发送的数据
     */
    @Override
    public void sendMessage(byte[] data) {
        synchronized (this) {
            send(data);
        }
    }
}
