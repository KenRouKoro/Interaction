package cn.korostudio.interaction.base.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<HH>> events = new ConcurrentHashMap<>();
    public static <T> void subscribe (Class<T> event,EventCallBack<T> callBack){
        if (event==null||callBack==null){
            return;
        }
        CopyOnWriteArrayList<HH> eventList = events.get(event);
        if (eventList==null){
            eventList = new CopyOnWriteArrayList<>();
            events.put(event,eventList);
        }
        eventList.add(new HH(event,callBack));
    }

    public static void push(Object event){
        Class<?> clazz = event.getClass();
        CopyOnWriteArrayList<HH> eventCallBacks;
        eventCallBacks = events.get(clazz);
        if (eventCallBacks==null){
            return;
        }
        eventCallBacks.forEach(callback->{
            callback.l.callback(event);
        });
    }

    public interface EventCallBack <T>{
        void callback(T event);
    }
    static class HH {
        protected Class<?> t;
        protected EventCallBack l;

        public HH(Class<?> type, EventCallBack callBack) {
            this.t = type;
            this.l = callBack;
        }
    }

}
