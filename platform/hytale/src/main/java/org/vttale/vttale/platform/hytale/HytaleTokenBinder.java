package org.vttale.vttale.platform.hytale;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.vttale.vttale.api.Kernel;
import org.vttale.vttale.api.events.EventBus;
import org.vttale.vttale.api.events.EventContext;
import org.vttale.vttale.api.token.CoreTokenType;
import org.vttale.vttale.api.token.Token;
import org.vttale.vttale.api.token.TokenPosition;
import org.vttale.vttale.api.token.TokenRegistry;
import org.vttale.vttale.api.token.events.TokenBoundEvent;
import org.vttale.vttale.api.token.events.TokenRemovedEvent;
import org.vttale.vttale.api.token.events.TokenUpdatedEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Handles synchronization between VTTale tokens and Hytale entities.
 * <p>
 * This class bridges the gap between the platform-agnostic Token system
 * and the Hytale-specific entity system.
 * <p>
 * Responsibilities:<br />
 * - Creating Hytale entities when tokens are spawned<br />
 * - Synchronizing token data to entities (position, name, etc.)<br />
 * - Listening to Hytale events and updating tokens<br />
 * - Managing the token-entity binding lifecycle
 * <p>
 * Thread Safety:
 * All Hytale entity access is performed via world.execute() to ensure
 * thread safety with Hytale's ECS system.
 */
public class HytaleTokenBinder {

    private static final Logger LOGGER = Logger.getLogger(HytaleTokenBinder.class.getName());

    private final Kernel kernel;
    private final TokenRegistry tokenRegistry;
    private final JavaPlugin plugin;

    /**
     * Creates a new HytaleTokenBinder.
     *
     * @param kernel the VTTale kernel
     * @param plugin the Hytale plugin instance
     */
    public HytaleTokenBinder(Kernel kernel, JavaPlugin plugin) {
        this.kernel = kernel;
        this.tokenRegistry = kernel.getService(TokenRegistry.class);
        this.plugin = plugin;
    }

    /**
     * Initializes the binder and registers event listeners.
     */
    public void initialize() {
        LOGGER.info("Initializing HytaleTokenBinder...");

        EventBus eventBus = kernel.getEventBus();

        // Listen for VTTale token events
        eventBus.subscribe(TokenBoundEvent.class, this::onTokenBound);
        eventBus.subscribe(TokenUpdatedEvent.class, this::onTokenUpdated);
        eventBus.subscribe(TokenRemovedEvent.class, this::onTokenRemoved);

        // Listen for Hytale player events
        plugin.getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);
        plugin.getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        LOGGER.info("HytaleTokenBinder initialized");
    }

    // ==================== VTTale Token Events ====================

    /**
     * Handles token binding/unbinding events.
     */
    private void onTokenBound(TokenBoundEvent event, EventContext context) {
        Token token = event.getToken();

        // Handles token binding/unbinding; logs and syncs as needed
        if (event.isBound()) {
            UUID entityId = event.getEntityId().orElse(null);
            if (entityId != null) {
                LOGGER.fine("Token " + token.getName() + " bound to entity " + entityId);
                syncTokenToEntity(token, entityId);
            }
        } else if (event.isUnbound()) {
            UUID previousEntityId = event.getPreviousEntityId().orElse(null);
            if (previousEntityId != null) {
                LOGGER.fine("Token " + token.getName() + " unbound from entity " + previousEntityId);
            }
        }
    }

    /**
     * Handles token update events.
     */
    private void onTokenUpdated(TokenUpdatedEvent event, EventContext context) {
        Token token = event.getToken();

        // Only sync if token is bound to an entity
        token.getBoundEntityId().ifPresent(entityId -> {
            switch (event.getUpdateType()) {
                case POSITION_CHANGED -> syncPositionToEntity(token, entityId);
                case NAME_CHANGED -> syncNameToEntity(token, entityId);
                case COMPONENT_UPDATED, COMPONENT_ADDED -> {
                    // Could sync specific component data if needed
                }
                default -> { /* No sync needed */ }
            }
        });
    }

    /**
     * Handles token removal events.
     */
    private void onTokenRemoved(TokenRemovedEvent event, EventContext context) {
        // If the token was bound to an entity, log it
        // Note: We don't auto-despawn entities here, as the entity might be
        // a player or have other purposes. Let the caller handle despawning.
        event.getBoundEntityId().ifPresent(entityId -> {
            LOGGER.fine("Token " + event.getTokenName() + " removed, was bound to entity " + entityId);
        });
    }

    // ==================== Hytale Player Events ====================

    /**
     * Handles player connection - creates a token for the player.
     */
    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        World world = event.getWorld();

        // Check if a token already exists for this player
        Optional<Token> existingToken = tokenRegistry.getByEntityId(playerRef.getUuid());
        if (existingToken.isPresent()) {
            LOGGER.fine("Player " + playerRef.getUsername() + " already has a token");
            return;
        }

        // Create a new token for the player
        Token playerToken = createTokenForPlayer(playerRef.getUuid(), playerRef.getUsername());

        // Set the world
        playerToken.setWorldId(world.getWorldConfig().getUuid());

        // Bind to the player entity
        tokenRegistry.bindToEntity(playerToken.getId(), playerRef.getUuid());

        // Update position from player
        world.execute(() -> {
            var transform = playerRef.getTransform();
            // Updates token position from player transform
            if (transform != null) {
                TokenPosition pos = new TokenPosition(
                        transform.getPosition().getX(),
                        transform.getPosition().getY(),
                        transform.getPosition().getZ(),
                        transform.getRotation().getY(), // yaw
                        transform.getRotation().getX()  // pitch
                );
                playerToken.setPosition(pos);
            }
        });

        LOGGER.info("Created token for player: " + playerRef.getUsername());
    }

    /**
     * Handles player disconnection.
     */
    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();

        // Find and unbind the player's token (but don't remove it)
        tokenRegistry.getByEntityId(playerRef.getUuid()).ifPresent(token -> {
            tokenRegistry.unbindFromEntity(token.getId());
            LOGGER.fine("Unbound token for disconnected player: " + playerRef.getUsername());
        });
    }

    // ==================== Synchronization ====================

    /**
     * Synchronizes all token data to a Hytale entity.
     */
    private void syncTokenToEntity(Token token, UUID entityId) {
        World world = getWorldForToken(token);
        if (world == null) return;

        world.execute(() -> {
            Entity entity = world.getEntity(entityId);
            if (entity == null) {
                LOGGER.warning("Cannot sync token to entity: entity " + entityId + " not found");
                return;
            }

            // Sync position
            token.getPosition().ifPresent(pos -> {
                entity.moveTo(
                        entity.getReference(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        world.getEntityStore().getStore()
                );
            });

            LOGGER.fine("Synced token " + token.getName() + " to entity " + entityId);
        });
    }

    /**
     * Synchronizes token position to entity.
     */
    private void syncPositionToEntity(Token token, UUID entityId) {
        World world = getWorldForToken(token);
        if (world == null) return;

        token.getPosition().ifPresent(pos -> {
            world.execute(() -> {
                Entity entity = world.getEntity(entityId);
                if (entity != null) {
                    entity.moveTo(
                            entity.getReference(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            world.getEntityStore().getStore()
                    );
                }
            });
        });
    }

    /**
     * Synchronizes token name to entity (for entities that support display names).
     */
    private void syncNameToEntity(Token token, UUID entityId) {
        // Most entities don't have settable display names, but we can
        // update this for custom NPCs when the API supports it
        LOGGER.fine("Name sync requested for " + token.getName() + " (not implemented yet)");
    }

    // ==================== Public API ====================

    /**
     * Creates a token for a player.
     *
     * @param playerId   the player's UUID
     * @param playerName the player's name
     * @return the created token
     */
    public Token createTokenForPlayer(UUID playerId, String playerName) {
        Token token = tokenRegistry.create(playerName, CoreTokenType.PLAYER_CHARACTER, playerId);
        LOGGER.info("Created token for player: " + playerName);
        return token;
    }

    /**
     * Spawns a Hytale entity for a token.
     *
     * @param token      the token to spawn
     * @param entityType the Hytale entity type ID
     * @return a future that completes with the entity UUID
     */
    public CompletableFuture<UUID> spawnEntityForToken(Token token, String entityType) {
        CompletableFuture<UUID> future = new CompletableFuture<>();

        World world = getWorldForToken(token);
        if (world == null) {
            future.completeExceptionally(new IllegalStateException("No world available"));
            return future;
        }

        TokenPosition pos = token.getPosition().orElse(TokenPosition.origin());

        world.execute(() -> {
            try {
                Vector3d position = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
                Vector3f rotation = new Vector3f(pos.getPitch(), pos.getYaw(), 0);

                // TODO: Use Hytale's entity registry to spawn the correct entity type
                // Entity entity = world.spawnEntity(entityType, position, rotation);
                // tokenRegistry.bindToEntity(token.getId(), entity.getUuid());
                // future.complete(entity.getUuid());

                LOGGER.info("Would spawn entity type " + entityType + " at " + position);
                future.completeExceptionally(new UnsupportedOperationException(
                        "Entity spawning not yet fully implemented - waiting for Hytale API"));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Spawns an entity for a token at a specific position (simplified version).
     *
     * @param token    the token to spawn
     * @param position the position to spawn at
     * @return the entity UUID (placeholder until Hytale API available)
     */
    public UUID spawnEntityForToken(Token token, TokenPosition position) {
        token.setPosition(position);

        World world = getWorldForToken(token);
        if (world != null) {
            // TODO: Implement actual entity spawning
            LOGGER.info("Would spawn entity for token: " + token.getName() + " at " + position);
        }

        // For now, create a placeholder UUID and bind
        UUID entityId = UUID.randomUUID();
        tokenRegistry.bindToEntity(token.getId(), entityId);
        return entityId;
    }

    /**
     * Despawns the entity bound to a token.
     *
     * @param token the token to despawn
     * @return true if the entity was despawned
     */
    public boolean despawnEntityForToken(Token token) {
        UUID entityId = token.getBoundEntityId().orElse(null);
        if (entityId == null) {
            return false;
        }

        World world = getWorldForToken(token);
        if (world != null) {
            world.execute(() -> {
                Entity entity = world.getEntity(entityId);
                if (entity != null && !(entity instanceof Player)) {
                    // Don't despawn players
                    entity.remove();
                    LOGGER.fine("Despawned entity " + entityId + " for token " + token.getName());
                }
            });
        }

        // Unbind the token
        tokenRegistry.unbindFromEntity(token.getId());
        LOGGER.info("Despawned entity for token: " + token.getName());

        return true;
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the Hytale World for a token.
     *
     * @param token the token
     * @return the World, or default world if token has no world set
     */
    private World getWorldForToken(Token token) {
        UUID worldId = token.getWorldId().orElse(null);
        if (worldId == null) {
            return Universe.get().getDefaultWorld();
        }
        return Universe.get().getWorld(worldId);
    }

    /**
     * Gets the kernel.
     *
     * @return the kernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Gets the token registry.
     *
     * @return the token registry
     */
    public TokenRegistry getTokenRegistry() {
        return tokenRegistry;
    }
}