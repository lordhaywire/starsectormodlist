package RealisticCombat.renderers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;
import RealisticCombat.plugins.CommonRadar;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.DrawQueue;
import RealisticCombat.util.SpriteBatch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP;
import static org.lwjgl.opengl.GL15.GL_SRC0_RGB;

public final class ShipRenderer implements CombatRenderer {

    private static final int MAX_SHIPS_SHOWN = 100, MAX_SHIELD_SEGMENTS = 64;

    private static final java.awt.Color
            SHIELD_COLOR = new java.awt.Color(0,255,255,200),
            MARKER_COLOR = new java.awt.Color(255,255,255,255);

    private static final float
            FIGHTER_SIZE_MOD = 0.8f,
            MIN_FIGHTER_SIZE = 6,
            MIN_SHIP_SIZE = 8,
            MIN_SHIP_ALPHA_MULT = 0.5f;

    private Map<Integer, SpriteBatch> shipBatches;

    private DrawQueue drawQueue;

    private CommonRadar<CombatEntityAPI> radar;


    private static float minSize(final ShipAPI ship) {
        return ship.isFighter() ? MIN_FIGHTER_SIZE : MIN_SHIP_SIZE;
    }

    public static float getSizeModifier(final ShipAPI ship,
                                        final CommonRadar<CombatEntityAPI> radar)
    {
        final float base = ship.getCollisionRadius() * radar.getCurrentPixelsPerSU(),
                    adjusted = Math.max(!ship.isFighter() ? base
                                                          : base * FIGHTER_SIZE_MOD, minSize(ship));
        return adjusted / base;
    }

    public static float getContactRadius(final ShipAPI ship,
                                         final CommonRadar<CombatEntityAPI> radar)
    {
        return ship.getCollisionRadius()
               * getSizeModifier(ship, radar)
               * radar.getCurrentPixelsPerSU();
    }

    public static float getShieldRadius(final ShipAPI ship,
                                        final CommonRadar<CombatEntityAPI> radar)
    {
        return (ship.getShield() == null) ? 0f : ship.getShield().getRadius()
                                                 * getSizeModifier(ship, radar)
                                                 * radar.getCurrentPixelsPerSU();
    }

    @Override
    public void init(final CommonRadar<CombatEntityAPI> radar) {
        this.radar = radar;
        drawQueue = new DrawQueue(8 + MAX_SHIPS_SHOWN * 2 * (MAX_SHIELD_SEGMENTS + 4));
        shipBatches = new LinkedHashMap<>();
    }

    private void addShieldToBuffer(final ShipAPI contact) {
        final ShieldAPI shield = contact.getShield();
        if (shield == null || !shield.isOn()) return;
        final int numSegments = (int) (MAX_SHIELD_SEGMENTS / (360f / shield.getActiveArc()) + 0.5f);
        if (numSegments < 1) return;
        final float[] radarLoc = radar.getRawPointOnRadar(shield.getLocation());
        final float size = getShieldRadius(contact, radar),
                    startAngle = (float) Math.toRadians(shield.getFacing()
                                                        - (shield.getActiveArc() / 2f)),
                    arcAngle = (float) Math.toRadians(shield.getActiveArc());

        // Precalculate the sine and cosine
        // Instead of recalculating sin/cos for each line segment,
        // this algorithm rotates the line around the center point
        final float theta = arcAngle / numSegments,
                    cos = (float) Math.cos(theta),
                    sin = (float) Math.sin(theta);

        // Start at angle startAngle
        float x = (float) (size * Math.cos(startAngle)), y = (float) (size * Math.sin(startAngle)),
              tmp;

        final float[] vertices = new float[numSegments * 2 + 4];
        vertices[0] = radarLoc[0]; vertices[1] = radarLoc[1];
        for (int i = 2; i < vertices.length - 2; i += 2) {
            // Output vertex
            vertices[i] = x + radarLoc[0];
            vertices[i + 1] = y + radarLoc[1];

            // Apply the rotation matrix
            tmp = x;
            x = (cos * x) - (sin * y); y = (sin * tmp) + (cos * y);
        }
        vertices[vertices.length - 2] = x + radarLoc[0];
        vertices[vertices.length - 1] = y + radarLoc[1];

        // Add vertices to master vertex map
        drawQueue.addVertices(vertices);
        drawQueue.finishShape(GL_TRIANGLE_FAN);
    }

    private void addTargetMarker(final ShipAPI target) {
        // Generate vertices
        final float size = getContactRadius(target, radar), margin = size * .5f;
        final Vector2f radarLoc = radar.getPointOnRadar(target.getLocation());
        final float[] vertices = new float[] {
            // Upper left corner
            radarLoc.x - size, radarLoc.y + size, // 0
            radarLoc.x - margin, radarLoc.y + size, // 1
            radarLoc.x - size, radarLoc.y + size, // 0
            radarLoc.x - size, radarLoc.y + margin, // 2
            // Upper right corner
            radarLoc.x + size, radarLoc.y + size, // 3
            radarLoc.x + margin, radarLoc.y + size, // 4
            radarLoc.x + size, radarLoc.y + size, // 3
            radarLoc.x + size, radarLoc.y + margin, // 5
            // Lower left corner
            radarLoc.x - size, radarLoc.y - size, // 6
            radarLoc.x - margin, radarLoc.y - size, // 7
            radarLoc.x - size, radarLoc.y - size, // 6
            radarLoc.x - size, radarLoc.y - margin, // 8
            // Lower right corner
            radarLoc.x + size, radarLoc.y - size, // 9
            radarLoc.x + margin, radarLoc.y - size, // 10
            radarLoc.x + size, radarLoc.y - size, // 9
            radarLoc.x + size, radarLoc.y - margin  // 11
        };

        drawQueue.setNextColor(MARKER_COLOR, radar.getContactAlpha());
        drawQueue.addVertices(vertices);
        drawQueue.finishShape(GL_LINES);
    }

    private float getAlphaMod(final ShipAPI ship) {
        // Fully transparent ships don't appear on the radar
        if (ship.getCombinedAlphaMult() <= 0f) return 0f;
        // Adjust alpha levels for phasing/fighter takeoff and landing
        return radar.getContactAlpha()
               * Math.max(MIN_SHIP_ALPHA_MULT, 1f - ((1f - ship.getCombinedAlphaMult()) * 2f));
    }

    private void addShip(final ShipAPI ship) {
        final SpriteAPI sprite = ship.getSpriteAPI();
        if (sprite == null) return;

        final int textureId = sprite.getTextureId();
        SpriteBatch batch = shipBatches.get(textureId);
        if (batch == null) {
            batch = new SpriteBatch(sprite, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            shipBatches.put(textureId, batch);
        }

        final float[] loc = radar.getRawPointOnRadar(ship.getLocation());
        batch.add(loc[0],
                  loc[1],
                  ship.getFacing(),
                  ship.getSpriteAPI().getHeight()
                   * getSizeModifier(ship, radar)
                   * radar.getCurrentPixelsPerSU(),
                  Colors.getColor(ship),
                  getAlphaMod(ship));
    }

    @Override
    public void render(final ShipAPI player, final float amount, final boolean isUpdateFrame) {
        if (!player.isAlive()) return;
        if (isUpdateFrame) {
            drawQueue.clear();
            for (SpriteBatch batch : shipBatches.values()) batch.clear();
            final List<ShipAPI> ships =
                    radar.filterVisible(Global.getCombatEngine().getShips(), MAX_SHIPS_SHOWN);
            if (!ships.isEmpty())
                drawQueue.setNextColor(SHIELD_COLOR, radar.getContactAlpha() * 0.5f);
            for (ShipAPI ship : ships) {
                // TODO: Get these to look good (re-add triangulator?)
                if (ship.isPiece()) continue;
                // Draw marker around current ship target
                if (ship == player.getShipTarget()) addTargetMarker(ship);
                addShip(ship);
                addShieldToBuffer(ship);
            }
            drawQueue.finish();
            for (SpriteBatch batch : shipBatches.values()) batch.finish();
        }

        // Draw cached render data
        radar.enableStencilTest();
        glEnable(GL_BLEND);

        if (!drawQueue.isEmpty()) {
            glEnableClientState(GL_VERTEX_ARRAY);
            glEnableClientState(GL_COLOR_ARRAY);
            glEnable(GL_POLYGON_SMOOTH);
            drawQueue.draw();
            glDisable(GL_POLYGON_SMOOTH);
            glDisableClientState(GL_COLOR_ARRAY);
            glDisableClientState(GL_VERTEX_ARRAY);
        }

        glEnable(GL_TEXTURE_2D);

        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
        glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);

        for (SpriteBatch toDraw : shipBatches.values()) toDraw.draw();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        radar.disableStencilTest();
    }
}
