package org.vttale.vttale.platform.hytale.commands.player.hide;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class RealHidePlayerCommand extends AbstractTargetPlayerCommand {
    private final Map<UUID, Vector3d> hiddenPlayers = new HashMap<>();

    public RealHidePlayerCommand() {
        super("realhide", "Hide player also for itself.");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nullable Ref<EntityStore> sourceRef,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world,
            @Nonnull Store<EntityStore> store) {
        if (!hiddenPlayers.containsKey(playerRef.getUuid())) {
            enable(store, ref, playerRef);
            context.sendMessage(Message.raw("RealHide Activated"));
        } else {
            disable(store, ref, playerRef);
            context.sendMessage(Message.raw("RealHide Disabled"));
        }
    }

    private void enable(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef) {
        // 1. Store and move player up using Teleport component
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform != null) {
            Vector3d currentPos = new Vector3d(transform.getPosition());
            hiddenPlayers.put(playerRef.getUuid(), currentPos);

            Vector3d targetPos = new Vector3d(currentPos.getX(), currentPos.getY() + 25.0, currentPos.getZ());

            // Adding Teleport component ensures the client actually moves
            store.addComponent(ref, Teleport.getComponentType(),
                    Teleport.createForPlayer(targetPos, transform.getRotation()));
        }

        // 2. Set Creative and flight
        Player.setGameMode(ref, GameMode.Creative, store);
        playerRef.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(true)));

        // 2b. Block space bar from toggling flight off
        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager != null) {
            MovementSettings settings = movementManager.getSettings();
            if (settings != null) {
                settings.canFly = false; // Prevents double-space from toggling flight
                settings.jumpForce = 0; // Prevents single jump from doing anything
                playerRef.getPacketHandler().writeNoCache(new UpdateMovementSettings(settings));
            }
        }

        // 3. Components for visibility, interaction and damage
        hideFromOtherPlayers(store, ref);
        store.ensureComponent(ref, Intangible.getComponentType());
        store.ensureComponent(ref, Invulnerable.getComponentType());

        // 4. Camera settings
        applyCameraSettings(playerRef);
    }

    private void disable(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef) {
        Vector3d originalPosition = hiddenPlayers.remove(playerRef.getUuid());

        // 1. Restore GameMode and movement first
        Player.setGameMode(ref, GameMode.Adventure, store);

        // Update MovementManager to default for Adventure
        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager != null) {
            movementManager.resetDefaultsAndUpdate(ref, store);
            playerRef.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(false)));
        }

        // 2. Restore position AFTER GameMode change to prevent overwriting
        if (originalPosition != null) {
            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform != null) {
                store.addComponent(ref, Teleport.getComponentType(),
                        Teleport.createForPlayer(originalPosition, transform.getRotation()));
                playerRef.sendMessage(Message.raw("Restoring position to: " + originalPosition.toString()));
            }
        } else {
            playerRef.sendMessage(Message.raw("Error: Original position lost!"));
        }

        // 3. Restore camera to First Person
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));

        // 4. Remove components
        store.tryRemoveComponent(ref, HiddenFromAdventurePlayers.getComponentType());
        store.tryRemoveComponent(ref, Intangible.getComponentType());
        store.tryRemoveComponent(ref, Invulnerable.getComponentType());
    }

    private void hideFromOtherPlayers(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
        if (!store.getArchetype(ref).contains(HiddenFromAdventurePlayers.getComponentType())) {
            store.addComponent(ref, HiddenFromAdventurePlayers.getComponentType(),
                    HiddenFromAdventurePlayers.INSTANCE);
        }
    }

    private void applyCameraSettings(PlayerRef playerRef) {
        ServerCameraSettings cameraSettings = new ServerCameraSettings();
        cameraSettings.positionLerpSpeed = 0.5F;
        cameraSettings.rotationLerpSpeed = 0.5F;

        // cameraSettings.distance = 25.0F;

        cameraSettings.displayCursor = true;
        cameraSettings.sendMouseMotion = true;

        cameraSettings.isFirstPerson = false;
        cameraSettings.eyeOffset = true;
        cameraSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
        cameraSettings.rotationType = RotationType.Custom;
        cameraSettings.rotation = new Direction(0.0F, (float) (-Math.PI / 2), 0.0F);
        cameraSettings.movementForceRotationType = MovementForceRotationType.Custom;
        cameraSettings.mouseInputType = MouseInputType.LookAtPlane;
        cameraSettings.planeNormal = new Vector3f(0.0F, 1.0F, 0.0F);
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, cameraSettings));
    }
}
