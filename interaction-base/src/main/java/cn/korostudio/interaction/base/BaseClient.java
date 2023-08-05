package cn.korostudio.interaction.base;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.base.data.BaseMessage;
import cn.korostudio.interaction.base.data.Server;
import cn.korostudio.interaction.base.event.EventBus;
import cn.korostudio.interaction.base.event.ServiceMessageBus;
import cn.korostudio.interaction.base.event.connect.ConnectEvent;
import cn.korostudio.interaction.base.event.connect.ConnectStatus;
import cn.korostudio.interaction.base.service.ConnectManager;
import cn.korostudio.interaction.base.service.MessageManager;
import cn.korostudio.interaction.base.service.WebSocketServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.WebSocketDefaultHandler;
import org.smartboot.http.server.handler.WebSocketRouteHandler;

import java.util.List;
@Slf4j
public class BaseClient {

    @Getter
    @Setter
    private static Server mine;
    @Getter
    private static WebSocketRouteHandler routeHandle;
    @Getter
    private static HttpBootstrap bootstrap;

    /**
     * 初始化方法，启动内部定时器，注册默认回调
     */
    public static void init(Server mine) {
        log.info("正在初始化Interaction核心");
        BaseClient.mine = mine;
        log.info("正在注册信息回调");
        ServiceMessageBus.subscribe("server_info", message -> {
            Server server = ObjectUtil.deserialize(message.getMessage());
            ConnectManager.connect(server);
        });

        ServiceMessageBus.subscribe("server_info_update", message -> {
            Server server = ObjectUtil.deserialize(message.getMessage());
            ConnectManager.update(server);
        });

        ServiceMessageBus.subscribe("server_info_list", message -> {
            List<Server> serverList = ObjectUtil.deserialize(message.getMessage());
            serverList.forEach(ConnectManager::connect);
        });

        ServiceMessageBus.subscribe("get_server_info_update", message -> {
            String target = message.getForm();
            BaseMessage baseMessage = new BaseMessage("server_info_update", target, ObjectUtil.serialize(BaseClient.getMine()));
            MessageManager.send(baseMessage);
        });

        ServiceMessageBus.subscribe("get_server_info_list", message -> {
            String target = message.getForm();
            BaseMessage baseMessage = new BaseMessage("server_info_list", target, ObjectUtil.serialize(ListUtil.toList(ConnectManager.getServerMap().values())));
            MessageManager.send(baseMessage);
        });
        log.info("正在注册基本事件");
        EventBus.subscribe(ConnectEvent.class, event -> {
            if (event.getStatus() == ConnectStatus.OnOpen) {
                MessageManager.send(new BaseMessage("get_server_info_update", event.getId()));
                MessageManager.send(new BaseMessage("get_server_info_list", event.getId()));
            }
        });

        if (!Config.isCenter) {
            log.info("正在启动内置WS服务端，端口：{}",mine.getPort());
            bootstrap = new HttpBootstrap();
            bootstrap.setPort(mine.getPort());
            routeHandle = new WebSocketRouteHandler();
            routeHandle.route("/ws", new WebSocketServer());
            bootstrap.webSocketHandler(routeHandle);
            bootstrap.start();
        }

        if (Config.enableCenter) {
            log.info("正在尝试链接中央节点");
            ConnectManager.connect(Config.centerServer);
        }

        log.info("初始化完成");
    }
}
