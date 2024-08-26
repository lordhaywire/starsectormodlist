package RealisticCombat.calculation;

import org.lwjgl.util.vector.Vector2f;

public final class Vector {

    public static Vector2f sum(final Vector2f a, final Vector2f b) {
        return Vector2f.add(a, b, new Vector2f());
    }

    public static Vector2f difference(final Vector2f a, final Vector2f b) {
        return Vector2f.sub(a, b, new Vector2f());
    }

    public static Vector2f scalarProduct(final float scalar, final Vector2f vector) {
        return new Vector2f(scalar * vector.x, scalar * vector.y);
    }

    public static Vector2f reflection(final Vector2f v, final Vector2f n) {
        return Vector2f.sub(scalarProduct(2 * Vector2f.dot(v, n), n), v, new Vector2f());
    }

    private static final Vector2f TEMP_VECTOR = new Vector2f();

    /**
     * Rotates a {@link Vector2f} by a specified amount and stores the result in a destination vector.
     *
     * @param toRotate The {@link Vector2f} to rotate. Will not be modified; instead the result will be placed in {@code
     *                 dest}.
     * @param angle    How much to rotate the destination vector, in degrees.
     * @param dest     The destination {@link Vector2f}. Can be {@code toRotate}.
     *
     * @return {@code dest}, rotated based on {@code toRotate}, returned for easier chaining of methods.
     *
     * @since 1.7
     */
    public static Vector2f rotate(final Vector2f toRotate, float angle, final Vector2f dest) {
        if (angle == 0f) return dest.set(toRotate);

        angle = (float) Math.toRadians(angle);
        final float cos = (float) FastTrig.cos(angle), sin = (float) FastTrig.sin(angle);
        dest.set((toRotate.x * cos) - (toRotate.y * sin),
                 (toRotate.x * sin) + (toRotate.y * cos));
        return dest;
    }

    /**
     * Rotates a {@link Vector2f} by a specified amount around a pivot point and stores the result in a destination
     * vector.
     *
     * @param toRotate   The {@link Vector2f} to rotate. Will not be modified; instead the result will be placed in
     *                   {@code dest}.
     * @param pivotPoint The central point to pivot around.
     * @param angle      How much to rotate the destination vector, in degrees.
     * @param dest       The destination {@link Vector2f}. Can be {@code toRotate}.
     *
     * @return {@code dest}, rotated based on {@code toRotate} around {@code pivotPoint}, returned for easier chaining
     *         of methods.
     *
     * @since 1.7
     */
    public static Vector2f rotateAroundPivot(final Vector2f toRotate,
                                             final Vector2f pivotPoint,
                                             final float angle,
                                             final Vector2f dest)
    {
        if (angle == 0f) return dest.set(toRotate);

        Vector2f.sub(toRotate, pivotPoint, TEMP_VECTOR);
        rotate(TEMP_VECTOR, angle, TEMP_VECTOR);
        Vector2f.add(TEMP_VECTOR, pivotPoint, dest);
        return dest;
    }
}
