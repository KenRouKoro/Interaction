package cn.korostudio.interaction.base.event;

import cn.hutool.core.util.StrUtil;
import cn.korostudio.interaction.base.data.BaseMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceMessageBus {
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceMessageCallback>> callbackMap = new ConcurrentHashMap<>();

    public static void subscribe(String service,ServiceMessageCallback callback){
        if (StrUtil.isBlankIfStr(service)||callback==null){
            return;
        }
        CopyOnWriteArrayList<ServiceMessageCallback> callbacks = callbackMap.get(service);
        if (callbacks==null){
            callbacks = new CopyOnWriteArrayList<>();
            callbackMap.put(service,callbacks);
        }
        callbacks.add(callback);
    }
    public static void push(BaseMessage message){
        if (message==null||StrUtil.isBlankIfStr(message.getService())){
            return;
        }
        CopyOnWriteArrayList<ServiceMessageCallback> callbacks = callbackMap.get(message.getService());
        if (callbacks==null){
            return;
        }
        callbacks.forEach(callback -> {
            callback.callback(message);
        });
    }

    public interface ServiceMessageCallback {
        void callback(BaseMessage message);
    }

}
