package org.vttale.vttale.module.token.behavior;

import org.vttale.vttale.api.token.Token;
import org.vttale.vttale.api.token.TokenRegistry;
import org.vttale.vttale.api.token.behavior.Behavior;
import org.vttale.vttale.api.token.behavior.BehaviorContext;
import org.vttale.vttale.api.token.behavior.BehaviorDispatcher;
import org.vttale.vttale.api.token.behavior.BehaviorEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of BehaviorDispatcher.
 * <p>
 * Dispatches BehaviorEvents to token behaviors in a predictable order:<br />
 * 1. Target token's behaviors (if any)<br />
 * 2. Source token's behaviors (if any)<br />
 * 3. Global listeners
 */
public class SimpleBehaviorDispatcher implements BehaviorDispatcher {

    private static final Logger LOGGER = Logger.getLogger(SimpleBehaviorDispatcher.class.getName());

    private final TokenRegistry tokenRegistry;
    private final List<BehaviorEventListener> globalListeners;

    public SimpleBehaviorDispatcher(TokenRegistry tokenRegistry) {
        this.tokenRegistry = tokenRegistry;
        this.globalListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void dispatch(BehaviorEvent event) {
        // Dispatch to target token's behaviors
        event.getTargetToken().ifPresent(target -> dispatchToToken(event, target));

        // Dispatch to source token's behaviors (if different from target)
        event.getSourceToken().ifPresent(source -> {
            if (event.getTargetToken().map(t -> !t.getId().equals(source.getId())).orElse(true)) {
                dispatchToToken(event, source);
            }
        });

        // Notify global listeners
        notifyGlobalListeners(event);
    }

    @Override
    public void dispatchTo(BehaviorEvent event, Token token) {
        dispatchToToken(event, token);
        notifyGlobalListeners(event);
    }

    @Override
    public void dispatchTo(BehaviorEvent event, Collection<Token> tokens) {
        for (Token token : tokens) {
            dispatchToToken(event, token);
        }
        notifyGlobalListeners(event);
    }

    @Override
    public void broadcast(BehaviorEvent event) {
        for (Token token : tokenRegistry.getAll()) {
            dispatchToToken(event, token);
        }
        notifyGlobalListeners(event);
    }

    @Override
    public void addGlobalListener(BehaviorEventListener listener) {
        if (listener != null) {
            globalListeners.add(listener);
        }
    }

    @Override
    public void removeGlobalListener(BehaviorEventListener listener) {
        globalListeners.remove(listener);
    }

    /**
     * Dispatches an event to a single token's behaviors.
     */
    private void dispatchToToken(BehaviorEvent event, Token token) {
        for (Behavior behavior : token.getAllBehaviors()) {
            try {
                Optional<BehaviorContext> contextOpt = token.getBehaviorContext(behavior.getId());
                if (contextOpt.isPresent()) {
                    behavior.onEvent(token, event, contextOpt.get());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Error dispatching event " + event.getEventType() +
                                " to behavior " + behavior.getId() +
                                " on token " + token.getName(), e);
            }
        }
    }

    /**
     * Notifies all global listeners of an event.
     */
    private void notifyGlobalListeners(BehaviorEvent event) {
        for (BehaviorEventListener listener : globalListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in global behavior listener", e);
            }
        }
    }
}
