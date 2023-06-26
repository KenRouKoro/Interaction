package cn.korostudio.interaction.base;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.base.data.BaseMessage;
import cn.korostudio.interaction.base.data.Server;
import cn.korostudio.interaction.base.event.ServiceMessageBus;
import cn.korostudio.interaction.base.service.PlatformConnect;
import cn.korostudio.interaction.base.service.PlatformMessage;
import lombok.Getter;
import lombok.Setter;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.WebSocketDefaultHandler;
import org.smartboot.http.server.handler.WebSocketRouteHandler;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class BaseClient {

    @Getter
    @Setter
    private static Server mine ;
    @Getter
    private static WebSocketRouteHandler routeHandle;
    @Getter
    private static HttpBootstrap bootstrap;

    /**
     * 初始化方法，启动内部定时器,注册默认回调
     */
    public static void init(String heartTime){
        CronUtil.schedule(heartTime, (Task) () -> {

        });
        CronUtil.setMatchSecond(true);
        CronUtil.start(true);

        ServiceMessageBus.subscribe("server_info",message -> {
            Server server = ObjectUtil.deserialize(message.getMessage());
            PlatformConnect.connect(server);
        });

        ServiceMessageBus.subscribe("server_info_list",message -> {
            ArrayList<Server> serverList = ObjectUtil.deserialize(message.getMessage());
            serverList.forEach(PlatformConnect::connect);
        });
        if (!Config.enableCenter){
            bootstrap = new HttpBootstrap();
            bootstrap.setPort(mine.getPort());
            routeHandle = new WebSocketRouteHandler();
            routeHandle.route("/ws",new WebSocketDefaultHandler());
            bootstrap.start();
        }


    }
}
