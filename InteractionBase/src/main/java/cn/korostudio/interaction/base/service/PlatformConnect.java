package cn.korostudio.interaction.base.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.URLUtil;
import cn.korostudio.interaction.base.BaseClient;
import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.base.data.Server;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
@Slf4j
public class PlatformConnect {
    @Getter
    private static final ConcurrentHashMap<String, Server> serverMap = new ConcurrentHashMap<>();
    @Getter
    private static final ConcurrentHashMap<String,Connect>connectMap = new ConcurrentHashMap<>();
    private static final ExecutorService connectWorker = ThreadUtil.newSingleExecutor();
    @Getter
    private static final ConcurrentHashMap<String,Thread>connectThreadMap = new ConcurrentHashMap<>();

    public static void removeServer(String id){
        serverMap.remove(id);
        connectMap.remove(id);
        connectThreadMap.remove(id);
    }
    public static void removeServer(Server server){
        removeServer(server.getId());
    }


    @Getter
    protected static PlatformConnect platformConnect = new PlatformConnect();
    public void connectServer(Server server){
        if(server.getId().equals(BaseClient.getMine().getId())){
            return;
        }
        Server hasServer = getServerMap().get(server.getId());
        Connect connect = connectMap.get(server.getId());
        if (hasServer!=null){
            hasServer.setService(server.getService());
            return;
        }
        if (connect != null){
            getServerMap().put(server.getId(),server);
            return;
        }

        WebSocketClient client;
        try {
            client = new WebSocketClient(new URI((server.isUseSSL()?"wss":"ws")+"://"+server.getAddress()+(server.getPort()>=0?":"+server.getPort():"")+"?token="+ URLUtil.encode(Config.ConnectToken)+"&id="+URLUtil.encode(BaseClient.getMine().getId())),server);
        } catch (URISyntaxException e) {
            log.error("拼接URI失败",e);
            return;
        }
        Thread clientThread = new Thread(()->{
            try {
                client.connectBlocking();
            } catch (InterruptedException e) {
                log.error("WebSocket客户端连接出错",e);
            }
        });
        clientThread.setName("WSConnect-"+clientThread.getId());
        connectThreadMap.put(server.getId(),clientThread);
        clientThread.start();

    }
    public static void connect(Server server){
        connectWorker.execute(()->{
            platformConnect.connectServer(server);
        });
    }
}
