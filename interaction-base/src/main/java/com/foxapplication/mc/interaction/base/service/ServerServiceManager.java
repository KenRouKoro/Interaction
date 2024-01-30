package com.foxapplication.mc.interaction.base.service;

import com.foxapplication.mc.interaction.base.data.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器服务管理器
 */
public class ServerServiceManager {
    private static final ConcurrentHashMap<Server, List<String>> serverToServices = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<Server>> serviceToServers = new ConcurrentHashMap<>();

    /**
     * 获取提供指定服务的服务器列表
     *
     * @param service 服务名称
     * @return 服务器列表
     */
    public List<Server> getServiceServers(String service) {
        List<Server> servers = serviceToServers.get(service);
        return servers == null ? Collections.emptyList() : new ArrayList<>(servers);
    }

    /**
     * 更新服务器提供的服务
     *
     * @param server 服务器对象
     */
    public synchronized void updateServerService(Server server) {
        List<String> newServices = server.getService();
        List<String> oldServices = serverToServices.get(server);

        if (oldServices != null && oldServices.equals(newServices)) {
            return; // 如果服务没有改变，则不需要更新
        }

        if (oldServices != null) {
            for (String service : oldServices) {
                serviceToServers.get(service).remove(server);
            }
        }
        addServerService(server, newServices);
    }

    /**
     * 添加服务器提供的服务
     *
     * @param server   服务器对象
     * @param services 服务列表
     */
    public synchronized void addServerService(Server server, List<String> services) {
        serverToServices.put(server, services);
        for (String service : services) {
            serviceToServers.computeIfAbsent(service, k -> new ArrayList<>()).add(server);
        }
    }

    /**
     * 移除服务器提供的服务
     *
     * @param server 服务器对象
     */
    public synchronized void removeServerService(Server server) {
        List<String> services = serverToServices.remove(server);

        if (services != null) {
            for (String service : services) {
                serviceToServers.computeIfPresent(service, (k, serverList) -> {
                    serverList.remove(server);
                    return serverList.isEmpty() ? null : serverList;
                });
            }
        }
    }
}
