package org.vttale.vttale.kernel;

import org.vttale.vttale.api.Kernel;
import org.vttale.vttale.api.command.CommandRegistry;
import org.vttale.vttale.api.events.EventBus;
import org.vttale.vttale.api.module.Module;
import org.vttale.vttale.api.module.ModuleRegistry;
import org.vttale.vttale.kernel.command.SimpleCommandRegistry;
import org.vttale.vttale.kernel.events.SimpleEventBus;
import org.vttale.vttale.kernel.module.SimpleModuleRegistry;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class VTTaleKernel implements Kernel {

    private final EventBus eventBus;
    private final CommandRegistry commandRegistry;
    private final ModuleRegistry moduleRegistry;
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Initializes registries; registers modules via service provider
     */
    public VTTaleKernel() {
        this.eventBus = new SimpleEventBus();
        this.commandRegistry = new SimpleCommandRegistry(eventBus);
        this.moduleRegistry = new SimpleModuleRegistry(this);

        // Auto-discover and register modules via SPI
        ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
        for (Module module : loader) {
            moduleRegistry.registerModule(module);
        }
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
    @Override
    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return serviceClass.cast(services.get(serviceClass));
    }

    @Override
    public <T> void registerService(Class<T> serviceClass, T service) {
        services.put(serviceClass, service);
    }
}
