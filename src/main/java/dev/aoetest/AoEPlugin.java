package dev.aoetest;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import dev.aoetest.commands.AoERadiusCommand;
import dev.aoetest.manager.AoEManager;

public class AoEPlugin extends JavaPlugin {

    public AoEPlugin(JavaPluginInit init) {
        super(init);
    }

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void setup() {
        this.getCommandRegistry().registerCommand(new AoERadiusCommand());
    }

    /**
     * Starts plugin; registers command; clears data on disconnect; starts task
     */
    @Override
    public void start() {
        LOGGER.atInfo().log("Starting the plugin AoE...");

        getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            AoEManager.clearPlayerData(event.getPlayerRef().getUuid());
        });

        AoEManager.startDetectionTask(getLogger());
    }

    @Override
    public void shutdown() {
        LOGGER.atInfo().log("Shutting down the plugin AoE...");
        AoEManager.stopDetectionTask();
    }
}