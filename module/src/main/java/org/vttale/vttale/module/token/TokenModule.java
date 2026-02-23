package org.vttale.vttale.module.token;

import org.vttale.vttale.api.Kernel;
import org.vttale.vttale.api.module.Module;
import org.vttale.vttale.api.token.TokenRegistry;
import org.vttale.vttale.api.token.behavior.BehaviorDispatcher;
import org.vttale.vttale.module.token.behavior.SimpleBehaviorDispatcher;

/**
 * Module providing token management capabilities.
 */
public class TokenModule implements Module {
    @Override
    public void onEnable(Kernel kernel) {
        // Instantiate and register token services
        TokenRegistry registry = new SimpleTokenRegistry(kernel);
        kernel.registerService(TokenRegistry.class, registry);

        BehaviorDispatcher dispatcher = new SimpleBehaviorDispatcher(registry);
        kernel.registerService(BehaviorDispatcher.class, dispatcher);
    }
}
