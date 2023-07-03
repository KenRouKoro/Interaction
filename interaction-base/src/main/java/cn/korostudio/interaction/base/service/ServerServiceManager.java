package cn.korostudio.interaction.base.service;

import cn.korostudio.interaction.base.data.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerServiceManager {
    private static final ConcurrentHashMap<Server, List<String>> serverToServices = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<Server>> serviceToServers = new ConcurrentHashMap<>();

    public List<Server> getServiceServers(String service) {
        return serviceToServers.get(service);
    }

    public void updateServerService(Server server) {
        List<String> oldServices = serverToServices.get(server);
        if (oldServices != null) {
            for (String service : oldServices) {
                serviceToServers.get(service).remove(server);
            }
        }
        addServerService(server, server.getService());
    }

    public void addServerService(Server server, List<String> services) {
        serverToServices.put(server, services);
        for (String service : services) {
            serviceToServers.computeIfAbsent(service, k -> new ArrayList<>()).add(server);
        }
    }

    public void removeServerService(Server server) {
        List<String> services = serverToServices.remove(server);

        if (services != null) {
            for (String service : services) {
                List<Server> servers = serviceToServers.get(service);
                if (servers != null) {
                    servers.remove(server);
                }
            }
        }
    }
}