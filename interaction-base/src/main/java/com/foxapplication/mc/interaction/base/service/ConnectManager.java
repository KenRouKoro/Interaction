package com.foxapplication.mc.interaction.base.service;

import com.foxapplication.embed.hutool.core.util.URLUtil;
import com.foxapplication.embed.hutool.log.Log;
import com.foxapplication.embed.hutool.log.LogFactory;
import com.foxapplication.mc.interaction.base.BaseClient;
import com.foxapplication.mc.interaction.base.config.InteractionBaseConfig;
import com.foxapplication.mc.interaction.base.data.Server;
import lombok.Getter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 连接管理器
 */
public class ConnectManager {
    /**
     * 服务器映射表
     */
    @Getter
    private static final ConcurrentHashMap<String, Server> serverMap = new ConcurrentHashMap<>();

    /**
     * 连接映射表
     */
    @Getter
    private static final ConcurrentHashMap<String, Connect> connectMap = new ConcurrentHashMap<>();

    /**
     * 连接工作线程池
     */
    private static final ExecutorService connectWorker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 连接线程映射表
     */
    @Getter
    private static final ConcurrentHashMap<String, Thread> connectThreadMap = new ConcurrentHashMap<>();

    /**
     * 服务器服务管理器
     */
    private static final ServerServiceManager serverServiceManager = new ServerServiceManager();

    /**
     * 日志记录器
     */
    private static final Log log = LogFactory.get();

    /**
     * 平台连接管理器
     */
    @Getter
    protected static ConnectManager platformConnect = new ConnectManager();

    /**
     * 获取服务的服务器列表
     *
     * @param service 服务名称
     * @return 服务器列表
     */
    public static List<Server> getServiceServers(String service) {
        return serverServiceManager.getServiceServers(service);
    }

    /**
     * 更新服务器服务
     *
     * @param server 服务器
     */
    public static void updateServerService(Server server) {
        serverServiceManager.updateServerService(server);
    }

    /**
     * 添加服务器服务
     *
     * @param server   服务器
     * @param services 服务列表
     */
    public static void addServerService(Server server, List<String> services) {
        serverServiceManager.addServerService(server, services);
    }

    /**
     * 移除服务器服务
     *
     * @param server 服务器
     */
    public static void removeServerService(Server server) {
        serverServiceManager.removeServerService(server);
    }

    /**
     * 移除服务器
     *
     * @param id 服务器ID
     */
    public static void removeServer(String id) {
        serverMap.remove(id);
        connectMap.remove(id);
        connectThreadMap.remove(id);
    }

    /**
     * 移除服务器
     *
     * @param server 服务器
     */
    public static void removeServer(Server server) {
        removeServer(server.getId());
    }

    /**
     * 连接服务器
     *
     * @param server 服务器
     */
    public static void connect(Server server) {
        if (serverMap.get(server.getId()) != null || connectMap.get(server.getId()) != null) {
            log.debug("节点已连接或正在握手");
            return;
        }
        connectWorker.execute(() -> platformConnect.connectServer(server));
    }

    /**
     * 更新服务器
     *
     * @param server 服务器
     */
    public static void update(Server server) {
        platformConnect.updateService(server);
    }

    /**
     * 更新服务
     *
     * @param server 服务器
     */
    public void updateService(Server server) {
        if (server == null) return;
        Server hasServer = getServerMap().get(server.getId());
        if (hasServer == null) {
            getServerMap().put(server.getId(), server);
            return;
        }
        List<String> oldServices = hasServer.getService();
        List<String> newServices = server.getService();
        if (oldServices != null && newServices != null && !oldServices.equals(newServices)) {
            hasServer.setService(newServices);
            serverServiceManager.updateServerService(hasServer);
        }
        if (!hasServer.getId().equals(server.getId())) {
            // 如果ID发生改变，则将serverMap，connectMap，connectThreadMap中的对应旧ID的数据的key更新为新ID
            Server updatedServer = serverMap.remove(hasServer.getId());
            if (updatedServer != null) {
                updatedServer.setId(server.getId());
                serverMap.put(server.getId(), updatedServer);
            }

            Connect updatedConnect = connectMap.remove(hasServer.getId());
            if (updatedConnect != null) {
                connectMap.put(server.getId(), updatedConnect);
            }

            Thread updatedThread = connectThreadMap.remove(hasServer.getId());
            if (updatedThread != null) {
                connectThreadMap.put(server.getId(), updatedThread);
            }
        }
    }

    /**
     * 连接服务器
     *
     * @param server 服务器
     */
    public void connectServer(Server server) {
        if (server.getId().equals(BaseClient.getMine().getId())) {
            log.debug("尝试连接自身，取消连接");
            return;
        }
        serverMap.computeIfAbsent(server.getId(), k -> {
            Connect connect = connectMap.get(k);
            if (connect != null) {
                log.debug("对应节点已连接，完成握手");
                return server;
            }

            WebSocketClient client;
            try {
                client = new WebSocketClient(new URI((server.isUseSSL() ? "wss" : "ws") + "://" + server.getAddress() + (server.getPort() >= 0 ? ":" + server.getPort() : "") + "/ws?token=" + URLUtil.encode(BaseClient.getConfig().getConnectToken()) + "&id=" + URLUtil.encode(BaseClient.getMine().getId())), server);
            } catch (URISyntaxException e) {
                log.error("拼接URI失败", e);
                return null;
            }
            connectWorker.submit(() -> {
                try {
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    log.error("WebSocket客户端连接出错", e);
                    Thread.currentThread().interrupt();
                }
            });

            connectThreadMap.put(k, Thread.currentThread());
            return null;
        });
    }
}
