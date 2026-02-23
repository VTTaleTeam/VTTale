package org.vttale.vttale.api.token.behavior;

import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.token.Token;

import java.util.Optional;

/**
 * Base interface for events that behaviors can react to.
 * <p>
 * BehaviorEvents are game events that get dispatched to all behaviors
 * attached to relevant tokens. Plugins define their own event types
 * by implementing this interface.
 * <p>
 * VTTale provides no default events - plugins create them based on
 * their game system's needs.
 * <p>
 * Example - Defining events in a plugin:
 * <pre>{@code
 * public class DamageEvent implements BehaviorEvent {
 *     private final Token target;
 *     private final Token source;
 *     private int damage;
 *     private final String damageType;
 *     private boolean cancelled = false;
 *
 *     // Constructor, getters...
 *
 *     @Override
 *     public Optional<Token> getTargetToken() {
 *         return Optional.of(target);
 *     }
 *
 *     // Modifiable - behaviors can reduce/increase damage
 *     public void setDamage(int damage) {
 *         this.damage = damage;
 *     }
 *
 *     // Cancellable - behaviors can prevent damage entirely
 *     public void cancel() {
 *         this.cancelled = true;
 *     }
 * }
 * }</pre>
 * <p>
 * Example - Reacting to events in a behavior:
 * <pre>{@code
 * public class ResistancesBehavior implements Behavior {
 *     @Override
 *     public void onEvent(Token token, BehaviorEvent event, BehaviorContext ctx) {
 *         if (event instanceof DamageEvent damage) {
 *             String type = damage.getDamageType();
 *             Set<String> resistances = ctx.get("resistances", Set.of());
 *
 *             if (resistances.contains(type)) {
 *                 damage.setDamage(damage.getDamage() / 2);
 *             }
 *         }
 *     }
 * }
 * }</pre>
 */
public interface BehaviorEvent extends Event {

    /**
     * Returns the primary token this event targets, if any.
     * <p>
     * This is used to determine which token's behaviors should react.
     * If empty, the event may be broadcast to all tokens or handled differently.
     *
     * @return an Optional containing the target token
     */
    default Optional<Token> getTargetToken() {
        return Optional.empty();
    }

    /**
     * Returns the token that caused this event, if any.
     *
     * @return an Optional containing the source token
     */
    default Optional<Token> getSourceToken() {
        return Optional.empty();
    }

    /**
     * Returns true if this event has been cancelled by a behavior.
     * <p>
     * Not all events are cancellable. Override this and provide a
     * cancel() method if your event can be cancelled.
     *
     * @return true if cancelled
     */
    default boolean isCancelled() {
        return false;
    }

    /**
     * Returns the event type identifier.
     * <p>
     * Format: "namespace:event_name" (e.g., "dnd5e:damage", "dnd5e:saving_throw")
     * <p>
     * Default implementation uses the class name.
     *
     * @return the event type ID
     */
    default String getEventType() {
        return getClass().getSimpleName();
    }
}
