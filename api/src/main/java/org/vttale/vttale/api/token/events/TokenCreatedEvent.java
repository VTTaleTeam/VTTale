package org.vttale.vttale.api.token.events;

import org.vttale.vttale.api.events.Event;
import org.vttale.vttale.api.token.Token;
import org.vttale.vttale.api.token.TokenType;

import java.util.UUID;

/**
 * Event published when a new token is created.
 * <p>
 * Game systems should listen to this event to add their components
 * to newly created tokens.
 * <p>
 * Example:
 * <pre>{@code
 * eventBus.subscribe(TokenCreatedEvent.class, (event, ctx) -> {
 *     Token token = event.getToken();
 *     if (token.getType() == TokenType.PLAYER_CHARACTER) {
 *         token.setComponent(new DnD5eStatsComponent());
 *     }
 * });
 * }</pre>
 */
public class TokenCreatedEvent implements Event {

    private final Token token;

    public TokenCreatedEvent(Token token) {
        this.token = token;
    }

    /**
     * Returns the newly created token.
     * <p>
     * The token is mutable - listeners can add components or modify it.
     *
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * Returns the token's UUID.
     *
     * @return the token ID
     */
    public UUID getTokenId() {
        return token.getId();
    }

    /**
     * Returns the token's type.
     *
     * @return the token type
     */
    public TokenType getTokenType() {
        return token.getType();
    }

    /**
     * Returns the token's name.
     *
     * @return the token name
     */
    public String getTokenName() {
        return token.getName();
    }

    @Override
    public String toString() {
        return "TokenCreatedEvent{token=" + token.getName() + ", type=" + token.getType() + "}";
    }
}
