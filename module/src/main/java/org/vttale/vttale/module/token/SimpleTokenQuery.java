package org.vttale.vttale.module.token;

import org.vttale.vttale.api.token.*;
import org.vttale.vttale.api.token.behavior.Behavior;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the TokenQuery interface.
 * <p>
 * Uses a stream-based approach for lazy evaluation and efficient filtering.
 */
public class SimpleTokenQuery implements TokenQuery {

    private final Collection<Token> tokens;
    private final List<Predicate<Token>> filters;
    private Comparator<Token> sorter;
    private int limitValue = -1;
    private int skipValue = 0;

    /**
     * Creates a new query over the given token collection.
     *
     * @param tokens the tokens to query
     */
    public SimpleTokenQuery(Collection<Token> tokens) {
        this.tokens = tokens;
        this.filters = new ArrayList<>();
    }

    // ==================== Type Filters ====================

    @Override
    public TokenQuery withType(TokenType... types) {
        if (types.length == 0) return this;
        Set<String> typeIds = Arrays.stream(types)
                .map(TokenType::getId)
                .collect(Collectors.toSet());
        filters.add(token -> typeIds.contains(token.getType().getId()));
        return this;
    }

    @Override
    public TokenQuery withTypeId(String... typeIds) {
        if (typeIds.length == 0) return this;
        Set<String> idSet = Set.of(typeIds);
        filters.add(token -> idSet.contains(token.getType().getId()));
        return this;
    }

    @Override
    public TokenQuery withTypeNamespace(String namespace) {
        filters.add(token -> token.getType().getNamespace().equals(namespace));
        return this;
    }

    // ==================== Behavior Filters ====================

    @Override
    public TokenQuery withBehavior(String behaviorId) {
        filters.add(token -> token.hasBehavior(behaviorId));
        return this;
    }

    @Override
    public TokenQuery withBehavior(Class<? extends Behavior> behaviorClass) {
        filters.add(token -> token.hasBehavior(behaviorClass));
        return this;
    }

    @Override
    public TokenQuery withAllBehaviors(String... behaviorIds) {
        for (String id : behaviorIds) {
            filters.add(token -> token.hasBehavior(id));
        }
        return this;
    }

    @Override
    public TokenQuery withAnyBehavior(String... behaviorIds) {
        if (behaviorIds.length == 0) return this;
        filters.add(token -> {
            for (String id : behaviorIds) {
                if (token.hasBehavior(id)) return true;
            }
            return false;
        });
        return this;
    }

    // ==================== Location Filters ====================

    @Override
    public TokenQuery inWorld(UUID worldId) {
        filters.add(token -> token.getWorldId().map(id -> id.equals(worldId)).orElse(false));
        return this;
    }

    @Override
    public TokenQuery withinRadius(TokenPosition center, double radius) {
        filters.add(token -> token.getPosition()
                .map(pos -> pos.distanceTo(center) <= radius)
                .orElse(false));
        return this;
    }

    @Override
    public TokenQuery withPosition() {
        filters.add(token -> token.getPosition().isPresent());
        return this;
    }

    @Override
    public TokenQuery withoutPosition() {
        filters.add(token -> token.getPosition().isEmpty());
        return this;
    }

    // ==================== Component Filters ====================

    @Override
    public TokenQuery withComponent(Class<? extends TokenComponent> componentType) {
        filters.add(token -> token.hasComponent(componentType));
        return this;
    }

    @Override
    @SafeVarargs
    public final TokenQuery withAllComponents(Class<? extends TokenComponent>... componentTypes) {
        for (Class<? extends TokenComponent> type : componentTypes) {
            filters.add(token -> token.hasComponent(type));
        }
        return this;
    }

    @Override
    @SafeVarargs
    public final TokenQuery withAnyComponent(Class<? extends TokenComponent>... componentTypes) {
        if (componentTypes.length == 0) return this;
        filters.add(token -> {
            for (Class<? extends TokenComponent> type : componentTypes) {
                if (token.hasComponent(type)) return true;
            }
            return false;
        });
        return this;
    }

    @Override
    public TokenQuery withComponentNamespace(String namespace) {
        filters.add(token -> token.getAllComponents().stream()
                .anyMatch(c -> c.getNamespace().equals(namespace)));
        return this;
    }

    // ==================== Ownership Filters ====================

    @Override
    public TokenQuery ownedBy(UUID ownerId) {
        filters.add(token -> token.getOwnerId().map(id -> id.equals(ownerId)).orElse(false));
        return this;
    }

    @Override
    public TokenQuery unowned() {
        filters.add(token -> token.getOwnerId().isEmpty());
        return this;
    }

    // ==================== Tag Filters ====================

    @Override
    public TokenQuery withTag(String tag) {
        filters.add(token -> token.hasTag(tag));
        return this;
    }

    @Override
    public TokenQuery withAllTags(String... tags) {
        for (String tag : tags) {
            filters.add(token -> token.hasTag(tag));
        }
        return this;
    }

    @Override
    public TokenQuery withAnyTag(String... tags) {
        if (tags.length == 0) return this;
        filters.add(token -> {
            for (String tag : tags) {
                if (token.hasTag(tag)) return true;
            }
            return false;
        });
        return this;
    }

    // ==================== Name Filters ====================

    @Override
    public TokenQuery withName(String name) {
        filters.add(token -> token.getName().equalsIgnoreCase(name));
        return this;
    }

    @Override
    public TokenQuery nameContains(String substring) {
        String lowerSubstring = substring.toLowerCase();
        filters.add(token -> token.getName().toLowerCase().contains(lowerSubstring));
        return this;
    }

    // ==================== Entity Binding Filters ====================

    @Override
    public TokenQuery boundToEntity() {
        filters.add(Token::isBoundToEntity);
        return this;
    }

    @Override
    public TokenQuery notBoundToEntity() {
        filters.add(token -> !token.isBoundToEntity());
        return this;
    }

    // ==================== Custom Filters ====================

    @Override
    public TokenQuery where(Predicate<Token> predicate) {
        filters.add(predicate);
        return this;
    }

    // ==================== Sorting ====================

    @Override
    public TokenQuery sortByName() {
        this.sorter = Comparator.comparing(Token::getName, String.CASE_INSENSITIVE_ORDER);
        return this;
    }

    @Override
    public TokenQuery sortByCreatedAt() {
        this.sorter = Comparator.comparingLong(Token::getCreatedAt);
        return this;
    }

    @Override
    public TokenQuery sortByLastModified() {
        this.sorter = Comparator.comparingLong(Token::getLastModifiedAt).reversed();
        return this;
    }

    @Override
    public TokenQuery sortByDistanceFrom(TokenPosition center) {
        this.sorter = Comparator.comparingDouble(token ->
                token.getPosition().map(pos -> pos.distanceTo(center)).orElse(Double.MAX_VALUE));
        return this;
    }

    // ==================== Limiting ====================

    @Override
    public TokenQuery limit(int maxResults) {
        this.limitValue = maxResults;
        return this;
    }

    @Override
    public TokenQuery skip(int count) {
        this.skipValue = count;
        return this;
    }

    // ==================== Execution ====================

    @Override
    public Collection<Token> execute() {
        return buildStream().collect(Collectors.toList());
    }

    @Override
    public Optional<Token> findFirst() {
        return buildStream().findFirst();
    }

    @Override
    public int count() {
        return (int) buildFilteredStream().count();
    }

    @Override
    public boolean exists() {
        return buildFilteredStream().findAny().isPresent();
    }

    // ==================== Private Helpers ====================

    /**
     * Builds a stream with all filters applied.
     */
    private Stream<Token> buildFilteredStream() {
        Stream<Token> stream = tokens.stream();

        // Apply all filters
        for (Predicate<Token> filter : filters) {
            stream = stream.filter(filter);
        }

        return stream;
    }

    /**
     * Builds a stream with filters, sorting, skip, and limit applied.
     */
    private Stream<Token> buildStream() {
        Stream<Token> stream = buildFilteredStream();

        // Apply sorting
        if (sorter != null) {
            stream = stream.sorted(sorter);
        }

        // Apply skip
        if (skipValue > 0) {
            stream = stream.skip(skipValue);
        }

        // Apply limit
        if (limitValue > 0) {
            stream = stream.limit(limitValue);
        }

        return stream;
    }
}
