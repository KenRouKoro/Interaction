package com.foxapplication.mc.interaction.base.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 事件总线，用于订阅和发布事件
 */
public class EventBus {
    /**
     * 事件处理器映射表，用于存储每个事件类型对应的事件处理器列表。
     * 使用线程安全的 ConcurrentHashMap 实现。
     * 键为事件类型的 Class 对象，值为 CopyOnWriteArrayList 类型的事件处理器列表。
     */
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventHandler<?>>> eventHandlers = new ConcurrentHashMap<>();

    /**
     * 事件处理线程池，用于执行事件处理任务。
     * 使用单线程的 ExecutorService 实现。
     */
    private static final ExecutorService eventService = Executors.newSingleThreadExecutor();


    /**
     * 订阅指定类型的事件，并指定回调函数
     *
     * @param eventType 事件类型
     * @param callback  回调函数
     * @param <T>       事件类型
     */
    public static <T> void subscribe(Class<T> eventType, EventCallback<T> callback) {
        if (eventType == null || callback == null) {
            return;
        }
        CopyOnWriteArrayList<EventHandler<?>> handlers = eventHandlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        handlers.add(new EventHandler<>(eventType, callback));
    }

    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    public static void push(Object event) {
        eventService.execute(() -> {
            Class<?> eventType = event.getClass();
            CopyOnWriteArrayList<EventHandler<?>> handlers = eventHandlers.get(eventType);
            if (handlers != null) {
                handlers.forEach(handler -> handler.invoke(event));
            }
        });
    }

    /**
     * 事件回调接口
     * @param <T> 事件类型
     */
    public interface EventCallback<T> {

        /**
         * 当事件发生时调用
         * @param event 事件对象
         */
        void onEvent(T event);
    }


    /**
     * 事件处理器类
     * @param <T> 事件类型
     */
    private static class EventHandler<T> {
        /**
         * 事件类型
         */
        private final Class<T> eventType;
        /**
         * 事件回调
         */
        private final EventCallback<T> callback;

        /**
         * 构造方法
         * @param eventType 事件类型
         * @param callback 事件回调
         */
        public EventHandler(Class<T> eventType, EventCallback<T> callback) {
            this.eventType = eventType;
            this.callback = callback;
        }

        /**
         * 调用事件回调
         * @param event 事件对象
         */
        @SuppressWarnings("unchecked")
        public void invoke(Object event) {
            if (eventType.isInstance(event)) {
                callback.onEvent((T) event);
            }
        }
    }

}
