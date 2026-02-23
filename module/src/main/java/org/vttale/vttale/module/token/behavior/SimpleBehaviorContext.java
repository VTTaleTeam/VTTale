package org.vttale.vttale.module.token.behavior;

import org.vttale.vttale.api.token.behavior.BehaviorContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of BehaviorContext.
 * <p>
 * Thread-safe key-value store for behavior state.
 */
public class SimpleBehaviorContext implements BehaviorContext {

    private final Map<String, Object> data = new ConcurrentHashMap<>();

    @Override
    public void set(String key, Object value) {
        if (key == null) return;
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    public boolean remove(String key) {
        return data.remove(key) != null;
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public String toString() {
        return "BehaviorContext" + data;
    }
}
