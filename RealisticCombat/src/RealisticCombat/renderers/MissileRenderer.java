package RealisticCombat.renderers;

import RealisticCombat.plugins.CommonRadar;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.SpriteBatch;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;

import org.lwjgl.util.vector.Vector2f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public final class MissileRenderer implements CombatRenderer {

    private boolean playerLock = false;

    private float missileSize, highestThreatAlpha = 0f;

    private Vector2f lockIconLocation;

    private final java.awt.Color MISSILE_LOCKED_COLOR = new java.awt.Color(255,200,0,255);

    private SpriteAPI lockIcon;

    private SpriteBatch missilesToDraw;

    private CommonRadar<CombatEntityAPI> radar;


    @Override
    public void init(final CommonRadar<CombatEntityAPI> radar) {
        this.radar = radar;
        missilesToDraw = new SpriteBatch(Global.getSettings().getSprite("radar", "missile"));
        float missileSizeMod = 0.075f;
        missileSize = radar.getRenderRadius() * missileSizeMod;
        final Vector2f radarCenter = radar.getRenderCenter();
        final float radarRadius = radar.getRenderRadius();
        lockIcon = Global.getSettings().getSprite("radar", "missileLock");
        lockIcon.setColor(MISSILE_LOCKED_COLOR);
        lockIconLocation = new Vector2f(radarCenter.x - (radarRadius * 0.9f),
                                        radarCenter.y - (radarRadius * 0.9f));
    }

    private float getAlphaMod(final MissileAPI missile) {
        return Math.min(1f, Math.max(0.45f, (missile.getDamageAmount()
                                             + (missile.getEmpAmount() / 2f)) / 750f))
               * radar.getContactAlpha()
               * (missile.isFading() ? .5f : 1f);
    }

    private void addMissileToDraw(final ShipAPI player, final MissileAPI missile) {
        float[] radarLoc = radar.getRawPointOnRadar(missile.getLocation()); // Calculate vertices
        float alphaMod = getAlphaMod(missile);
        // Calculate color
        java.awt.Color color;
        // Burnt-out missiles count as hostile
        if (missile.isFizzling()) color = Colors.getEnemy();
        else if (missile.getOwner() + player.getOwner() == 1) { // Enemy missile
            MissileAIPlugin ai = missile.getMissileAI();
            // Color missiles locked onto us differently
            if (ai instanceof GuidedMissileAI && player == ((GuidedMissileAI) ai).getTarget()) {
                playerLock = true;
                highestThreatAlpha = alphaMod;
                color = MISSILE_LOCKED_COLOR;
            } else color = Colors.getEnemy();
        } else color = Colors.getFriendly(); // Allied missile
        missilesToDraw.add(radarLoc[0], radarLoc[1], missile.getFacing(), missileSize, color,
                           alphaMod);
    }

    private void updateMissilesToDraw(final ShipAPI player) {
        playerLock = false;
        highestThreatAlpha = 0f;
        missilesToDraw.clear();
        int maxMissiles = 500;
        final List<MissileAPI> missiles = radar.filterVisible(
                Global.getCombatEngine().getMissiles(), maxMissiles);
        for (MissileAPI missile : missiles)
            if (!missile.isFlare()) addMissileToDraw(player, missile);
        missilesToDraw.finish();
    }

    private void drawMissiles() {
        radar.enableStencilTest();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        missilesToDraw.draw();
        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);

        radar.disableStencilTest();

        if (playerLock) {
            lockIcon.setAlphaMult(radar.getRadarAlpha() * highestThreatAlpha);
            lockIcon.renderAtCenter(lockIconLocation.x, lockIconLocation.y);
        }

        glDisable(GL_TEXTURE_2D);
    }

    @Override
    public void render(final ShipAPI player, final float amount, final boolean isUpdateFrame) {
        if (!player.isAlive()) return;
        if (isUpdateFrame) updateMissilesToDraw(player); // Regenerate all vertex data
        if (!missilesToDraw.isEmpty()) drawMissiles();
    }
}
