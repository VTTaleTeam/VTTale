package org.vttale.vttale.api.token.events;

import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.token.Token;
import org.vttale.vttale.api.token.TokenComponent;
import org.vttale.vttale.api.token.behavior.Behavior;

import java.util.Optional;
import java.util.UUID;

/**
 * Event published when a token's data is modified.
 * <p>
 * This event is published for various types of changes: <br/>
 * - Component added, removed, or modified <br/>
 * - Behavior attached or detached <br/>
 * - Name changed <br/>
 * - Type changed <br/>
 * - Position changed <br/>
 * - Tags modified <br/>
 * - Owner changed
 * <p>
 * The updateType field indicates what kind of change occurred.
 * <p>
 * Example:
 * <pre>{@code
 * eventBus.subscribe(TokenUpdatedEvent.class, (event, ctx) -> {
 *     if (event.getUpdateType() == UpdateType.COMPONENT_CHANGED) {
 *         String componentId = event.getComponentId().orElse("unknown");
 *         logger.info("Component {} changed on token {}",
 *                     componentId, event.getToken().getName());
 *     }
 *
 *     if (event.getUpdateType() == UpdateType.BEHAVIOR_ATTACHED) {
 *         String behaviorId = event.getBehaviorId().orElse("unknown");
 *         logger.info("Behavior {} attached to token {}",
 *                     behaviorId, event.getToken().getName());
 *     }
 * });
 * }</pre>
 */
public class TokenUpdatedEvent implements Event {

    /**
     * Types of updates that can occur on a token.
     */
    public enum UpdateType {

        /**
         * The token's name was changed
         */
        NAME_CHANGED,
        /**
         * The token's type was changed
         */
        TYPE_CHANGED,
        /**
         * The token's position was changed
         */
        POSITION_CHANGED,
        /**
         * The token's world was changed
         */
        WORLD_CHANGED,
        /**
         * The token's owner was changed
         */
        OWNER_CHANGED,
        /**
         * A tag was added
         */
        TAGS_ADDED,
        /**
         * A tag was removed
         */
        TAGS_REMOVED,
        /**
         * A component was added to the token
         */
        COMPONENT_ADDED,
        /**
         * A component was removed from the token
         */
        COMPONENT_REMOVED,
        /**
         * A component's data was modified
         */
        COMPONENT_UPDATED,
        /**
         * A behavior was attached to the token
         */
        BEHAVIOR_ATTACHED,
        /**
         * A behavior was detached from the token
         */
        BEHAVIOR_DETACHED,
        /**
         * Multiple properties changed at once
         */
        BULK_UPDATE
    }

    private final Token token;
    private final UpdateType updateType;
    private final String componentId;
    private final Class<? extends TokenComponent> componentClass;
    private final String behaviorId;
    private final Class<? extends Behavior> behaviorClass;
    private final Object oldValue;
    private final Object newValue;

    /**
     * Creates a basic update event.
     *
     * @param token      the updated token
     * @param updateType the type of update
     */
    public TokenUpdatedEvent(Token token, UpdateType updateType) {
        this(token, updateType, null, null, null, null, null, null);
    }

    /**
     * Private constructor for full initialization.
     */
    private TokenUpdatedEvent(Token token, UpdateType updateType,
                              String componentId, Class<? extends TokenComponent> componentClass,
                              String behaviorId, Class<? extends Behavior> behaviorClass,
                              Object oldValue, Object newValue) {
        this.token = token;
        this.updateType = updateType;
        this.componentId = componentId;
        this.componentClass = componentClass;
        this.behaviorId = behaviorId;
        this.behaviorClass = behaviorClass;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a component-related update event.
     *
     * @param token          the updated token
     * @param updateType     the type of update (should be COMPONENT_*)
     * @param componentId    the component's ID
     * @param componentClass the component's class
     * @return the event
     */
    public static TokenUpdatedEvent forComponent(Token token, UpdateType updateType,
                                                 String componentId, Class<? extends TokenComponent> componentClass) {
        return new TokenUpdatedEvent(token, updateType, componentId, componentClass, null, null, null, null);
    }

    /**
     * Creates a component-related update event with old and new values.
     *
     * @param token          the updated token
     * @param updateType     the type of update (should be COMPONENT_*)
     * @param componentId    the component's ID
     * @param componentClass the component's class
     * @param oldValue       the previous value
     * @param newValue       the new value
     * @return the event
     */
    public static TokenUpdatedEvent forComponent(Token token, UpdateType updateType,
                                                 String componentId, Class<? extends TokenComponent> componentClass,
                                                 Object oldValue, Object newValue) {
        return new TokenUpdatedEvent(token, updateType, componentId, componentClass, null, null, oldValue, newValue);
    }

    /**
     * Creates a behavior-related update event.
     *
     * @param token         the updated token
     * @param updateType    the type of update (should be BEHAVIOR_*)
     * @param behaviorId    the behavior's ID
     * @param behaviorClass the behavior's class
     * @return the event
     */
    public static TokenUpdatedEvent forBehavior(Token token, UpdateType updateType,
                                                String behaviorId, Class<? extends Behavior> behaviorClass) {
        return new TokenUpdatedEvent(token, updateType, null, null, behaviorId, behaviorClass, null, null);
    }

    /**
     * Returns the updated token.
     *
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * Returns the token's UUID.
     *
     * @return the token ID
     */
    public UUID getTokenId() {
        return token.getId();
    }

    /**
     * Returns the type of update that occurred.
     *
     * @return the update type
     */
    public UpdateType getUpdateType() {
        return updateType;
    }

    /**
     * Returns the component ID if this is a component-related update.
     *
     * @return an Optional containing the component ID
     */
    public Optional<String> getComponentId() {
        return Optional.ofNullable(componentId);
    }

    /**
     * Returns the component class if this is a component-related update.
     *
     * @return an Optional containing the component class
     */
    public Optional<Class<? extends TokenComponent>> getComponentClass() {
        return Optional.ofNullable(componentClass);
    }

    /**
     * Returns the behavior ID if this is a behavior-related update.
     *
     * @return an Optional containing the behavior ID
     */
    public Optional<String> getBehaviorId() {
        return Optional.ofNullable(behaviorId);
    }

    /**
     * Returns the behavior class if this is a behavior-related update.
     *
     * @return an Optional containing the behavior class
     */
    public Optional<Class<? extends Behavior>> getBehaviorClass() {
        return Optional.ofNullable(behaviorClass);
    }

    /**
     * Returns the old value if available.
     *
     * @return an Optional containing the old value
     */
    public Optional<Object> getOldValue() {
        return Optional.ofNullable(oldValue);
    }

    /**
     * Returns the new value if available.
     *
     * @return an Optional containing the new value
     */
    public Optional<Object> getNewValue() {
        return Optional.ofNullable(newValue);
    }

    /**
     * Checks if this is a component-related update.
     *
     * @return true if this update involves a component
     */
    public boolean isComponentUpdate() {
        return updateType == UpdateType.COMPONENT_ADDED ||
                updateType == UpdateType.COMPONENT_REMOVED ||
                updateType == UpdateType.COMPONENT_UPDATED;
    }

    /**
     * Checks if this is a behavior-related update.
     *
     * @return true if this update involves a behavior
     */
    public boolean isBehaviorUpdate() {
        return updateType == UpdateType.BEHAVIOR_ATTACHED ||
                updateType == UpdateType.BEHAVIOR_DETACHED;
    }

    /**
     * Provides string representation of token update event
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TokenUpdatedEvent{");
        sb.append("token=").append(token.getName());
        sb.append(", type=").append(updateType);
        if (componentId != null) {
            sb.append(", component=").append(componentId);
        }
        if (behaviorId != null) {
            sb.append(", behavior=").append(behaviorId);
        }
        sb.append("}");
        return sb.toString();
    }
}