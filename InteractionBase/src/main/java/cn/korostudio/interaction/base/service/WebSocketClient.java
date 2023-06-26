package cn.korostudio.interaction.base.service;

import cn.korostudio.interaction.base.data.Server;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient implements Connect{
    private final Server server;
    public WebSocketClient(URI serverUri, Server server) {
        super(serverUri);
        this.server = server;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        PlatformConnect.getConnectMap().put(server.getId(),this);
        PlatformConnect.getServerMap().put(server.getId(),server);
    }

    @Override
    public void onMessage(String message) {
        //Not Use
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        PlatformMessage.getMessage(bytes.array());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        PlatformConnect.removeServer(server);
    }

    @Override
    public void onError(Exception ex) {
        PlatformConnect.removeServer(server);
    }

    @Override
    public void sendMessage(byte[] data) {
        synchronized (this) {
            send(data);
        }
    }
}
