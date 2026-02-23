package org.vttale.vttale.api.token;

/**
 * Core token types provided by VTTale.
 * <p>
 * These are the fundamental types that most game systems will use as a base.
 * Plugins can:<br />
 * 1. Use these directly for common token types<br />
 * 2. Create their own TokenType implementations for game-specific needs
 * <p>
 * Example - Using core types:
 * <pre>{@code
 * Token player = tokenRegistry.create("Aragorn", CoreTokenType.PLAYER_CHARACTER);
 * Token goblin = tokenRegistry.create("Goblin", CoreTokenType.MONSTER);
 * }</pre>
 */
public enum CoreTokenType implements TokenType {

    PLAYER_CHARACTER("core:player_character", "Player Character", "PC"),
    NPC("core:npc", "Non-Player Character", "NPC"),
    MONSTER("core:monster", "Monster", "MON"),
    COMPANION("core:companion", "Companion", "CMP"),
    OBJECT("core:object", "Object", "OBJ"),
    VEHICLE("core:vehicle", "Vehicle", "VEH"),
    MARKER("core:marker", "Marker", "MRK");

    private final String id;
    private final String displayName;
    private final String shortName;

    CoreTokenType(String id, String displayName, String shortName) {
        this.id = id;
        this.displayName = displayName;
        this.shortName = shortName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    /**
     * Finds a CoreTokenType by its ID.
     *
     * @param id the type ID (e.g., "core:player_character")
     * @return the matching type, or null if not found
     */
    public static CoreTokenType fromId(String id) {
        if (id == null) return null;
        for (CoreTokenType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Finds a CoreTokenType by its short name (case-insensitive).
     *
     * @param shortName the short name (e.g., "PC", "NPC")
     * @return the matching type, or null if not found
     */
    public static CoreTokenType fromShortName(String shortName) {
        if (shortName == null) return null;
        for (CoreTokenType type : values()) {
            if (type.shortName.equalsIgnoreCase(shortName)) {
                return type;
            }
        }
        return null;
    }
}
