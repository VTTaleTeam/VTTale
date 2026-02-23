package org.vttale.vttale.api.token.behavior;

import java.util.Optional;

/**
 * Context for storing behavior-specific state on a token.
 * <p>
 * Each behavior attached to a token gets its own BehaviorContext.
 * This allows behaviors to maintain state without polluting the token
 * or conflicting with other behaviors.
 * <p>
 * The context is a simple key-value store with type-safe accessors.
 * <p>
 * Example usage:
 * <pre>{@code
 * // In onAttach()
 * context.set("charges", 3);
 * context.set("used_this_turn", false);
 *
 * // In onEvent()
 * int charges = context.getInt("charges", 0);
 * if (charges > 0) {
 *     context.set("charges", charges - 1);
 * }
 * }</pre>
 */
public interface BehaviorContext {

    /**
     * Sets a value in the context.
     *
     * @param key   the key
     * @param value the value (can be null)
     */
    void set(String key, Object value);

    /**
     * Gets a value from the context.
     *
     * @param key the key
     * @return an Optional containing the value, or empty if not set
     */
    Optional<Object> get(String key);

    /**
     * Gets a value with a default if not present.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return the value or default
     */
    @SuppressWarnings("unchecked")
    default <T> T get(String key, T defaultValue) {
        return (T) get(key).orElse(defaultValue);
    }

    /**
     * Gets a String value.
     *
     * @param key          the key
     * @param defaultValue the default
     * @return the value or default
     */
    default String getString(String key, String defaultValue) {
        return get(key).map(Object::toString).orElse(defaultValue);
    }

    /**
     * Gets an int value.
     *
     * @param key          the key
     * @param defaultValue the default
     * @return the value or default
     */
    default int getInt(String key, int defaultValue) {
        return get(key)
                .filter(v -> v instanceof Number)
                .map(v -> ((Number) v).intValue())
                .orElse(defaultValue);
    }

    /**
     * Gets a long value.
     *
     * @param key          the key
     * @param defaultValue the default
     * @return the value or default
     */
    default long getLong(String key, long defaultValue) {
        return get(key)
                .filter(v -> v instanceof Number)
                .map(v -> ((Number) v).longValue())
                .orElse(defaultValue);
    }

    /**
     * Gets a double value.
     *
     * @param key          the key
     * @param defaultValue the default
     * @return the value or default
     */
    default double getDouble(String key, double defaultValue) {
        return get(key)
                .filter(v -> v instanceof Number)
                .map(v -> ((Number) v).doubleValue())
                .orElse(defaultValue);
    }

    /**
     * Gets a boolean value.
     *
     * @param key          the key
     * @param defaultValue the default
     * @return the value or default
     */
    default boolean getBoolean(String key, boolean defaultValue) {
        return get(key)
                .filter(v -> v instanceof Boolean)
                .map(v -> (Boolean) v)
                .orElse(defaultValue);
    }

    /**
     * Checks if a key exists in the context.
     *
     * @param key the key
     * @return true if the key exists
     */
    boolean has(String key);

    /**
     * Removes a key from the context.
     *
     * @param key the key
     * @return true if the key was removed
     */
    boolean remove(String key);

    /**
     * Clears all values from the context.
     */
    void clear();

    /**
     * Increments an integer value and returns the new value.
     * If the key doesn't exist, initializes to 1.
     *
     * @param key the key
     * @return the new value
     */
    default int increment(String key) {
        int value = getInt(key, 0) + 1;
        set(key, value);
        return value;
    }

    /**
     * Decrements an integer value and returns the new value.
     * If the key doesn't exist, initializes to -1.
     *
     * @param key the key
     * @return the new value
     */
    default int decrement(String key) {
        int value = getInt(key, 0) - 1;
        set(key, value);
        return value;
    }
}
