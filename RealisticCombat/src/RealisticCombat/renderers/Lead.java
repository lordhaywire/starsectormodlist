package RealisticCombat.renderers;

import RealisticCombat.calculation.Vector;
import RealisticCombat.listeners.GunLocking;
import RealisticCombat.listeners.ThreeDimensionalTargeting;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.DrawUtils;
import RealisticCombat.util.OpenGLUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public final class Lead implements Indication {

    private static final float CROSSHAIRS_ON = 0.8f,
                               MINIMUM_RADIUS_SCREEN = 3,
                               CROSSHAIRS_WIDTH = 1.5f,
                               CROSSHAIRS_LENGTH = 50;

    private boolean wasOnTarget = false;

    private float blinkTimer = 0;


    private static boolean isFighting(final ShipAPI player) {
        return !(player == null || player.getHullSpec().getHullId().contentEquals("shuttlepod"));
    }

    private static boolean isLeading(final ShipAPI player) {
        return !(player.getShipTarget() == null
                 || player.getSelectedGroupAPI() == null
                 || player.getSelectedGroupAPI().getWeaponsCopy().isEmpty());
    }

    private static boolean isEveryWeaponAbleToBracket(final WeaponGroupAPI weaponGroup,
                                                      final float evasionTime) {
        for (final WeaponAPI weapon : weaponGroup.getWeaponsCopy())
            if (!RealisticCombat.calculation.Lead.isBracketingPossible(weapon, evasionTime))
                return false;
        return true;
    }

    private static float getCircleThickness(final float radius, final float viewMult) {
        return RealisticCombat.settings.Lead.getThicknessFactor()
                * radius
                * Math.max(1, 1 / viewMult);
    }

    private static float getCrosshairsLength(final float radiusScreen, final float viewMult) {
        return CROSSHAIRS_LENGTH
                * viewMult / 8
                * (float) Math.sqrt(Math.min(1, MINIMUM_RADIUS_SCREEN - radiusScreen));
    }

    private static void renderCrosshairs(final Vector2f lead,
                                         final float offset,
                                         final float length) {
        final float x = lead.getX(), y = lead.getY();
        final float[][] X = { { x + offset, x, x - offset, x },
                              { x + offset + length, x, x - offset - length, x } },
                        Y = { { y, y + offset, y, y - offset },
                              { y, y + offset + length, y, y - offset - length } };
        for (int i = 0; i < 4; i++) {
            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glVertex2f(X[0][i], Y[0][i]);
            GL11.glVertex2f(X[1][i], Y[1][i]);
            GL11.glEnd();
        }
    }

    @Override
    public void render(final ViewportAPI viewport) {
        final CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.isUIAutopilotOn()) return;
        final ShipAPI playerShip = engine.getPlayerShip();
        if (!(engine.isEntityInPlay(playerShip) && isFighting(playerShip))) return;

        final ShipAPI target = playerShip.getShipTarget();
        if (!isLeading(playerShip)
            || target == null
            || !engine.getFogOfWar(0).isVisible(target)) return;
        final WeaponGroupAPI selectedGroup = playerShip.getSelectedGroupAPI();
        if (selectedGroup == null) return;
        final Vector2f lead = RealisticCombat.calculation.Lead.getLead(selectedGroup, target);
        if (lead == null) return;

        final boolean allSelectedWeaponsCanBracket = isEveryWeaponAbleToBracket(selectedGroup,
                RealisticCombat.calculation.Lead.getEvasionTime(target));
        final float evasionDistance = RealisticCombat.calculation.Lead.getEvasionDistance(target),

                    radius = allSelectedWeaponsCanBracket ? target.getCollisionRadius() / 2
                                                          : evasionDistance,
                    viewMult = viewport.getViewMult(),
                    circleThickness = getCircleThickness(radius, viewMult),
                    alpha = 1,
                    radiusScreen = viewport.convertWorldHeightToScreenHeight(radius);

        GL11.glLineWidth(circleThickness);
        OpenGLUtils.glColor(Colors.getColor(target), alpha);
        OpenGLUtils.setupRendering();

        final boolean onTarget = GunLocking.isAnySelectedWeaponOnTarget();
        final int segments = RealisticCombat.settings.Lead.getSegments();
        if (radiusScreen > MINIMUM_RADIUS_SCREEN)
            DrawUtils.drawCircle(lead.getX(), lead.getY(), radius, segments, false);
        else {
            DrawUtils.drawCircle(lead.getX(), lead.getY(), radius, segments, true);
            if (onTarget) {
                blinkTimer = 0;
                wasOnTarget = true;
            } else {
                if (wasOnTarget) {
                    wasOnTarget = false;
                    blinkTimer = CROSSHAIRS_ON;
                }
                else {
                    blinkTimer += Global.getCombatEngine().getElapsedInLastFrame();
                    if (blinkTimer > 1) blinkTimer = 0;
                }
            }
            if (blinkTimer < CROSSHAIRS_ON) {
                final float length = getCrosshairsLength(radiusScreen, viewMult),
                            offset = radius + 0.75f * length;
                OpenGLUtils.glColor(Colors.getColor(target), alpha);
                GL11.glLineWidth(CROSSHAIRS_WIDTH);
                renderCrosshairs(lead, offset, length);
            }
        }
        OpenGLUtils.finishRendering();
    }
}
