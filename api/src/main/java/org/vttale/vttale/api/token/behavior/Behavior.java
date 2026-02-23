package org.vttale.vttale.api.token.behavior;

import org.vttale.vttale.api.token.Token;

/**
 * Represents a behavior that can be attached to tokens.
 * <p>
 * Behaviors define LOGIC - they react to events and perform actions.
 * VTTale provides no default behaviors; plugins create their own.
 * <p>
 * A Behavior is like a "script" attached to a token that reacts to game events.
 * Multiple behaviors can be attached to the same token.
 * <p>
 * Example - Creating a behavior in a plugin:
 * <pre>{@code
 * public class LegendaryBehavior implements Behavior {
 *
 *     @Override
 *     public String getId() {
 *         return "dnd5e:legendary";
 *     }
 *
 *     @Override
 *     public void onAttach(Token token, BehaviorContext context) {
 *         // Initialize legendary action counter
 *         context.set("actions_remaining", 3);
 *         context.set("resistances_remaining", 3);
 *     }
 *
 *     @Override
 *     public void onEvent(Token token, BehaviorEvent event, BehaviorContext context) {
 *         if (event instanceof TurnEndEvent turnEnd) {
 *             // Reset legendary actions at end of this creature's turn
 *             if (turnEnd.getToken().equals(token)) {
 *                 context.set("actions_remaining", 3);
 *             }
 *         }
 *
 *         if (event instanceof SavingThrowFailedEvent saveFailed) {
 *             int resistances = context.getInt("resistances_remaining", 0);
 *             if (resistances > 0) {
 *                 // Offer to use legendary resistance
 *                 event.getEventBus().publish(new LegendaryResistancePromptEvent(token));
 *             }
 *         }
 *     }
 * }
 * }</pre>
 * <p>
 * Attaching to a token:
 * <pre>{@code
 * Token dragon = tokenRegistry.create("Ancient Red Dragon", CoreTokenType.MONSTER);
 * dragon.attachBehavior(new LegendaryBehavior());
 * }</pre>
 */
public interface Behavior {

    /**
     * Returns the unique identifier for this behavior.
     * <p>
     * Format: "namespace:name" (e.g., "dnd5e:legendary", "dnd5e:pack_tactics")
     *
     * @return the behavior ID
     */
    String getId();

    /**
     * Returns a human-readable name for this behavior.
     * Used in UIs and logs.
     * <p>
     * Default implementation derives from ID.
     *
     * @return the display name
     */
    default String getDisplayName() {
        String id = getId();
        int colonIndex = id.indexOf(':');
        // Extracts display name from ID after namespace
        if (colonIndex >= 0 && colonIndex < id.length() - 1) {
            String name = id.substring(colonIndex + 1);
            return name.replace('_', ' ').substring(0, 1).toUpperCase()
                    + name.replace('_', ' ').substring(1);
        }
        return id;
    }

    /**
     * Returns the namespace (plugin) this behavior belongs to.
     *
     * @return the namespace
     */
    default String getNamespace() {
        String id = getId();
        int colonIndex = id.indexOf(':');
        return colonIndex > 0 ? id.substring(0, colonIndex) : "unknown";
    }

    /**
     * Called when this behavior is attached to a token.
     * Use this to initialize any state in the context.
     *
     * @param token   the token this behavior is attached to
     * @param context the behavior's private context for storing state
     */
    default void onAttach(Token token, BehaviorContext context) {
        // Default: do nothing
    }

    /**
     * Called when this behavior is detached from a token.
     * Use this to clean up any resources.
     *
     * @param token   the token this behavior was attached to
     * @param context the behavior's context
     */
    default void onDetach(Token token, BehaviorContext context) {
        // Default: do nothing
    }

    /**
     * Called when a game event occurs that this behavior might react to.
     * <p>
     * The behavior should check the event type and react accordingly.
     *
     * @param token   the token this behavior is attached to
     * @param event   the game event
     * @param context the behavior's context for reading/writing state
     */
    default void onEvent(Token token, BehaviorEvent event, BehaviorContext context) {
        // Default: do nothing
    }

    /**
     * Creates a new instance of this behavior.
     * <p>
     * Override this if your behavior needs to be cloned with specific state.
     * Default implementation returns this (assumes stateless or shared).
     *
     * @return a new instance or this
     */
    default Behavior copy() {
        return this;
    }
}
