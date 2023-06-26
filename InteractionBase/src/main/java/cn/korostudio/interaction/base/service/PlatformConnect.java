package cn.korostudio.interaction.base.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.korostudio.interaction.base.data.Server;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public abstract class PlatformConnect {
    @Getter
    private static final ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();
    private static final ExecutorService connectWorker = ThreadUtil.newSingleExecutor();

    @Getter
    protected static PlatformConnect platformConnect;
    public abstract void connectServer(Server server);
    public static void connect(Server server){
        connectWorker.execute(()->{
            platformConnect.connectServer(server);
        });
    }
}
