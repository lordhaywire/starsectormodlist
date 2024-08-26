package RealisticCombat.util;

/**
 * Provides methods to calculate vertices of common shapes. Equivalent
 * algorithms to LazyLib's {@link DrawUtils}, but returns the raw vertices
 * instead of drawing them for you.
 *
 * @author LazyWizard
 * @since 2.0
 */
public final class ShapeUtils {

    /**
     * Creates the vertices for a simple circle.
     * <p>
     * Optimized circle-drawing algorithm based on code taken from:
     * <a href=http://slabode.exofire.net/circle_draw.shtml>
     * http://slabode.exofire.net/circle_draw.shtml</a>
     *
     * @param centerX {@code float} x value of the center point
     * @param centerY {@code float} y value of the center point
     * @param radius {@code float} radius of the circle to be drawn
     * @param segments {@code int} How many line segments the circle should
     *                                 comprise (higher -> smoother)
     * <p>
     * @return The vertices needed to draw a circle with the given parameters.
     * <p>
     * @since 2.0
     */
    public static float[] createCircle(final float centerX,
                                       final float centerY,
                                       final float radius,
                                       final int segments)
    {
        // Precalculate the sine and cosine
        // Instead of recalculating sin/cos for each line segment,
        // this algorithm rotates the line around the center point
        final float theta = 2f * 3.1415926f / segments,
                    cos = (float) Math.cos(theta),
                    sin = (float) Math.sin(theta);

        // Start at angle = 0
        float x = radius, y = 0;
        float tmp;

        float[] vertices = new float[segments * 2];
        for (int i = 0; i < vertices.length; i += 2) {
            // Output vertex
            vertices[i] = x + centerX;
            vertices[i + 1] = y + centerY;
            // Apply the rotation matrix
            tmp = x;
            x = (cos * x) - (sin * y); y = (sin * tmp) + (cos * y);
        }
        return vertices;
    }

    private ShapeUtils() {}
}
