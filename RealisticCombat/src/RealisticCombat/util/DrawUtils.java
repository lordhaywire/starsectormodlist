package RealisticCombat.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static RealisticCombat.util.ShapeUtils.createCircle;


public final class DrawUtils {
    /**
     * Draws a simple circle made of line segments, or a filled circle if
     * {@code drawFilled} is true.
     * <p>
     * This method only contains the actual drawing code and assumes all OpenGL
     * flags, color, line width etc have been set by the user beforehand.
     * <p>
     * Optimized circle-drawing algorithm based on code taken from:
     * <a href=http://slabode.exofire.net/circle_draw.shtml>
     * http://slabode.exofire.net/circle_draw.shtml</a>
     * <p></p>
     * @param centerX     The x value of the center point of the circle.
     * @param centerY     The y value of the center point of the circle.
     * @param radius      The radius of the circle to be drawn.
     * @param numSegments How many line segments the circle should be made up
     *                    of (higher number = smoother circle, but higher GPU
     *                    cost).
     * @param drawFilled  Whether the circle should be hollow or filled.
     *
     * Adapted from LazyLib
     */
    public static void drawCircle(final float centerX,
                                  final float centerY,
                                  final float radius,
                                  final int numSegments,
                                  final boolean drawFilled)
    {
        if (numSegments < 3) return;
        float[] circle = createCircle(centerX, centerY, radius, numSegments);
        FloatBuffer vertexMap = BufferUtils.createFloatBuffer(circle.length);
        vertexMap.put(circle).flip();
        glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, 0, vertexMap);
        glDrawArrays(drawFilled ? GL_TRIANGLE_FAN : GL_LINE_LOOP, 0, circle.length / 2);
        glPopClientAttrib();
    }

    public static void drawTriangle(final Vector2f[] triangle) {
        GL11.glBegin(GL11.GL_POLYGON);
        for (final Vector2f point : triangle) GL11.glVertex2f(point.getX(), point.getY());
        GL11.glEnd();
    }
}
