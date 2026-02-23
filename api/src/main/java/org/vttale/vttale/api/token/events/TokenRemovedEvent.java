package org.vttale.vttale.api.token.events;

import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.token.Token;
import org.vttale.vttale.api.token.TokenType;

import java.util.Optional;
import java.util.UUID;

/**
 * Event published when a token is removed from the registry.
 * <p>
 * This event is published before the token is actually removed,
 * so listeners can still access the token's data.
 * <p>
 * Note: The token will be invalid after this event completes.
 * Do not store references to the token object.
 * <p>
 * Example:
 * <pre>{@code
 * eventBus.subscribe(TokenRemovedEvent.class, (event, ctx) -> {
 *     logger.info("Token {} was removed", event.getTokenName());
 *
 *     // Clean up any references to this token
 *     myTokenCache.remove(event.getTokenId());
 * });
 * }</pre>
 */
public class TokenRemovedEvent implements Event {

    private final UUID tokenId;
    private final String tokenName;
    private final TokenType tokenType;
    private final UUID boundEntityId;
    private final Token token; // May be null if already removed

    /**
     * Creates a token removed event.
     *
     * @param token the token being removed
     */
    public TokenRemovedEvent(Token token) {
        this.tokenId = token.getId();
        this.tokenName = token.getName();
        this.tokenType = token.getType();
        this.boundEntityId = token.getBoundEntityId().orElse(null);
        this.token = token;
    }

    /**
     * Creates a token removed event with just the ID (when token is already gone).
     *
     * @param tokenId   the token's UUID
     * @param tokenName the token's name
     * @param tokenType the token's type
     */
    public TokenRemovedEvent(UUID tokenId, String tokenName, TokenType tokenType) {
        this.tokenId = tokenId;
        this.tokenName = tokenName;
        this.tokenType = tokenType;
        this.boundEntityId = null;
        this.token = null;
    }

    /**
     * Returns the UUID of the removed token.
     *
     * @return the token ID
     */
    public UUID getTokenId() {
        return tokenId;
    }

    /**
     * Returns the name of the removed token.
     *
     * @return the token name
     */
    public String getTokenName() {
        return tokenName;
    }

    /**
     * Returns the type of the removed token.
     *
     * @return the token type
     */
    public TokenType getTokenType() {
        return tokenType;
    }

    /**
     * Returns the entity this token was bound to, if any.
     *
     * @return an Optional containing the entity UUID
     */
    public Optional<UUID> getBoundEntityId() {
        return Optional.ofNullable(boundEntityId);
    }

    /**
     * Returns the token object if still available.
     * <p>
     * Warning: The token may be in an invalid state. Use with caution.
     *
     * @return an Optional containing the token
     */
    public Optional<Token> getToken() {
        return Optional.ofNullable(token);
    }

    /**
     * Checks if this token was bound to an entity.
     *
     * @return true if the token had an entity binding
     */
    public boolean wasBoundToEntity() {
        return boundEntityId != null;
    }

    @Override
    public String toString() {
        return "TokenRemovedEvent{id=" + tokenId + ", name=" + tokenName + ", type=" + tokenType + "}";
    }
}
