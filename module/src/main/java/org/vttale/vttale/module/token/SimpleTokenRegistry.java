package org.vttale.vttale.module.token;

import org.vttale.vttale.api.Kernel;
import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.events.EventBus;
import org.vttale.vttale.api.events.EventContext;
import org.vttale.vttale.api.token.*;
import org.vttale.vttale.api.token.behavior.Behavior;
import org.vttale.vttale.api.token.events.TokenBoundEvent;
import org.vttale.vttale.api.token.events.TokenCreatedEvent;
import org.vttale.vttale.api.token.events.TokenRemovedEvent;
import org.vttale.vttale.api.token.events.TokenUpdatedEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of the TokenRegistry interface.
 * <p>
 * This implementation is fully thread-safe and publishes events
 * via the EventBus for all token changes.
 * <p>
 * The registry maintains two indices:<br />
 * - tokens: UUID -> Token (primary storage)<br />
 * - entityBindings: Entity UUID -> Token UUID (for entity lookups)
 */
public class SimpleTokenRegistry implements TokenRegistry {

    private final Kernel kernel;
    private final Map<UUID, SimpleToken> tokens;
    private final Map<UUID, UUID> entityBindings; // entityId -> tokenId

    /**
     * Creates a new SimpleTokenRegistry.
     *
     * @param kernel the kernel instance for accessing the EventBus
     */
    public SimpleTokenRegistry(Kernel kernel) {
        this.kernel = Objects.requireNonNull(kernel, "Kernel cannot be null");
        this.tokens = new ConcurrentHashMap<>();
        this.entityBindings = new ConcurrentHashMap<>();
    }

    // ==================== Token Lifecycle ====================

    @Override
    public Token create(String name, TokenType type) {
        return create(name, type, null);
    }

    @Override
    public Token create(String name, TokenType type, UUID ownerId) {
        SimpleToken token = new SimpleToken(name, type, ownerId);
        tokens.put(token.getId(), token);

        // Publish event for listeners (game systems will add their components)
        publishEvent(new TokenCreatedEvent(token));

        return token;
    }

    @Override
    public Optional<Token> get(UUID id) {
        return Optional.ofNullable(tokens.get(id));
    }

    @Override
    public boolean remove(UUID id) {
        SimpleToken token = tokens.get(id);
        if (token == null) {
            return false;
        }

        // Unbind from entity if bound
        token.getBoundEntityId().ifPresent(entityBindings::remove);

        // Publish event before removal
        publishEvent(new TokenRemovedEvent(token));

        // Remove from registry
        tokens.remove(id);
        return true;
    }

    @Override
    public boolean exists(UUID id) {
        return tokens.containsKey(id);
    }

    @Override
    public int count() {
        return tokens.size();
    }

    // ==================== Query Methods ====================

    @Override
    public Collection<Token> getAll() {
        return Collections.unmodifiableCollection(new ArrayList<>(tokens.values()));
    }

    @Override
    public Collection<Token> getByType(TokenType type) {
        String typeId = type.getId();
        return tokens.values().stream()
                .filter(t -> t.getType().getId().equals(typeId))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<Token> getByTypeId(String typeId) {
        return tokens.values().stream()
                .filter(t -> t.getType().getId().equals(typeId))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<Token> getByWorld(UUID worldId) {
        return tokens.values().stream()
                .filter(t -> t.getWorldId().map(id -> id.equals(worldId)).orElse(false))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<Token> getByOwner(UUID ownerId) {
        return tokens.values().stream()
                .filter(t -> t.getOwnerId().map(id -> id.equals(ownerId)).orElse(false))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public <T extends TokenComponent> Collection<Token> getByComponent(Class<T> componentType) {
        return tokens.values().stream()
                .filter(t -> t.hasComponent(componentType))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<Token> getByTag(String tag) {
        return tokens.values().stream()
                .filter(t -> t.hasTag(tag))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<Token> filter(Predicate<Token> predicate) {
        return tokens.values().stream()
                .filter(predicate)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public TokenQuery query() {
        return new SimpleTokenQuery(getAll());
    }

    // ==================== Entity Binding ====================

    @Override
    public boolean bindToEntity(UUID tokenId, UUID entityId) {
        SimpleToken token = tokens.get(tokenId);
        if (token == null) {
            return false;
        }

        // Get previous binding for the event
        UUID previousEntityId = token.getBoundEntityId().orElse(null);

        // Remove old entity binding if exists
        if (previousEntityId != null) {
            entityBindings.remove(previousEntityId);
        }

        // Remove any existing token binding to this entity
        UUID existingTokenId = entityBindings.get(entityId);
        if (existingTokenId != null && !existingTokenId.equals(tokenId)) {
            SimpleToken existingToken = tokens.get(existingTokenId);
            if (existingToken != null) {
                existingToken.setBoundEntityId(null);
                publishEvent(TokenBoundEvent.unbound(existingToken, entityId));
            }
        }

        // Create new binding
        token.setBoundEntityId(entityId);
        entityBindings.put(entityId, tokenId);

        // Publish event
        publishEvent(new TokenBoundEvent(token, entityId, previousEntityId));

        return true;
    }

    @Override
    public boolean unbindFromEntity(UUID tokenId) {
        SimpleToken token = tokens.get(tokenId);
        if (token == null) {
            return false;
        }

        UUID entityId = token.getBoundEntityId().orElse(null);
        if (entityId == null) {
            return false; // Already unbound
        }

        // Remove binding
        token.setBoundEntityId(null);
        entityBindings.remove(entityId);

        // Publish event
        publishEvent(TokenBoundEvent.unbound(token, entityId));

        return true;
    }

    @Override
    public Optional<Token> getByEntityId(UUID entityId) {
        UUID tokenId = entityBindings.get(entityId);
        if (tokenId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(tokenId));
    }

    @Override
    public Collection<Token> getBoundTokens() {
        return tokens.values().stream()
                .filter(Token::isBoundToEntity)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<Token> getUnboundTokens() {
        return tokens.values().stream()
                .filter(t -> !t.isBoundToEntity())
                .collect(Collectors.toUnmodifiableList());
    }

    // ==================== Bulk Operations ====================

    @Override
    public void clear() {
        // Publish removal events for all tokens
        for (SimpleToken token : tokens.values()) {
            publishEvent(new TokenRemovedEvent(token));
        }

        tokens.clear();
        entityBindings.clear();
    }

    @Override
    public int removeIf(Predicate<Token> predicate) {
        List<UUID> toRemove = tokens.values().stream()
                .filter(predicate)
                .map(Token::getId)
                .collect(Collectors.toList());
        toRemove.forEach(this::remove);
        return toRemove.size();
    }

    @Override
    public int removeByWorld(UUID worldId) {
        return removeIf(token -> token.getWorldId().map(id -> id.equals(worldId)).orElse(false));
    }

    // ==================== Internal Methods ====================

    /**
     * Publishes an event via the kernel's EventBus.
     * Uses "KERNEL" as the sender ID for system-generated events.
     *
     * @param event the event to publish
     */
    private void publishEvent(Event event) {
        EventBus eventBus = kernel.getEventBus();
        if (eventBus != null) {
            eventBus.publish(event, new EventContext("KERNEL"));
        }
    }

    /**
     * Notifies the registry that a token was updated externally.
     * This is called by the token itself when modified.
     *
     * @param token      the updated token
     * @param updateType the type of update
     */
    void notifyTokenUpdated(SimpleToken token, TokenUpdatedEvent.UpdateType updateType) {
        publishEvent(new TokenUpdatedEvent(token, updateType));
    }

    /**
     * Notifies the registry that a component was changed.
     *
     * @param token          the token
     * @param updateType     the type of change
     * @param componentId    the component ID
     * @param componentClass the component class
     */
    void notifyComponentChanged(SimpleToken token, TokenUpdatedEvent.UpdateType updateType,
                                String componentId, Class<? extends TokenComponent> componentClass) {
        publishEvent(TokenUpdatedEvent.forComponent(token, updateType, componentId, componentClass));
    }

    /**
     * Notifies the registry that a behavior was changed.
     *
     * @param token         the token
     * @param updateType    the type of change
     * @param behaviorId    the behavior ID
     * @param behaviorClass the behavior class
     */
    void notifyBehaviorChanged(SimpleToken token, TokenUpdatedEvent.UpdateType updateType,
                               String behaviorId, Class<? extends Behavior> behaviorClass) {
        publishEvent(TokenUpdatedEvent.forBehavior(token, updateType, behaviorId, behaviorClass));
    }
}
