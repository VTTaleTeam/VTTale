package org.vttale.vttale.api.token;

import org.vttale.vttale.api.token.behavior.Behavior;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Fluent query builder for finding tokens in the registry.
 * <p>
 * TokenQuery allows building complex queries using a chainable API.
 * All filter conditions are combined with AND logic.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Find all player characters in a specific world
 * Collection<Token> pcs = tokenRegistry.query()
 *     .withType(CoreTokenType.PLAYER_CHARACTER)
 *     .inWorld(worldId)
 *     .execute();
 *
 * // Find all creatures that are GM controlled
 * Collection<Token> gmCreatures = tokenRegistry.query()
 *     .creaturesOnly()
 *     .gmControlledOnly()
 *     .execute();
 *
 * // Find tokens by custom type from a plugin
 * Collection<Token> sidekicks = tokenRegistry.query()
 *     .withTypeId("dnd5e:sidekick")
 *     .execute();
 * }</pre>
 */
public interface TokenQuery {

    // ==================== Type Filters ====================

    /**
     * Filters to tokens of the specified type(s).
     * Accepts any TokenType implementation (CoreTokenType or custom).
     *
     * @param types one or more token types
     * @return this query for chaining
     */
    TokenQuery withType(TokenType... types);

    /**
     * Filters to tokens matching the specified type ID(s).
     * Useful when you don't have the TokenType instance.
     *
     * @param typeIds one or more type IDs (e.g., "core:npc", "dnd5e:sidekick")
     * @return this query for chaining
     */
    TokenQuery withTypeId(String... typeIds);

    /**
     * Filters to tokens from a specific namespace.
     *
     * @param namespace the type namespace (e.g., "core", "dnd5e")
     * @return this query for chaining
     */
    TokenQuery withTypeNamespace(String namespace);

    // ==================== Behavior Filters ====================

    /**
     * Filters to tokens that have the specified behavior.
     *
     * @param behaviorId the behavior ID
     * @return this query for chaining
     */
    TokenQuery withBehavior(String behaviorId);

    /**
     * Filters to tokens that have the specified behavior class.
     *
     * @param behaviorClass the behavior class
     * @return this query for chaining
     */
    TokenQuery withBehavior(Class<? extends Behavior> behaviorClass);

    /**
     * Filters to tokens that have ALL of the specified behaviors.
     *
     * @param behaviorIds the behavior IDs
     * @return this query for chaining
     */
    TokenQuery withAllBehaviors(String... behaviorIds);

    /**
     * Filters to tokens that have ANY of the specified behaviors.
     *
     * @param behaviorIds the behavior IDs
     * @return this query for chaining
     */
    TokenQuery withAnyBehavior(String... behaviorIds);

    // ==================== Location Filters ====================

    /**
     * Filters to tokens in the specified world.
     *
     * @param worldId the world's UUID
     * @return this query for chaining
     */
    TokenQuery inWorld(UUID worldId);

    /**
     * Filters to tokens within a certain distance of a position.
     *
     * @param center the center position
     * @param radius the maximum distance
     * @return this query for chaining
     */
    TokenQuery withinRadius(TokenPosition center, double radius);

    /**
     * Filters to tokens that have a position (are placed on the map).
     *
     * @return this query for chaining
     */
    TokenQuery withPosition();

    /**
     * Filters to tokens that don't have a position (not on map).
     *
     * @return this query for chaining
     */
    TokenQuery withoutPosition();

    // ==================== Component Filters ====================

    /**
     * Filters to tokens that have the specified component type.
     *
     * @param componentType the component class
     * @return this query for chaining
     */
    TokenQuery withComponent(Class<? extends TokenComponent> componentType);

    /**
     * Filters to tokens that have ALL of the specified component types.
     *
     * @param componentTypes the component classes
     * @return this query for chaining
     */
    TokenQuery withAllComponents(Class<? extends TokenComponent>... componentTypes);

    /**
     * Filters to tokens that have ANY of the specified component types.
     *
     * @param componentTypes the component classes
     * @return this query for chaining
     */
    TokenQuery withAnyComponent(Class<? extends TokenComponent>... componentTypes);

    /**
     * Filters to tokens from a specific game system namespace.
     * Checks if the token has any component with the given namespace.
     *
     * @param namespace the component namespace (e.g., "dnd5e", "daggerheart")
     * @return this query for chaining
     */
    TokenQuery withComponentNamespace(String namespace);

    // ==================== Ownership Filters ====================

    /**
     * Filters to tokens owned by the specified player.
     *
     * @param ownerId the owner's UUID
     * @return this query for chaining
     */
    TokenQuery ownedBy(UUID ownerId);

    /**
     * Filters to tokens with no owner.
     *
     * @return this query for chaining
     */
    TokenQuery unowned();

    // ==================== Tag Filters ====================

    /**
     * Filters to tokens that have the specified tag.
     *
     * @param tag the tag
     * @return this query for chaining
     */
    TokenQuery withTag(String tag);

    /**
     * Filters to tokens that have ALL of the specified tags.
     *
     * @param tags the tags
     * @return this query for chaining
     */
    TokenQuery withAllTags(String... tags);

    /**
     * Filters to tokens that have ANY of the specified tags.
     *
     * @param tags the tags
     * @return this query for chaining
     */
    TokenQuery withAnyTag(String... tags);

    // ==================== Name Filters ====================

    /**
     * Filters to tokens with the exact name (case-insensitive).
     *
     * @param name the name to match
     * @return this query for chaining
     */
    TokenQuery withName(String name);

    /**
     * Filters to tokens whose name contains the given string (case-insensitive).
     *
     * @param substring the substring to search for
     * @return this query for chaining
     */
    TokenQuery nameContains(String substring);

    // ==================== Entity Binding Filters ====================

    /**
     * Filters to tokens that are bound to a Hytale entity.
     *
     * @return this query for chaining
     */
    TokenQuery boundToEntity();

    /**
     * Filters to tokens that are NOT bound to a Hytale entity.
     *
     * @return this query for chaining
     */
    TokenQuery notBoundToEntity();

    // ==================== Custom Filters ====================

    /**
     * Filters using a custom predicate.
     * <p>
     * This is the most flexible option for complex filtering logic.
     *
     * @param predicate the filter predicate
     * @return this query for chaining
     */
    TokenQuery where(Predicate<Token> predicate);

    // ==================== Sorting ====================

    /**
     * Sorts results by name (ascending).
     *
     * @return this query for chaining
     */
    TokenQuery sortByName();

    /**
     * Sorts results by creation time (oldest first).
     *
     * @return this query for chaining
     */
    TokenQuery sortByCreatedAt();

    /**
     * Sorts results by last modified time (most recent first).
     *
     * @return this query for chaining
     */
    TokenQuery sortByLastModified();

    /**
     * Sorts results by distance from a position (closest first).
     *
     * @param center the reference position
     * @return this query for chaining
     */
    TokenQuery sortByDistanceFrom(TokenPosition center);

    // ==================== Limiting ====================

    /**
     * Limits the number of results returned.
     *
     * @param maxResults the maximum number of results
     * @return this query for chaining
     */
    TokenQuery limit(int maxResults);

    /**
     * Skips the first N results.
     *
     * @param count the number of results to skip
     * @return this query for chaining
     */
    TokenQuery skip(int count);

    // ==================== Execution ====================

    /**
     * Executes the query and returns all matching tokens.
     *
     * @return a collection of matching tokens
     */
    Collection<Token> execute();

    /**
     * Executes the query and returns the first matching token.
     *
     * @return an Optional containing the first match, or empty
     */
    Optional<Token> findFirst();

    /**
     * Executes the query and returns the count of matching tokens.
     *
     * @return the number of matching tokens
     */
    int count();

    /**
     * Executes the query and checks if any tokens match.
     *
     * @return true if at least one token matches
     */
    boolean exists();
}
