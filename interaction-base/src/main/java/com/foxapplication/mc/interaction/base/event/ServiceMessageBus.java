package com.foxapplication.mc.interaction.base.event;

import com.foxapplication.embed.hutool.core.util.StrUtil;
import com.foxapplication.embed.hutool.log.Log;
import com.foxapplication.embed.hutool.log.LogFactory;
import com.foxapplication.mc.interaction.base.data.BaseMessage;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务消息总线
 */
public class ServiceMessageBus {
    private static final ConcurrentHashMap<String, List<ServiceMessageCallback>> callbackMap = new ConcurrentHashMap<>();
    private static final Log log = LogFactory.get();

    /**
     * 订阅服务消息
     *
     * @param service  服务名称
     * @param callback 回调函数
     */
    public static void subscribe(String service, ServiceMessageCallback callback) {
        if (StrUtil.isBlank(service) || callback == null) {
            return;
        }
        List<ServiceMessageCallback> callbacks = callbackMap.computeIfAbsent(service, k -> new CopyOnWriteArrayList<>());
        callbacks.add(callback);
    }
    /**
     * 取消订阅服务消息
     *
     * @param service  服务名称
     * @param callback 回调函数
     */
    public static void unsubscribe(String service, ServiceMessageCallback callback) {
        if (StrUtil.isBlank(service) || callback == null) {
            return;
        }
        List<ServiceMessageCallback> callbacks = callbackMap.get(service);
        if (callbacks != null) {
            callbacks.remove(callback);
        }
    }

    /**
     * 取消订阅服务消息
     *
     * @param service 服务名称
     */
    public static void unsubscribe(String service) {
        callbackMap.remove(service);
    }

    /**
     * 推送服务消息
     *
     * @param message 消息对象
     */
    public static void push(BaseMessage message) {
        if (message == null || StrUtil.isBlank(message.getService())) {
            return;
        }
        List<ServiceMessageCallback> callbacks = callbackMap.get(message.getService());
        if (callbacks != null) {
            for (ServiceMessageCallback callback : callbacks) {
                try {
                    callback.callback(message);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    /**
     * 服务消息回调接口
     */
    public interface ServiceMessageCallback {
        /**
         * 回调函数
         *
         * @param message 消息对象
         */
        void callback(BaseMessage message);
    }
}
