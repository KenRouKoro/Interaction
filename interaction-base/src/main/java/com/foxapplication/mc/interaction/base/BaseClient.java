package com.foxapplication.mc.interaction.base;

import com.foxapplication.embed.hutool.core.collection.ListUtil;
import com.foxapplication.embed.hutool.core.util.ObjectUtil;
import com.foxapplication.embed.hutool.log.Log;
import com.foxapplication.embed.hutool.log.LogFactory;
import com.foxapplication.mc.core.config.BeanFoxConfig;
import com.foxapplication.mc.core.config.LocalFoxConfig;
import com.foxapplication.mc.interaction.base.config.InteractionBaseConfig;
import com.foxapplication.mc.interaction.base.data.BaseMessage;
import com.foxapplication.mc.interaction.base.data.Server;
import com.foxapplication.mc.interaction.base.event.EventBus;
import com.foxapplication.mc.interaction.base.event.ServiceMessageBus;
import com.foxapplication.mc.interaction.base.event.connect.ConnectEvent;
import com.foxapplication.mc.interaction.base.event.connect.ConnectStatus;
import com.foxapplication.mc.interaction.base.service.ConnectManager;
import com.foxapplication.mc.interaction.base.service.MessageManager;
import com.foxapplication.mc.interaction.base.service.WebSocketServer;
import lombok.Getter;
import lombok.Setter;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.WebSocketRouteHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础客户端类
 */
public class BaseClient {

    /**
     * 日志对象
     */
    private static final Log log = LogFactory.get();

    /**
     * 服务器对象
     */
    @Getter
    @Setter
    private static Server mine;

    /**
     * WebSocket路由处理器
     */
    @Getter
    private static WebSocketRouteHandler routeHandle;

    /**
     * HTTP引导程序
     */
    @Getter
    private static HttpBootstrap bootstrap;

    /**
     * 交互基础配置
     */
    @Getter
    private static InteractionBaseConfig config;

    /**
     * BeanFox配置
     */
    @Getter
    private static BeanFoxConfig beanFoxConfig;

    /**
     * 本地Fox配置
     */
    @Getter
    private static LocalFoxConfig localFoxConfig;

    /**
     * 初始化方法，启动内部定时器，注册默认回调
     *
     * @param mine 服务器信息
     */
    public static void init(Server mine) {
        log.info("正在初始化Interaction基本核心");

        localFoxConfig = new LocalFoxConfig(InteractionBaseConfig.class);
        beanFoxConfig = localFoxConfig.getBeanFoxConfig();
        config = (InteractionBaseConfig) beanFoxConfig.getBean();

        BaseClient.mine = mine;
        log.info("正在注册信息回调");
        ServiceMessageBus.subscribe("server_info", message -> {
            Server server = ObjectUtil.deserialize(message.getMessage(),Server.class);
            ConnectManager.connect(server);
        });

        ServiceMessageBus.subscribe("server_info_update", message -> {
            Server server = ObjectUtil.deserialize(message.getMessage(),Server.class);
            ConnectManager.update(server);
            log.info("已更新 {} 的数据： {}",server.getId(),server);
        });

        ServiceMessageBus.subscribe("server_info_list", message -> {
            List<Server> serverList = ObjectUtil.deserialize(message.getMessage(),List.class,Server.class);
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

        if (!config.isOtherServer) {
            log.info("正在启动内置WS服务端，端口：{}", mine.getPort());
            bootstrap = new HttpBootstrap();
            bootstrap.setPort(mine.getPort());
            routeHandle = new WebSocketRouteHandler();
            routeHandle.route("/ws", new WebSocketServer());
            bootstrap.webSocketHandler(routeHandle);
            bootstrap.start();
        }

        if (config.enableCenter) {
            log.info("正在尝试链接中央节点");
            ConnectManager.connect(new Server(config.centerID, config.centerAddress, config.centerUseSSL,config.centerPort,new ArrayList<>()));
        }

        log.info("初始化完成");
    }

    /**
     * 停止方法
     */
    public static void stop(){
        ConnectManager.getConnectMap().forEach((key,server) -> server.close());
        ConnectManager.getConnectMap().keySet().forEach(ConnectManager::removeServer);

        if (bootstrap!=null) bootstrap.shutdown();
    }
}
