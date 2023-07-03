package cn.korostudio.interaction.base.service;

import cn.korostudio.interaction.base.data.Server;
import cn.korostudio.interaction.base.event.EventBus;
import cn.korostudio.interaction.base.event.connect.ConnectEvent;
import cn.korostudio.interaction.base.event.connect.ConnectStatus;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient implements Connect {
    private final Server server;

    public WebSocketClient(URI serverUri, Server server) {
        super(serverUri);
        this.server = server;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        ConnectManager.getConnectMap().put(server.getId(), this);
        ConnectManager.getServerMap().put(server.getId(), server);
        EventBus.push(new ConnectEvent(server.getId(), ConnectStatus.OnOpen));
    }

    @Override
    public void onMessage(String message) {
        //Not Use
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        MessageManager.getMessage(bytes.array());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        ConnectManager.removeServer(server);
    }

    @Override
    public void onError(Exception ex) {
        ConnectManager.removeServer(server);
    }

    @Override
    public void sendMessage(byte[] data) {
        synchronized (this) {
            send(data);
        }
    }
}
