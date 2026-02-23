package org.vttale.vttale.api.token;

/**
 * Represents a token type in the VTTale system.
 * <p>
 * TokenType is an interface to allow plugins to define their own types.
 * Core types are provided via {@link CoreTokenType}, but plugins can create
 * custom implementations for game-specific needs.
 * <p>
 * Example - Creating custom types for a plugin:
 * <pre>{@code
 * public enum DnD5eTokenType implements TokenType {
 *     SIDEKICK("dnd5e:sidekick", "Sidekick", "SDK"),
 *     FAMILIAR("dnd5e:familiar", "Familiar", "FAM"),
 *     LAIR("dnd5e:lair", "Lair", "LAIR");
 *
 *     // ... implementation
 * }
 * }</pre>
 */
public interface TokenType {

    /**
     * Returns the unique identifier for this token type.
     * <p>
     * Format: "namespace:name" where:<br />
     * - namespace: identifies the plugin/system (e.g., "core", "dnd5e", "daggerheart")<br />
     * - name: identifies the specific type (e.g., "player_character", "familiar")
     * <p>
     * Examples:<br />
     * - "core:player_character"<br />
     * - "core:npc"<br />
     * - "dnd5e:sidekick"<br />
     * - "daggerheart:companion"
     *
     * @return the type ID, never null
     */
    String getId();

    /**
     * Returns a human-readable display name for this token type.
     * Used in UIs and logs.
     *
     * @return the display name (e.g., "Player Character", "Non-Player Character")
     */
    String getDisplayName();

    /**
     * Returns a short name for compact display.
     * Useful for badges, icons, and space-constrained UIs.
     *
     * @return the short name (e.g., "PC", "NPC", "MON")
     */
    String getShortName();

    /**
     * Returns the namespace (plugin/system) this type belongs to.
     * <p>
     * Default implementation extracts from the ID.
     *
     * @return the namespace (e.g., "core", "dnd5e")
     */
    default String getNamespace() {
        String id = getId();
        int colonIndex = id.indexOf(':');
        return colonIndex > 0 ? id.substring(0, colonIndex) : "core";
    }

    /**
     * Returns the name part of the ID (without namespace).
     *
     * @return the name (e.g., "player_character", "familiar")
     */
    default String getName() {
        String id = getId();
        int colonIndex = id.indexOf(':');
        return colonIndex >= 0 && colonIndex < id.length() - 1
                ? id.substring(colonIndex + 1)
                : id;
    }

    /**
     * Checks if this type matches another type by ID.
     *
     * @param other the other type
     * @return true if IDs match
     */
    default boolean matches(TokenType other) {
        return other != null && getId().equals(other.getId());
    }

    /**
     * Checks if this type matches a type ID string.
     *
     * @param typeId the type ID to match
     * @return true if IDs match
     */
    default boolean matches(String typeId) {
        return typeId != null && getId().equals(typeId);
    }
}
