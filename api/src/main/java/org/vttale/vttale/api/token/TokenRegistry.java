package org.vttale.vttale.api.token;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Central registry for managing tokens in the VTTale system.
 * <p>
 * The TokenRegistry is the single source of truth for all token data.
 * It handles token lifecycle (create, read, update, delete) and provides
 * powerful query capabilities for game systems.
 * <p>
 * Access via: VTTale.getKernel().getService(TokenRegistry.class)
 * <p>
 * Events:
 * The registry publishes events via the EventBus for all token changes:<br />
 * - TokenCreatedEvent: when a new token is created<br />
 * - TokenUpdatedEvent: when a token's data changes<br />
 * - TokenRemovedEvent: when a token is removed<br />
 * - TokenBoundEvent: when a token is bound/unbound from an entity
 */
public interface TokenRegistry {

    // ==================== Token Lifecycle ====================

    /**
     * Creates a new token with the given name and type.
     * <p>
     * This will publish a TokenCreatedEvent, allowing game systems
     * to add their components to the new token.
     *
     * @param name the token's display name
     * @param type the token type (CoreTokenType or custom)
     * @return the newly created token
     */
    Token create(String name, TokenType type);

    /**
     * Creates a new token with additional initial configuration.
     *
     * @param name    the token's display name
     * @param type    the token type (CoreTokenType or custom)
     * @param ownerId the UUID of the owning player, or null
     * @return the newly created token
     */
    Token create(String name, TokenType type, UUID ownerId);

    /**
     * Retrieves a token by its UUID.
     *
     * @param id the token's UUID
     * @return an Optional containing the token, or empty if not found
     */
    Optional<Token> get(UUID id);

    /**
     * Removes a token from the registry.
     * <p>
     * This will publish a TokenRemovedEvent and unbind the token
     * from any associated entity.
     *
     * @param id the token's UUID
     * @return true if the token was removed, false if it didn't exist
     */
    boolean remove(UUID id);

    /**
     * Removes a token from the registry.
     *
     * @param token the token to remove
     * @return true if the token was removed
     */
    default boolean remove(Token token) {
        return remove(token.getId());
    }

    /**
     * Checks if a token with the given ID exists.
     *
     * @param id the token's UUID
     * @return true if the token exists
     */
    boolean exists(UUID id);

    /**
     * Returns the total number of tokens in the registry.
     *
     * @return the token count
     */
    int count();

    // ==================== Query Methods ====================

    /**
     * Returns all tokens in the registry.
     *
     * @return an unmodifiable collection of all tokens
     */
    Collection<Token> getAll();

    /**
     * Returns all tokens of the specified type.
     * Works with CoreTokenType or any custom TokenType.
     *
     * @param type the token type to filter by
     * @return a collection of matching tokens
     */
    Collection<Token> getByType(TokenType type);

    /**
     * Returns all tokens matching the specified type ID.
     *
     * @param typeId the type ID (e.g., "core:npc", "dnd5e:sidekick")
     * @return a collection of matching tokens
     */
    Collection<Token> getByTypeId(String typeId);

    /**
     * Returns all tokens in the specified world.
     *
     * @param worldId the world's UUID
     * @return a collection of tokens in that world
     */
    Collection<Token> getByWorld(UUID worldId);

    /**
     * Returns all tokens owned by the specified player.
     *
     * @param ownerId the owner's UUID
     * @return a collection of tokens owned by that player
     */
    Collection<Token> getByOwner(UUID ownerId);

    /**
     * Returns all tokens that have the specified component type.
     *
     * @param componentType the component class to filter by
     * @param <T>           the component type
     * @return a collection of tokens with that component
     */
    <T extends TokenComponent> Collection<Token> getByComponent(Class<T> componentType);

    /**
     * Returns all tokens that have the specified tag.
     *
     * @param tag the tag to filter by
     * @return a collection of tokens with that tag
     */
    Collection<Token> getByTag(String tag);

    /**
     * Returns all tokens matching the given predicate.
     *
     * @param predicate the filter predicate
     * @return a collection of matching tokens
     */
    Collection<Token> filter(Predicate<Token> predicate);

    /**
     * Creates a fluent query builder for complex token queries.
     *
     * @return a new TokenQuery builder
     */
    TokenQuery query();

    // ==================== Entity Binding ====================

    /**
     * Binds a token to a Hytale entity.
     * <p>
     * This creates a link between the VTTale token and a Hytale entity,
     * allowing position synchronization and visual representation.
     * <p>
     * Publishes a TokenBoundEvent.
     *
     * @param tokenId  the token's UUID
     * @param entityId the Hytale entity's UUID
     * @return true if the binding was successful
     */
    boolean bindToEntity(UUID tokenId, UUID entityId);

    /**
     * Unbinds a token from its associated Hytale entity.
     * <p>
     * Publishes a TokenBoundEvent with null entityId.
     *
     * @param tokenId the token's UUID
     * @return true if the token was unbound
     */
    boolean unbindFromEntity(UUID tokenId);

    /**
     * Finds a token by its bound entity's UUID.
     *
     * @param entityId the Hytale entity's UUID
     * @return an Optional containing the token, or empty if no token is bound
     */
    Optional<Token> getByEntityId(UUID entityId);

    /**
     * Returns all tokens that are currently bound to entities.
     *
     * @return a collection of bound tokens
     */
    Collection<Token> getBoundTokens();

    /**
     * Returns all tokens that are not bound to any entity.
     *
     * @return a collection of unbound tokens
     */
    Collection<Token> getUnboundTokens();

    // ==================== Bulk Operations ====================

    /**
     * Removes all tokens from the registry.
     * <p>
     * Use with caution! This will publish TokenRemovedEvent for each token.
     */
    void clear();

    /**
     * Removes all tokens matching the given predicate.
     *
     * @param predicate the filter for tokens to remove
     * @return the number of tokens removed
     */
    int removeIf(Predicate<Token> predicate);

    /**
     * Removes all tokens in the specified world.
     *
     * @param worldId the world's UUID
     * @return the number of tokens removed
     */
    int removeByWorld(UUID worldId);
}
