package org.vttale.vttale.api;

import org.vttale.vttale.api.command.CommandRegistry;
import org.vttale.vttale.api.events.EventBus;
import org.vttale.vttale.api.module.ModuleRegistry;

/**
 * The central service container for the VTTale system.
 * <p>
 * The Kernel provides access to core services such as:<br />
 * - {@link EventBus} for publish/subscribe event communication,<br />
 * - {@link CommandRegistry} for registering VTT commands,<br />
 * - {@link ModuleRegistry} for managing module lifecycle.
 * <p>
 * Other capabilities (e.g., token management, behavior dispatching, etc.)
 * are provided by modules and exposed via {@code getService(Class)}.
 * <p>
 * It acts as the main entry point for modules to interact with the VTT infrastructure.
 * @see VTTale#getKernel()
 */
public interface Kernel {

    /**
     * Returns the global event bus for publishing and subscribing to events.
     *
     * @return the event bus instance
     */
    EventBus getEventBus();

    /**
     * Returns the command registry for registering VTT commands.
     *
     * @return the command registry instance
     */
    CommandRegistry getCommandRegistry();

    /**
     * Returns the module registry for managing module lifecycle.
     *
     * @return the module registry instance
     */
    ModuleRegistry getModuleRegistry();

    /**
     * Returns a service by its class.
     *
     * @param serviceClass the class of the service to return
     * @param <T> the type of the service
     * @return the service instance, or null if not registered
     */
    <T> T getService(Class<T> serviceClass);

    /**
     * Registers a service.
     *
     * @param serviceClass the class to register the service under
     * @param service the service instance
     * @param <T> the type of the service
     */
    <T> void registerService(Class<T> serviceClass, T service);

}
