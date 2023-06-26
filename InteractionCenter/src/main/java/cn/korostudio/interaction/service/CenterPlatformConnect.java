package cn.korostudio.interaction.service;

import cn.korostudio.interaction.base.data.Server;
import cn.korostudio.interaction.base.service.PlatformConnect;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.http.WebSocket;

public class CenterPlatformConnect extends PlatformConnect {
    @Override
    public void connectServer(Server server) {
        Server hasServer = getServers().get(server.getId());
        if (hasServer!=null){
            hasServer.setService(server.getService());
            return;
        }
    }


}
