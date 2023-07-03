package cn.korostudio.interaction.base.service;

import cn.hutool.core.util.URLUtil;
import cn.korostudio.interaction.base.BaseClient;
import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.base.data.Server;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ConnectManager {
    @Getter
    private static final ConcurrentHashMap<String, Server> serverMap = new ConcurrentHashMap<>();
    @Getter
    private static final ConcurrentHashMap<String, Connect> connectMap = new ConcurrentHashMap<>();
    private static final ExecutorService connectWorker = Executors.newCachedThreadPool();
    @Getter
    private static final ConcurrentHashMap<String, Thread> connectThreadMap = new ConcurrentHashMap<>();

    @Getter
    protected static ConnectManager platformConnect = new ConnectManager();

    private static final ServerServiceManager serverServiceManager = new ServerServiceManager();

    public static List<Server> getServiceServers(String service) {
        return serverServiceManager.getServiceServers(service);
    }

    public static void updateServerService(Server server) {
        serverServiceManager.updateServerService(server);
    }

    public static void addServerService(Server server, List<String> services) {
        serverServiceManager.addServerService(server, services);
    }

    public static void removeServerService(Server server) {
        serverServiceManager.removeServerService(server);
    }

    public static void removeServer(String id) {
        serverMap.remove(id);
        connectMap.remove(id);
        connectThreadMap.remove(id);
    }

    public static void removeServer(Server server) {
        removeServer(server.getId());
    }

    public static void connect(Server server) {
        if (serverMap.get(server.getId()) != null || connectMap.get(server.getId()) != null) {
            log.debug("节点以连接或正在握手");
            return;
        }
        connectWorker.execute(() -> platformConnect.connectServer(server));
    }
    public static void update(Server server) {
        platformConnect.updateService(server);
    }

    public void updateService(Server server) {
        if (server == null) return;
        Server hasServer = getServerMap().get(server.getId());
        if (hasServer == null) return;
        List<String> oldServices = hasServer.getService();
        List<String> newServices = server.getService();
        if (oldServices != null && newServices != null && !oldServices.equals(newServices)) {
            hasServer.setService(newServices);
            serverServiceManager.updateServerService(hasServer);
        }
    }

    public void connectServer(Server server) {
        if (server.getId().equals(BaseClient.getMine().getId())) {
            log.debug("尝试连接自身，取消连接");
            return;
        }
        Server hasServer = getServerMap().get(server.getId());
        Connect connect = connectMap.get(server.getId());
        if (hasServer != null) {
            log.debug("对应节点以完成握手");
            return;
        }
        if (connect != null) {
            log.debug("对应节点以连接，完成握手");
            getServerMap().put(server.getId(), server);
            return;
        }

        WebSocketClient client;
        try {
            client = new WebSocketClient(new URI((server.isUseSSL() ? "wss" : "ws") + "://" + server.getAddress() + (server.getPort() >= 0 ? ":" + server.getPort() : "") + "?token=" + URLUtil.encode(Config.ConnectToken) + "&id=" + URLUtil.encode(BaseClient.getMine().getId())), server);
        } catch (URISyntaxException e) {
            log.error("拼接URI失败", e);
            return;
        }
        connectWorker.submit(() -> {
            try {
                client.connectBlocking();
            } catch (InterruptedException e) {
                log.error("WebSocket客户端连接出错", e);
            }
        });

        connectThreadMap.put(server.getId(), Thread.currentThread());
    }
}