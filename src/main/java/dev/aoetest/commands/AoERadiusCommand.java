package dev.aoetest.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.aoetest.manager.AoEManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class AoERadiusCommand extends AbstractPlayerCommand {

    private final RequiredArg<Double> radiusArg;

    public AoERadiusCommand() {
        super("aoeradius", "Define the AoE detection radius");

        this.radiusArg = this.withRequiredArg("radius", "The size of radius", ArgTypes.DOUBLE)
                .addValidator(Validators.greaterThan(0.0));
    }

    /**
     * Sets player radius and sends confirmation message
     */
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        Double radius = radiusArg.get(commandContext);

        AoEManager.setPlayerRadius(playerRef.getUuid(), radius);

        commandContext.sendMessage(Message.raw("Radius set to " + radius).color(Color.green));
    }
}