package dev.aoetest.manager;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AoEManager {
    // Configuration des joueurs
    private static final Map<UUID, Double> playerRadius = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> playerSquaredRadius = new ConcurrentHashMap<>(); // Pré-calculé

    // État des zones
    private static final Map<UUID, Set<UUID>> entitiesInsideZones = new ConcurrentHashMap<>();
    private static final Map<UUID, String> entityNameCache = new ConcurrentHashMap<>();

    // Collections réutilisables (évite les allocations)
    private static final Map<UUID, Map<UUID, String>> currentTickResults = new ConcurrentHashMap<>();

    private static ScheduledFuture<?> detectionTask;
    private static HytaleLogger logger;

    public static void setPlayerRadius(UUID playerUUID, double radius) {
        logger.atInfo().log("Player " + playerUUID + " radius set to " + radius);
        if (radius <= 0) {
            playerRadius.remove(playerUUID);
            playerSquaredRadius.remove(playerUUID);
        } else {
            playerRadius.put(playerUUID, radius);
            playerSquaredRadius.put(playerUUID, radius * radius); // Pré-calcul
        }
    }

    public static void clearPlayerData(UUID playerUUID) {
        playerRadius.remove(playerUUID);
        playerSquaredRadius.remove(playerUUID);
        entitiesInsideZones.remove(playerUUID);
        currentTickResults.remove(playerUUID);
    }

    public static void startDetectionTask(HytaleLogger logger) {
        AoEManager.logger = logger;
        detectionTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                performDetection();
            } catch (Exception e) {
                logger.atSevere().log("Issue in AoE detection task", e);
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopDetectionTask() {
        if (detectionTask != null && !detectionTask.isCancelled()) {
            detectionTask.cancel(true);
        }
    }

    /**
     * Performs entity detection within configured player radii
     */
    private static void performDetection() {
        Universe universe = Universe.get();
        if (universe == null) return;

        World world = universe.getDefaultWorld();
        if (world == null) return;

        // Skip si aucun joueur n'a de radius configuré
        if (playerSquaredRadius.isEmpty()) return;

        world.execute(() -> {
            Store<EntityStore> store = world.getEntityStore().getStore();

            // Pré-calcule les données des joueurs actifs avec radius
            List<PlayerDetectionData> activeDetectors = new ArrayList<>();
            for (PlayerRef player : world.getPlayerRefs()) {
                UUID playerUUID = player.getUuid();
                Double sqRadius = playerSquaredRadius.get(playerUUID);
                if (sqRadius == null) continue;

                Vector3d pos = player.getTransform().getPosition();
                double radius = playerRadius.get(playerUUID);

                // Réutilise ou crée la map pour ce joueur
                Map<UUID, String> results = currentTickResults.computeIfAbsent(playerUUID, _ -> new ConcurrentHashMap<>());
                results.clear();

                activeDetectors.add(new PlayerDetectionData(player, playerUUID, pos, radius, sqRadius, results));
            }

            if (activeDetectors.isEmpty()) return;

            // UNE SEULE itération sur toutes les entités
            store.forEachEntityParallel(TransformComponent.getComponentType(), (index, archetypeChunk, _) -> {
                UUIDComponent uuidComp = archetypeChunk.getComponent(index, UUIDComponent.getComponentType());
                if (uuidComp == null) return;

                UUID entityUUID = uuidComp.getUuid();

                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) return;

                Vector3d entityPos = transform.getPosition();
                String entityName = null; // Lazy loading

                // Vérifie pour chaque joueur actif
                for (PlayerDetectionData detector : activeDetectors) {
                    if (entityUUID.equals(detector.playerUUID)) continue;

                    // Early exit: vérifie Y d'abord (souvent le plus discriminant)
                    double dy = detector.pos.y - entityPos.y;
                    if (dy * dy > detector.squaredRadius) continue;

                    // Ensuite X
                    double dx = detector.pos.x - entityPos.x;
                    if (dx * dx > detector.squaredRadius) continue;

                    // Ensuite Z
                    double dz = detector.pos.z - entityPos.z;
                    if (dz * dz > detector.squaredRadius) continue;

                    // Calcul final complet
                    double squaredDistance = dx * dx + dy * dy + dz * dz;
                    if (squaredDistance <= detector.squaredRadius) {
                        // Lazy load du nom (une seule fois par entité)
                        if (entityName == null) {
                            entityName = getEntityName(index, archetypeChunk, entityUUID);
                        }
                        detector.results.put(entityUUID, entityName);
                    }
                }
            });

            // Traitement des résultats pour chaque joueur
            for (PlayerDetectionData detector : activeDetectors) {
                processResults(detector.player, detector.playerUUID, detector.results);
            }
        });
    }

    private static void processResults(PlayerRef player, UUID playerUUID, Map<UUID, String> currentlyInside) {
        Set<UUID> previouslyInside = entitiesInsideZones.computeIfAbsent(playerUUID, _ -> new HashSet<>());

        // Entrées
        for (Map.Entry<UUID, String> entry : currentlyInside.entrySet()) {
            UUID entityUUID = entry.getKey();
            if (!previouslyInside.contains(entityUUID)) {
                String name = entry.getValue();
                entityNameCache.put(entityUUID, name);
                handleEntry(player, name);
            }
        }

        // Sorties
        for (UUID oldUUID : previouslyInside) {
            if (!currentlyInside.containsKey(oldUUID)) {
                String name = entityNameCache.remove(oldUUID);
                if (name == null) name = oldUUID.toString().substring(0, 8);
                handleExit(player, name);
            }
        }

        previouslyInside.clear();
        previouslyInside.addAll(currentlyInside.keySet());
    }

    private static String getEntityName(int index, ArchetypeChunk archetypeChunk, UUID fallbackUUID) {
        Entity entity = EntityUtils.getEntity(index, archetypeChunk);
        if (entity != null) {
            return entity.getClass().getSimpleName();
        }
        return fallbackUUID.toString().substring(0, 8);
    }

    private static void handleEntry(PlayerRef player, String entityName) {
        player.sendMessage(
                Message.raw(entityName + " has entered your AoE zone!").color(java.awt.Color.GREEN)
        );
    }

    private static void handleExit(PlayerRef player, String entityName) {
        player.sendMessage(
                Message.raw(entityName + " has left your AoE zone!").color(java.awt.Color.RED)
        );
    }

    // Structure pour les données de détection pré-calculées
    private static class PlayerDetectionData {
        final PlayerRef player;
        final UUID playerUUID;
        final Vector3d pos;
        final double radius;
        final double squaredRadius;
        final Map<UUID, String> results;

        PlayerDetectionData(PlayerRef player, UUID playerUUID, Vector3d pos, double radius, double squaredRadius, Map<UUID, String> results) {
            this.player = player;
            this.playerUUID = playerUUID;
            this.pos = pos;
            this.radius = radius;
            this.squaredRadius = squaredRadius;
            this.results = results;
        }
    }
}