package org.vttale.vttale.api.token;

import org.vttale.vttale.api.token.behavior.Behavior;
import org.vttale.vttale.api.token.behavior.BehaviorContext;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a token in the VTTale system.
 * <p>
 * A token is the universal abstraction for any entity on the virtual tabletop:
 * player characters, NPCs, monsters, objects, etc.
 * <p>
 * Tokens support three extension mechanisms:<br />
 * - Components: Data/variables (e.g., stats, health)<br />
 * - Tags: Categories for filtering (e.g., "undead", "boss")<br />
 * - Behaviors: Logic/reactions (e.g., legendary actions, pack tactics)
 */
public interface Token {

    // ==================== Identity ====================

    /**
     * Returns the unique identifier of this token.
     *
     * @return the token's UUID
     */
    UUID getId();

    /**
     * Returns the display name of this token.
     *
     * @return the token's name
     */
    String getName();

    /**
     * Sets the display name of this token.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Returns the type of this token.
     *
     * @return the token type
     */
    TokenType getType();

    /**
     * Sets the type of this token.
     *
     * @param type the new type
     */
    void setType(TokenType type);

    /**
     * Checks if this token's type matches the given type.
     * Convenience method that compares type IDs.
     *
     * @param type the type to check
     * @return true if types match
     */
    default boolean isType(TokenType type) {
        return getType().matches(type);
    }

    /**
     * Checks if this token's type matches the given type ID.
     *
     * @param typeId the type ID to check (e.g., "core:player_character")
     * @return true if type ID matches
     */
    default boolean isType(String typeId) {
        return getType().matches(typeId);
    }

    // ==================== Components ====================

    /**
     * Gets a component of the specified type.
     *
     * @param type the component class
     * @param <T>  the component type
     * @return an Optional containing the component, or empty if not present
     */
    <T extends TokenComponent> Optional<T> getComponent(Class<T> type);

    /**
     * Sets a component on this token. If a component of the same type exists,
     * it will be replaced.
     *
     * @param component the component to set
     * @param <T>       the component type
     */
    <T extends TokenComponent> void setComponent(T component);

    /**
     * Removes a component from this token.
     *
     * @param type the component class to remove
     * @param <T>  the component type
     * @return true if the component was removed, false if it wasn't present
     */
    <T extends TokenComponent> boolean removeComponent(Class<T> type);

    /**
     * Checks if this token has a component of the specified type.
     *
     * @param type the component class
     * @return true if present
     */
    boolean hasComponent(Class<? extends TokenComponent> type);

    /**
     * Returns all components attached to this token.
     *
     * @return unmodifiable collection of components
     */
    Collection<TokenComponent> getAllComponents();

    /**
     * Returns all component types attached to this token.
     *
     * @return set of component classes
     */
    Set<Class<? extends TokenComponent>> getComponentTypes();

    // ==================== Behaviors ====================

    /**
     * Attaches a behavior to this token.
     * <p>
     * The behavior's onAttach() method will be called.
     * If a behavior with the same ID is already attached, it will be replaced.
     *
     * @param behavior the behavior to attach
     */
    void attachBehavior(Behavior behavior);

    /**
     * Detaches a behavior from this token.
     * <p>
     * The behavior's onDetach() method will be called.
     *
     * @param behaviorId the behavior ID to detach
     * @return true if the behavior was detached
     */
    boolean detachBehavior(String behaviorId);

    /**
     * Detaches a behavior from this token.
     *
     * @param behavior the behavior to detach
     * @return true if detached
     */
    default boolean detachBehavior(Behavior behavior) {
        return detachBehavior(behavior.getId());
    }

    /**
     * Gets a behavior by its ID.
     *
     * @param behaviorId the behavior ID
     * @return an Optional containing the behavior
     */
    Optional<Behavior> getBehavior(String behaviorId);

    /**
     * Gets a behavior by its class.
     *
     * @param behaviorClass the behavior class
     * @param <T>           the behavior type
     * @return an Optional containing the behavior
     */
    <T extends Behavior> Optional<T> getBehavior(Class<T> behaviorClass);

    /**
     * Checks if this token has a specific behavior.
     *
     * @param behaviorId the behavior ID
     * @return true if the behavior is attached
     */
    boolean hasBehavior(String behaviorId);

    /**
     * Checks if this token has a behavior of the specified class.
     *
     * @param behaviorClass the behavior class
     * @return true if attached
     */
    boolean hasBehavior(Class<? extends Behavior> behaviorClass);

    /**
     * Returns all behaviors attached to this token.
     *
     * @return unmodifiable collection of behaviors
     */
    Collection<Behavior> getAllBehaviors();

    /**
     * Returns the IDs of all behaviors attached to this token.
     *
     * @return set of behavior IDs
     */
    Set<String> getBehaviorIds();

    /**
     * Gets the context for a specific behavior.
     * Returns empty if the behavior is not attached.
     *
     * @param behaviorId the behavior ID
     * @return an Optional containing the context
     */
    Optional<BehaviorContext> getBehaviorContext(String behaviorId);

    // ==================== Tags ====================

    /**
     * Returns all tags attached to this token.
     *
     * @return unmodifiable set of tags
     */
    Set<String> getTags();

    /**
     * Adds a tag to this token.
     *
     * @param tag the tag to add
     */
    void addTag(String tag);

    /**
     * Removes a tag from this token.
     *
     * @param tag the tag to remove
     * @return true if removed
     */
    boolean removeTag(String tag);

    /**
     * Checks if this token has a specific tag.
     *
     * @param tag the tag to check
     * @return true if present
     */
    boolean hasTag(String tag);

    // ==================== Position & World ====================

    /**
     * Returns the current position of this token, if it has one.
     * A token may not have a position if it's not placed on a map.
     *
     * @return an Optional containing the position
     */
    Optional<TokenPosition> getPosition();

    /**
     * Sets the position of this token.
     *
     * @param position the new position, or null to clear
     */
    void setPosition(TokenPosition position);

    /**
     * Returns the UUID of the world this token is in, if any.
     *
     * @return an Optional containing the world UUID
     */
    Optional<UUID> getWorldId();

    /**
     * Sets the world this token is in.
     *
     * @param worldId the world UUID, or null to clear
     */
    void setWorldId(UUID worldId);

    // ==================== Entity Binding ====================

    /**
     * Returns the UUID of the Hytale entity this token is bound to, if any.
     * A token can exist without being bound to an entity.
     *
     * @return an Optional containing the entity UUID
     */
    Optional<UUID> getBoundEntityId();

    /**
     * Checks if this token is bound to a Hytale entity.
     *
     * @return true if bound
     */
    default boolean isBoundToEntity() {
        return getBoundEntityId().isPresent();
    }

    // ==================== Ownership ====================

    /**
     * Returns the UUID of the player who owns this token.
     *
     * @return an Optional containing the owner's UUID
     */
    Optional<UUID> getOwnerId();

    /**
     * Sets the owner of this token.
     *
     * @param ownerId the owner's UUID, or null to clear
     */
    void setOwnerId(UUID ownerId);

    // ==================== Timestamps ====================

    /**
     * Returns the timestamp when this token was created.
     *
     * @return creation timestamp in milliseconds
     */
    long getCreatedAt();

    /**
     * Returns the timestamp when this token was last modified.
     *
     * @return last modified timestamp in milliseconds
     */
    long getLastModifiedAt();
}
