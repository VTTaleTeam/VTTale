package org.vttale.vttale.api.token;

/**
 * Base interface for all token components.
 * <p>
 * TokenComponent is the key extensibility point of VTTale. Each game system
 * defines its own components to store game-specific data on tokens.
 * <p>
 * Components are identified by a unique string ID in the format "namespace:name"
 * (e.g., "dnd5e:ability_scores", "daggerheart:stress").
 * <p>
 * Implementation Guidelines: <p>
 * 1. Components should be immutable or thread-safe <p>
 * 2. Implement equals() and hashCode() based on component data <p>
 * 3. Consider implementing a copy() method for cloning <p>
 * 4. Use the companion CODEC pattern for serialization
 * <p>
 * Example implementation:
 * <pre>{@code
 * public class DnD5eStatsComponent implements TokenComponent {
 *     public static final String ID = "dnd5e:ability_scores";
 *
 *     private int strength = 10;
 *     private int dexterity = 10;
 *     // ... other ability scores
 *
 *     @Override
 *     public String getComponentId() {
 *         return ID;
 *     }
 *
 *     // Getters, setters, etc.
 * }
 * }</pre>
 */
public interface TokenComponent {

    /**
     * Returns the unique identifier for this component type.
     * <p>
     * The ID should be in the format "namespace:name" where:
     * - namespace: identifies the game system or module (e.g., "dnd5e", "daggerheart", "core")
     * - name: identifies the specific component (e.g., "ability_scores", "spells", "health")
     * <p>
     * This ID is used for serialization and component lookup.
     *
     * @return the component's unique identifier
     */
    String getComponentId();

    /**
     * Returns a human-readable name for this component.
     * Used for display in UIs and logs.
     * Default implementation extracts the name part from the component ID.
     *
     * @return the display name
     */
    default String getDisplayName() {
        String id = getComponentId();
        int colonIndex = id.indexOf(':');
        // Extracts display name from component ID
        if (colonIndex >= 0 && colonIndex < id.length() - 1) {
            String name = id.substring(colonIndex + 1);
            return toTitleCase(name.replace('_', ' '));
        }
        return id;
    }

    /**
     * Returns the namespace (game system) this component belongs to.
     * <p>
     * Default implementation extracts the namespace from the component ID.
     *
     * @return the namespace
     */
    default String getNamespace() {
        String id = getComponentId();
        int colonIndex = id.indexOf(':');
        if (colonIndex > 0) {
            return id.substring(0, colonIndex);
        }
        return "unknown";
    }

    /**
     * Creates a deep copy of this component.
     * <p>
     * This is used when cloning tokens or creating templates.
     * Default implementation returns this (assumes immutability).
     * <p>
     * Mutable components should override this method.
     *
     * @return a copy of this component
     */
    default TokenComponent copy() {
        return this;
    }

    /**
     * Validates that this component's data is in a valid state.
     * <p>
     * This is called before saving or when explicitly requested.
     * Components can override this to enforce invariants.
     *
     * @return true if the component data is valid
     */
    default boolean isValid() {
        return true;
    }

    /**
     * Returns a brief summary of this component's data for logging/debugging.
     * <p>
     * Default implementation returns the component ID.
     *
     * @return a summary string
     */
    default String getSummary() {
        return getComponentId();
    }

    // ==================== Utility Methods ====================

    /**
     * Converts a string to Title Case.
     */
    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            // Converts character to title case respecting whitespace
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
