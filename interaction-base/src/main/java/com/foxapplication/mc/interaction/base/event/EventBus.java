package com.foxapplication.mc.interaction.base.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 事件总线，用于订阅和发布事件
 */
public class EventBus {
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventHandler<?>>> eventHandlers = new ConcurrentHashMap<>();
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
     *
     * @param <T> 事件类型
     */
    public interface EventCallback<T> {
        void onEvent(T event);
    }

    private static class EventHandler<T> {
        private final Class<T> eventType;
        private final EventCallback<T> callback;

        public EventHandler(Class<T> eventType, EventCallback<T> callback) {
            this.eventType = eventType;
            this.callback = callback;
        }

        @SuppressWarnings("unchecked")
        public void invoke(Object event) {
            if (eventType.isInstance(event)) {
                callback.onEvent((T) event);
            }
        }
    }
}
