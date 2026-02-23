package org.vttale.vttale.kernel.events;

import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.events.EventBus;
import org.vttale.vttale.api.events.EventContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class SimpleEventBus implements EventBus {

    private final Map<Class<?>, List<BiConsumer<?, EventContext>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Publishes event to subscribers; handles errors
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event, EventContext context) {
        List<BiConsumer<?, EventContext>> eventListeners = subscribers.get(event.getClass());
        if (eventListeners != null) {
            for (BiConsumer<?, EventContext> listener : eventListeners) {
                try {
                    ((BiConsumer<T, EventContext>) listener).accept(event, context);
                } catch (Exception e) {
                    // TODO: Proper error handling
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, BiConsumer<T, EventContext> listener) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

}
