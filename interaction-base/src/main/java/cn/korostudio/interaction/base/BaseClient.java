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
import cn.korostudio.interaction.base.service.PlatformConnect;
import cn.korostudio.interaction.base.service.PlatformMessage;
import lombok.Getter;
import lombok.Setter;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.WebSocketDefaultHandler;
import org.smartboot.http.server.handler.WebSocketRouteHandler;

import java.util.List;

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
        BaseClient.mine = mine;

        ServiceMessageBus.subscribe("server_info", message -> {
            Server server = ObjectUtil.deserialize(message.getMessage());
            if (PlatformConnect.getConnectMap().get(server.getId()) == null) {
                BaseMessage baseMessage = new BaseMessage("server_info", PlatformMessage.ALL, ObjectUtil.serialize(server));
                PlatformMessage.send(baseMessage);
            }
            PlatformConnect.connect(server);
        });

        ServiceMessageBus.subscribe("server_info_list", message -> {
            List<Server> serverList = ObjectUtil.deserialize(message.getMessage());
            serverList.forEach(PlatformConnect::connect);
        });

        ServiceMessageBus.subscribe("get_server_info", message -> {
            String target = message.getForm();
            BaseMessage baseMessage = new BaseMessage("server_info", target, ObjectUtil.serialize(BaseClient.getMine()));
            PlatformMessage.send(baseMessage);
        });

        ServiceMessageBus.subscribe("get_server_info_list", message -> {
            String target = message.getForm();
            BaseMessage baseMessage = new BaseMessage("server_info_list", target, ObjectUtil.serialize(ListUtil.toList(PlatformConnect.getServerMap().values())));
            PlatformMessage.send(baseMessage);
        });

        EventBus.subscribe(ConnectEvent.class, event -> {
            if (event.getStatus() == ConnectStatus.OnOpen) {
                PlatformMessage.send(new BaseMessage("get_server_info", event.getId()));
                PlatformMessage.send(new BaseMessage("get_server_info_list", event.getId()));
            }
        });

        if (!Config.isCenter) {
            bootstrap = new HttpBootstrap();
            bootstrap.setPort(mine.getPort());
            routeHandle = new WebSocketRouteHandler();
            routeHandle.route("/ws", new WebSocketDefaultHandler());
            bootstrap.start();
        }

        if (Config.enableCenter) {
            PlatformConnect.connect(Config.centerServer);
        }


    }
}
