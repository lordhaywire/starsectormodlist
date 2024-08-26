package RealisticCombat.util;

import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * OpenGL11 boilerplate method calls.
 *
 * @author Liral
 */
public final class OpenGLUtils {

    public static void glColor(final Color color) {
        GL11.glColor4ub((byte) color.getRed(),
                        (byte) color.getGreen(),
                        (byte) color.getBlue(),
                        (byte) (color.getAlpha()));
    }

    /**
     * Tells GL11 to render everything after this method call in this color.
     * <p></p>
     * @param color {@link Color}
     * @param alphaMult {@code float}
     */
    public static void glColor(final Color color, final float alphaMult) {
        GL11.glColor4ub((byte) color.getRed(),
                        (byte) color.getGreen(),
                        (byte) color.getBlue(),
                        (byte) (color.getAlpha() * alphaMult));
    }

    /**
     * Prepare OpenGL11 to render what you request during this frame.
     *
     * Must be called before any rendering calls to OpenGL11.
     */
    public static void setupRendering() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);
    }

    /**
     * Finalize all the rendering you told OpenGL11 to do this frame.
     *
     * Must be called after OpenGL11 is done rendering, or else the rendering
     * will be very wrong.
     */
    public static void finishRendering() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}