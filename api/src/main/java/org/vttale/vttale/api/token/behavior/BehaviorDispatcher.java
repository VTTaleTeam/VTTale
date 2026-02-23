package org.vttale.vttale.api.token.behavior;

import org.vttale.vttale.api.token.Token;

import java.util.Collection;

/**
 * Dispatches BehaviorEvents to token behaviors.
 * <p>
 * The dispatcher is responsible for sending events to the appropriate
 * behaviors attached to tokens. Plugins use this to trigger behavior
 * reactions when game events occur.
 * <p>
 * Access via: VTTale.getKernel().getService(BehaviorDispatcher.class)
 * <p>
 * Example - Dispatching a damage event:
 * <pre>{@code
 * BehaviorDispatcher dispatcher = kernel.getService(BehaviorDispatcher.class);
 *
 * DamageEvent event = new DamageEvent(target, source, 15, "fire");
 *
 * // Dispatch to target's behaviors (they can modify/cancel)
 * dispatcher.dispatch(event);
 *
 * // Check if cancelled
 * if (!event.isCancelled()) {
 *     int finalDamage = event.getDamage();
 *     // Apply damage...
 * }
 * }</pre>
 * <p>
 * Dispatch order:<br />
 * 1. Event is sent to all behaviors on the target token (if any)<br />
 * 2. Event is sent to all behaviors on the source token (if any)<br />
 * 3. Global listeners are notified (for logging, UI updates, etc.)
 */
public interface BehaviorDispatcher {

    /**
     * Dispatches an event to relevant token behaviors.
     * <p>
     * The event is sent to:<br />
     * 1. Behaviors on event.getTargetToken() (if present)<br />
     * 2. Behaviors on event.getSourceToken() (if present)
     *
     * @param event the event to dispatch
     */
    void dispatch(BehaviorEvent event);

    /**
     * Dispatches an event to a specific token's behaviors only.
     *
     * @param event the event to dispatch
     * @param token the token whose behaviors should receive the event
     */
    void dispatchTo(BehaviorEvent event, Token token);

    /**
     * Dispatches an event to multiple tokens' behaviors.
     *
     * @param event  the event to dispatch
     * @param tokens the tokens whose behaviors should receive the event
     */
    void dispatchTo(BehaviorEvent event, Collection<Token> tokens);

    /**
     * Dispatches an event to ALL tokens' behaviors.
     * Use sparingly - this can be expensive.
     *
     * @param event the event to broadcast
     */
    void broadcast(BehaviorEvent event);

    /**
     * Registers a global listener that receives all dispatched events.
     * Useful for logging, debugging, or UI updates.
     *
     * @param listener the listener
     */
    void addGlobalListener(BehaviorEventListener listener);

    /**
     * Removes a global listener.
     *
     * @param listener the listener to remove
     */
    void removeGlobalListener(BehaviorEventListener listener);

    /**
     * Functional interface for global event listeners.
     */
    @FunctionalInterface
    interface BehaviorEventListener {
        /**
         * Called when any behavior event is dispatched.
         *
         * @param event the event
         */
        void onEvent(BehaviorEvent event);
    }
}
