package org.vttale.vttale.platform.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import org.vttale.vttale.api.VTTale;
import org.vttale.vttale.platform.hytale.commands.player.hide.RealHidePlayerCommand;

import javax.annotation.Nonnull;

public class VTTaleHytalePlugin extends JavaPlugin {
    public VTTaleHytalePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        VTTale.init();
        VTTale.getKernel().getModuleRegistry().registerModule(new HytaleAdapter(this));
        this.getCommandRegistry().registerCommand(new RealHidePlayerCommand());
    }

}
