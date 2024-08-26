package RealisticCombat.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import RealisticCombat.renderers.CombatRenderer;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.DrawQueue;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static RealisticCombat.util.DrawUtils.drawCircle;

public final class Radar extends BaseEveryFrameCombatPlugin {

    private static final float padding = 250f;

    private boolean initialized = false, keyDown = false;

    private int zoomLevel;

    private float timeSinceLastUpdateFrame = 9999f, renderRadius, sightRadius, radarScaling,
                  currentZoom, intendedZoom;

    private Vector2f renderCenter;

    private final List<CombatRenderer> renderers = new ArrayList<>();

    private CombatRadarInfo radarInfo;


    private void setZoomLevel(final int zoom) {
        intendedZoom = zoom / (float) RealisticCombat.settings.Radar.getNumZoomLevels();
        if (zoomLevel == 0) currentZoom = intendedZoom;
        zoomLevel = zoom;
    }

    private void checkInit() {
        if (initialized) return;
        initialized = true;
        renderRadius = RealisticCombat.settings.Radar.getRadarRenderRadius();
        renderCenter = new Vector2f(Display.getWidth() - renderRadius, renderRadius);
        setZoomLevel(RealisticCombat.settings.Radar.getNumZoomLevels());
        currentZoom = intendedZoom;

        DrawQueue.releaseDeadQueues();
        renderers.clear(); // Needed due to a .6.2a bug
        radarInfo = new CombatRadarInfo();
        for (Class<? extends CombatRenderer> rendererClass
                : RealisticCombat.settings.Radar.getCombatRendererClasses()) {
            try {
                CombatRenderer renderer = rendererClass.newInstance();
                renderers.add(renderer);
                renderer.init(radarInfo);
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void checkInput() {
        final boolean zoomIn = Keyboard.isKeyDown(RealisticCombat.settings.Radar.getZoomInKey()),
                      zoomOut = Keyboard.isKeyDown(RealisticCombat.settings.Radar.getZoomOutKey());
        if (zoomIn) {
            if (!keyDown) {
                if (zoomLevel == 1) setZoomLevel(RealisticCombat.settings.Radar.getNumZoomLevels());
                else setZoomLevel(zoomLevel - 1);
                keyDown = true;
            }
        } else if (zoomOut) {
            if (!keyDown) {
                if (zoomLevel == RealisticCombat.settings.Radar.getNumZoomLevels()) setZoomLevel(1);
                else setZoomLevel(zoomLevel + 1);
                keyDown = true;
            }
        } else keyDown = false;
    }

    private void advanceZoom(final float amount) {
        // Gradually zoom towards actual zoom level
        final float animationSpeed = amount
                                      * RealisticCombat.settings.Radar.getNumZoomLevels()
                                      / RealisticCombat.settings.Radar.getZoomAnimationDuration();
        if (currentZoom < intendedZoom)
            currentZoom = Math.min(intendedZoom, currentZoom + animationSpeed);
        else if (currentZoom > intendedZoom)
            currentZoom = Math.max(intendedZoom, currentZoom - animationSpeed);
        // Calculate zoom effect on radar elements
        sightRadius = RealisticCombat.settings.Radar.getMaxCombatSightRange() * currentZoom;
        radarScaling = renderRadius / sightRadius;
    }

    private void render(final float amount) {
        boolean isUpdateFrame = false;
        timeSinceLastUpdateFrame += amount;
        if (timeSinceLastUpdateFrame > RealisticCombat.settings.Radar.getTimeBetweenUpdateFrames())
        {
            isUpdateFrame = true;
            advanceZoom(timeSinceLastUpdateFrame);
            timeSinceLastUpdateFrame = 0f;
        }

        // Set OpenGL flags
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        radarInfo.resetView();
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef(0.01f, 0.01f, 0);

        // Set up the stencil test
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glColorMask(false, false, false, false);
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        drawCircle(renderCenter.x,
                   renderCenter.y,
                   renderRadius,
                   RealisticCombat.settings.Radar.getVerticesPerCircle(),
                true);
        glColorMask(true, true, true, true);
        radarInfo.disableStencilTest();

        // Draw the radar elements individually
        for (CombatRenderer renderer : renderers)
            renderer.render(Global.getCombatEngine().getPlayerShip(), amount, isUpdateFrame);

        // Finalize drawing
        glDisable(GL_BLEND);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
    }

    @Override
    public void renderInUICoords(final ViewportAPI viewport) {
        // Zoom 0 = radar disabled
        if (!(RealisticCombat.settings.Radar.isEnabled() && Indication.isVisible())
              || zoomLevel == 0) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        render(engine.getElapsedInLastFrame() / engine.getTimeMult().getModifiedValue());
    }

    @Override
    public void advance(final float amount, final List<InputEventAPI> events) {
        // This also acts as a main menu check
        checkInit();
        checkInput();
    }

    @Override
    public void init(final CombatEngineAPI combatEngine) { initialized = false; }

    private class CombatRadarInfo implements CommonRadar<CombatEntityAPI> {
        @Override
        public void resetView() {
            // Retina display fix
            final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor()),
                      height = (int) (Display.getHeight() * Display.getPixelScaleFactor());
            glViewport(0, 0, width, height);
            glOrtho(0, width, 0, height, -1, 1);
        }

        @Override
        public void enableStencilTest() {
            glEnable(GL_STENCIL_TEST);
            glStencilFunc(GL_EQUAL, 1, 1);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        @Override
        public void disableStencilTest() {
            glDisable(GL_STENCIL_TEST);
        }

        @Override
        public Vector2f getRenderCenter() {
            return renderCenter;
        }

        @Override
        public float getRenderRadius() {
            return renderRadius;
        }

        @Override
        public float getCurrentPixelsPerSU() {
            return radarScaling;
        }

        @Override
        public float getCurrentZoomLevel() {
            return RealisticCombat.settings.Radar.getNumZoomLevels() / (float) zoomLevel;
        }

        @Override
        public float getCurrentSightRadius() {
            return sightRadius;
        }

        @Override
        public float getRadarAlpha() {
            return Colors.getAlpha(Global.getCombatEngine().getViewport().getViewMult());
        }

        @Override
        public float getContactAlpha() { return RealisticCombat.settings.Radar.getRadarContactAlpha(); }

        @Override
        public java.awt.Color getFriendlyContactColor() {
            return Colors.getFriendly();
        }

        @Override
        public java.awt.Color getEnemyContactColor() {
            return Colors.getEnemy();
        }

        @Override
        public java.awt.Color getNeutralContactColor() {
            return Colors.getNeutral();
        }

        @Override
        public java.awt.Color getAlliedContactColor() { return Colors.getAlly(); }

        @Override
        public boolean isPointOnRadar(final Vector2f worldLoc, final float padding) {
            return isPointOnRadar(worldLoc.getX(), worldLoc.getY(), padding);
        }

        @Override
        public boolean isPointOnRadar(final float worldLocX, final float worldLocY, float sweep) {
            final float x = worldLocX - Global.getCombatEngine().getPlayerShip().getLocation().x,
                        y = worldLocY - Global.getCombatEngine().getPlayerShip().getLocation().y;
            return x * x + y * y < sweep * sweep;
        }

        @Override
        public Vector2f getPointOnRadar(final Vector2f worldLoc) {
            final float[] loc = getRawPointOnRadar(worldLoc);
            return new Vector2f(loc[0], loc[1]);
        }

        @Override
        public float[] getRawPointOnRadar(final Vector2f worldLoc) {
            return getRawPointOnRadar(worldLoc.x, worldLoc.y);
        }

        @Override
        public float[] getRawPointOnRadar(final float worldX, final float worldY) {
            // Get position relative to {0,0}
            // Scale point to fit within the radar properly
            // Translate point to inside the radar box
            final ShipAPI player = Global.getCombatEngine().getPlayerShip();
            return new float[] {
                    ((worldX - player.getLocation().x) * radarScaling) + renderCenter.x,
                    ((worldY - player.getLocation().y) * radarScaling) + renderCenter.y
            };
        }

        @Override
        public float[] getRawPointsOnRadar(final float[] worldCoords) {
            if ((worldCoords.length & 1) != 0)
                throw new RuntimeException("Coordinates must be in x,y pairs!");
            final float[] coords = new float[worldCoords.length];
            final float playerX = Global.getCombatEngine().getPlayerShip().getLocation().getX(),
                        playerY = Global.getCombatEngine().getPlayerShip().getLocation().getY();
            for (int i = 0; i < worldCoords.length; i += 2) {
                // Get position relative to {0,0}
                // Scale point to fit within the radar properly
                // Translate point to inside the radar box
                coords[i] = ((worldCoords[i] - playerX) * radarScaling) + renderCenter.x;
                coords[i + 1] = ((worldCoords[i + 1] - playerY) * radarScaling) + renderCenter.y;
            } return coords;
        }

        @Override
        public List<CombatEntityAPI> filterVisible(final List contacts, final int maxContacts) {
            final List<CombatEntityAPI> visible = new ArrayList<>();
            final float sweep = sightRadius + padding;
            for (Object tmp : contacts) {
                // Limit maximum contacts displayed
                if (maxContacts >= 0 && visible.size() >= maxContacts) break;
                CombatEntityAPI contact = (CombatEntityAPI) tmp;
                // Reveal not what the fog of war hides
                if (!Global.getCombatEngine().getFogOfWar(0).isVisible(contact)) continue;
                // If any part of the contact be visible
                if (isPointOnRadar(contact.getLocation(), sweep)) visible.add(contact);
            } return visible;
        }
    }
}
