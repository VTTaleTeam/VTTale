package org.vttale.vttale.module.token;

import org.vttale.vttale.api.token.Token;
import org.vttale.vttale.api.token.TokenComponent;
import org.vttale.vttale.api.token.TokenPosition;
import org.vttale.vttale.api.token.TokenType;
import org.vttale.vttale.api.token.behavior.Behavior;
import org.vttale.vttale.api.token.behavior.BehaviorContext;
import org.vttale.vttale.module.token.behavior.SimpleBehaviorContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the Token interface.
 * <p>
 * This class is thread-safe and uses concurrent collections for all mutable state.
 * <p>
 * Note: This class should only be instantiated by the TokenRegistry.
 * External code should interact with tokens through the Token interface.
 */
public class SimpleToken implements Token {

    private final UUID id;
    private volatile String name;
    private volatile TokenType type;
    private volatile TokenPosition position;
    private volatile UUID worldId;
    private volatile UUID boundEntityId;
    private volatile UUID ownerId;
    private final long createdAt;
    private volatile long lastModifiedAt;

    // Components: Class -> Component
    private final Map<Class<? extends TokenComponent>, TokenComponent> components;

    // Tags
    private final Set<String> tags;

    // Behaviors: ID -> Behavior
    private final Map<String, Behavior> behaviors;

    // Behavior contexts: ID -> Context
    private final Map<String, BehaviorContext> behaviorContexts;

    /**
     * Creates a new SimpleToken with a CoreTokenType.
     *
     * @param name the display name
     * @param type the token type
     */
    public SimpleToken(String name, TokenType type) {
        this(UUID.randomUUID(), name, type, null);
    }

    /**
     * Creates a new SimpleToken with an owner.
     *
     * @param name    the display name
     * @param type    the token type
     * @param ownerId the owner's UUID
     */
    public SimpleToken(String name, TokenType type, UUID ownerId) {
        this(UUID.randomUUID(), name, type, ownerId);
    }

    /**
     * Creates a new SimpleToken with a specific UUID.
     *
     * @param id      the token's UUID
     * @param name    the display name
     * @param type    the token type
     * @param ownerId the owner's UUID
     */
    public SimpleToken(UUID id, String name, TokenType type, UUID ownerId) {
        this.id = Objects.requireNonNull(id, "Token ID cannot be null");
        this.name = Objects.requireNonNull(name, "Token name cannot be null");
        this.type = Objects.requireNonNull(type, "Token type cannot be null");
        this.ownerId = ownerId;
        this.createdAt = System.currentTimeMillis();
        this.lastModifiedAt = this.createdAt;
        this.components = new ConcurrentHashMap<>();
        this.tags = ConcurrentHashMap.newKeySet();
        this.behaviors = new ConcurrentHashMap<>();
        this.behaviorContexts = new ConcurrentHashMap<>();
    }

    // ==================== Identity ====================

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Token name cannot be null");
        markModified();
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public void setType(TokenType type) {
        this.type = Objects.requireNonNull(type, "Token type cannot be null");
        markModified();
    }

    // ==================== Components ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TokenComponent> Optional<T> getComponent(Class<T> type) {
        return Optional.ofNullable((T) components.get(type));
    }

    @Override
    public <T extends TokenComponent> void setComponent(T component) {
        Objects.requireNonNull(component, "Component cannot be null");
        components.put(component.getClass(), component);
        markModified();
    }

    @Override
    public <T extends TokenComponent> boolean removeComponent(Class<T> type) {
        boolean removed = components.remove(type) != null;
        if (removed) {
            markModified();
        }
        return removed;
    }

    @Override
    public boolean hasComponent(Class<? extends TokenComponent> type) {
        return components.containsKey(type);
    }

    @Override
    public Collection<TokenComponent> getAllComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    @Override
    public Set<Class<? extends TokenComponent>> getComponentTypes() {
        return Collections.unmodifiableSet(components.keySet());
    }

    // ==================== Behaviors ====================

    @Override
    public void attachBehavior(Behavior behavior) {
        Objects.requireNonNull(behavior, "Behavior cannot be null");

        String id = behavior.getId();

        // Detach existing behavior with same ID
        if (behaviors.containsKey(id)) {
            detachBehavior(id);
        }

        // Create context for this behavior
        BehaviorContext context = new SimpleBehaviorContext();

        // Store behavior and context
        behaviors.put(id, behavior);
        behaviorContexts.put(id, context);

        // Call onAttach
        behavior.onAttach(this, context);

        markModified();
    }

    @Override
    public boolean detachBehavior(String behaviorId) {
        Behavior behavior = behaviors.get(behaviorId);
        if (behavior == null) {
            return false;
        }

        // Get context before removing
        BehaviorContext context = behaviorContexts.get(behaviorId);

        // Call onDetach
        if (context != null) {
            behavior.onDetach(this, context);
        }

        // Remove behavior and context
        behaviors.remove(behaviorId);
        behaviorContexts.remove(behaviorId);

        markModified();
        return true;
    }

    @Override
    public Optional<Behavior> getBehavior(String behaviorId) {
        return Optional.ofNullable(behaviors.get(behaviorId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Behavior> Optional<T> getBehavior(Class<T> behaviorClass) {
        for (Behavior behavior : behaviors.values()) {
            if (behaviorClass.isInstance(behavior)) {
                return Optional.of((T) behavior);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean hasBehavior(String behaviorId) {
        return behaviors.containsKey(behaviorId);
    }

    @Override
    public boolean hasBehavior(Class<? extends Behavior> behaviorClass) {
        for (Behavior behavior : behaviors.values()) {
            if (behaviorClass.isInstance(behavior)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Behavior> getAllBehaviors() {
        return Collections.unmodifiableCollection(behaviors.values());
    }

    @Override
    public Set<String> getBehaviorIds() {
        return Collections.unmodifiableSet(behaviors.keySet());
    }

    @Override
    public Optional<BehaviorContext> getBehaviorContext(String behaviorId) {
        return Optional.ofNullable(behaviorContexts.get(behaviorId));
    }

    // ==================== Tags ====================

    @Override
    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            tags.add(tag.toLowerCase().trim());
            markModified();
        }
    }

    @Override
    public boolean removeTag(String tag) {
        if (tag == null) return false;
        boolean removed = tags.remove(tag.toLowerCase().trim());
        if (removed) {
            markModified();
        }
        return removed;
    }

    @Override
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.toLowerCase().trim());
    }

    // ==================== Position & World ====================

    @Override
    public Optional<TokenPosition> getPosition() {
        return Optional.ofNullable(position);
    }

    @Override
    public void setPosition(TokenPosition position) {
        this.position = position;
        markModified();
    }

    @Override
    public Optional<UUID> getWorldId() {
        return Optional.ofNullable(worldId);
    }

    @Override
    public void setWorldId(UUID worldId) {
        this.worldId = worldId;
        markModified();
    }

    // ==================== Entity Binding ====================

    @Override
    public Optional<UUID> getBoundEntityId() {
        return Optional.ofNullable(boundEntityId);
    }

    void setBoundEntityId(UUID entityId) {
        this.boundEntityId = entityId;
        markModified();
    }

    // ==================== Ownership ====================

    @Override
    public Optional<UUID> getOwnerId() {
        return Optional.ofNullable(ownerId);
    }

    @Override
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        markModified();
    }

    // ==================== Timestamps ====================

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    /**
     * Marks this token as modified, updating the lastModifiedAt timestamp.
     */
    private void markModified() {
        this.lastModifiedAt = System.currentTimeMillis();
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleToken that = (SimpleToken) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Token{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", type=").append(type.getId());
        if (position != null) {
            sb.append(", pos=").append(position);
        }
        if (!behaviors.isEmpty()) {
            sb.append(", behaviors=").append(behaviors.keySet());
        }
        if (!components.isEmpty()) {
            sb.append(", components=").append(components.size());
        }
        if (!tags.isEmpty()) {
            sb.append(", tags=").append(tags);
        }
        sb.append('}');
        return sb.toString();
    }
}
