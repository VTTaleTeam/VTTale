package org.vttale.vttale.api.token;

import java.util.Objects;

/**
 * Represents the spatial position of a token in the world.
 * <p>
 * This is a simple value object that holds x, y, z coordinates and rotation.
 * It's independent from Hytale's Vector3d to keep the API platform-agnostic.
 */
public final class TokenPosition {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;   // Horizontal rotation (0-360)
    private final float pitch; // Vertical rotation (-90 to 90)

    /**
     * Creates a new TokenPosition with the given coordinates and no rotation.
     *
     * @param x the X coordinate
     * @param y the Y coordinate (vertical)
     * @param z the Z coordinate
     */
    public TokenPosition(double x, double y, double z) {
        this(x, y, z, 0f, 0f);
    }

    /**
     * Creates a new TokenPosition with the given coordinates and rotation.
     *
     * @param x     the X coordinate
     * @param y     the Y coordinate (vertical)
     * @param z     the Z coordinate
     * @param yaw   the horizontal rotation in degrees (0-360)
     * @param pitch the vertical rotation in degrees (-90 to 90)
     */
    public TokenPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = normalizeYaw(yaw);
        this.pitch = clampPitch(pitch);
    }

    // ==================== Getters ====================

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    // ==================== Utility Methods ====================

    /**
     * Creates a new position with the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return a new {@link TokenPosition}
     */
    public TokenPosition withCoordinates(double x, double y, double z) {
        return new TokenPosition(x, y, z, this.yaw, this.pitch);
    }

    /**
     * Returns a new TokenPosition with the given rotation.
     *
     * @param Yaw   the yaw
     * @param Pitch the pitch
     * @return a new TokenPosition
     */
    public TokenPosition withRotation(float Yaw, float Pitch) {
        return new TokenPosition(x, y, z, Yaw, Pitch);
    }

    /**
     * Returns a new TokenPosition with the given offset added.
     *
     * @param dx X offset
     * @param dy Y offset
     * @param dz Z offset
     * @return a new {@link TokenPosition}
     */
    public TokenPosition add(double dx, double dy, double dz) {
        return new TokenPosition(x + dx, y + dy, z + dz, yaw, pitch);
    }

    /**
     * Calculates the distance to another position.
     *
     * @param other the other position
     * @return the distance
     */
    public double distanceTo(TokenPosition other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates the horizontal (2D) distance to another position.
     * Ignores the Y coordinate.
     *
     * @param other the other position
     * @return the horizontal distance
     */
    public double horizontalDistanceTo(TokenPosition other) {
        double dx = other.x - this.x;
        double dz = other.z - this.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Returns the block coordinates (floored integers).
     *
     * @return an array of [blockX, blockY, blockZ]
     */
    public int[] toBlockCoordinates() {
        return new int[]{
                (int) Math.floor(x),
                (int) Math.floor(y),
                (int) Math.floor(z)
        };
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a TokenPosition at the origin (0, 0, 0).
     *
     * @return a position at origin
     */
    public static TokenPosition origin() {
        return new TokenPosition(0, 0, 0);
    }

    /**
     * Creates a TokenPosition from block coordinates (centered on block).
     *
     * @param blockX the block X
     * @param blockY the block Y
     * @param blockZ the block Z
     * @return a position centered on the block
     */
    public static TokenPosition fromBlock(int blockX, int blockY, int blockZ) {
        return new TokenPosition(blockX + 0.5, blockY, blockZ + 0.5);
    }

    // ==================== Private Helpers ====================

    private static float normalizeYaw(float yaw) {
        yaw = yaw % 360f;
        if (yaw < 0) yaw += 360f;
        return yaw;
    }

    private static float clampPitch(float pitch) {
        return Math.max(-90f, Math.min(90f, pitch));
    }

    // ==================== Object Methods ====================

    /**
     * Implements equality check based on position and orientation
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPosition that = (TokenPosition) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Float.compare(that.yaw, yaw) == 0 &&
                Float.compare(that.pitch, pitch) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return String.format("TokenPosition{x=%.2f, y=%.2f, z=%.2f, yaw=%.1f, pitch=%.1f}",
                x, y, z, yaw, pitch);
    }
}
