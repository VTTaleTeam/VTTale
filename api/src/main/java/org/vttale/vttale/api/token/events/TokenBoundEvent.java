package org.vttale.vttale.api.token.events;

import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.token.Token;

import java.util.Optional;
import java.util.UUID;

/**
 * Event published when a token is bound to or unbound from a Hytale entity.
 * <p>
 * This event is used by the platform adapter to synchronize token data
 * with the Hytale entity system.
 * <p>
 * Binding scenarios: <p>
 * - Token spawned: entityId is set, previousEntityId is null <p>
 * - Token despawned: entityId is null, previousEntityId is set <p>
 * - Token rebound: both entityId and previousEntityId are set
 * <p>
 * Example:
 * <pre>{@code
 * eventBus.subscribe(TokenBoundEvent.class, (event, ctx) -> {
 *     if (event.isBound()) {
 *         // Token was bound to an entity - sync data
 *         syncTokenToEntity(event.getToken(), event.getEntityId().get());
 *     } else {
 *         // Token was unbound - despawn the entity
 *         despawnEntity(event.getPreviousEntityId().get());
 *     }
 * });
 * }</pre>
 */
public class TokenBoundEvent implements Event {

    private final Token token;
    private final UUID entityId;
    private final UUID previousEntityId;

    /**
     * Creates a token bound event.
     *
     * @param token            the token
     * @param entityId         the new entity UUID (null if unbinding)
     * @param previousEntityId the previous entity UUID (null if first binding)
     */
    public TokenBoundEvent(Token token, UUID entityId, UUID previousEntityId) {
        this.token = token;
        this.entityId = entityId;
        this.previousEntityId = previousEntityId;
    }

    /**
     * Creates a binding event (token bound to a new entity).
     *
     * @param token    the token
     * @param entityId the entity UUID
     * @return the event
     */
    public static TokenBoundEvent bound(Token token, UUID entityId) {
        return new TokenBoundEvent(token, entityId, token.getBoundEntityId().orElse(null));
    }

    /**
     * Creates an unbinding event (token unbound from entity).
     *
     * @param token            the token
     * @param previousEntityId the entity UUID it was bound to
     * @return the event
     */
    public static TokenBoundEvent unbound(Token token, UUID previousEntityId) {
        return new TokenBoundEvent(token, null, previousEntityId);
    }

    /**
     * Returns the token.
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
     * Returns the entity UUID this token is now bound to.
     *
     * @return an Optional containing the entity UUID, empty if unbound
     */
    public Optional<UUID> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    /**
     * Returns the entity UUID this token was previously bound to.
     *
     * @return an Optional containing the previous entity UUID
     */
    public Optional<UUID> getPreviousEntityId() {
        return Optional.ofNullable(previousEntityId);
    }

    /**
     * Checks if the token is now bound to an entity.
     *
     * @return true if bound
     */
    public boolean isBound() {
        return entityId != null;
    }

    /**
     * Checks if the token was unbound from an entity.
     *
     * @return true if unbound
     */
    public boolean isUnbound() {
        return entityId == null && previousEntityId != null;
    }

    /**
     * Checks if the token was rebound to a different entity.
     *
     * @return true if rebound
     */
    public boolean isRebound() {
        return entityId != null && previousEntityId != null;
    }

    /**
     * Checks if this is the first time the token is bound to any entity.
     *
     * @return true if first binding
     */
    public boolean isFirstBinding() {
        return entityId != null && previousEntityId == null;
    }

    /**
     * Provides token binding state for debugging
     */
    @Override
    public String toString() {
        if (isUnbound()) {
            return "TokenBoundEvent{token=" + token.getName() + ", UNBOUND from " + previousEntityId + "}";
        } else if (isRebound()) {
            return "TokenBoundEvent{token=" + token.getName() + ", REBOUND from " + previousEntityId + " to " + entityId + "}";
        } else {
            return "TokenBoundEvent{token=" + token.getName() + ", BOUND to " + entityId + "}";
        }
    }
}
