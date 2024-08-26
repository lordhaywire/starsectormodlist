package RealisticCombat.renderers;

import RealisticCombat.util.DrawUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import RealisticCombat.scripts.Categorization;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.OpenGLUtils;

/**
 * Displays the momentum of every ship.
 *
 * Renders a triangle near every moving ship to indicate the momentum of
 * that ship. The triangle indicating the momentum of a ship points along
 * and lengthens with the velocity of the ship and widens with the tenth
 * logarithm of the mass of the ship.
 */
public final class Momentum implements Indication {

    private static boolean isRequired(final ShipAPI ship) {
        return Categorization.isFlying(ship)
                && !(Categorization.isStrikeCraft(ship) || Categorization.isStation(ship))
                && Global.getCombatEngine().getFogOfWar(0).isVisible(ship);
    }

    private static float getOffset(final ShipAPI ship) {
        return RealisticCombat.settings.Momentum.getOffsetFactor() * ship.getShieldRadiusEvenIfNoShield();
    }

    private static float getSpeedLevel(final ShipAPI ship) {
        return Math.max(Math.min(ship.getVelocity().length() / ship.getMaxSpeed(), 1), 0);
    }

    private static float getLength(final ShipAPI ship) {
        return RealisticCombat.settings.Momentum.getLengthFactor()
                * (float) Math.sqrt(ship.getMaxSpeed())
                * getSpeedLevel(ship);
    }

    private static float getWidth(final ShipAPI ship) {
        return RealisticCombat.settings.Momentum.getWidthFactor()
                * (float) Math.log10(ship.getMassWithModules());
    }

    private static Vector2f[] getTriangle(final Vector2f center,
                                          final Vector2f velocity,
                                          final float offset,
                                          final float width,
                                          final float length)
    {
        final Vector2f
                unitVelocity = Misc.normalise(new Vector2f(velocity)),
                unitPerpendicularVelocity = Misc.getPerp(unitVelocity),
                beginning = new Vector2f(center.getX() + offset * unitVelocity.getX(),
                                         center.getY() + offset * unitVelocity.getY()),
                spread = new Vector2f(width * unitPerpendicularVelocity.getX(),
                                      width * unitPerpendicularVelocity.getY()),
                left = Vector2f.sub(beginning, spread, new Vector2f()),
                right = Vector2f.add(beginning, spread, new Vector2f()),
                point = new Vector2f(center.getX() + (offset + length) * unitVelocity.getX(),
                                     center.getY() + (offset + length) * unitVelocity.getY());
        return new Vector2f[] { left, point, right };
    }

    private static void indicate(final ShipAPI ship, final float alpha) {
        OpenGLUtils.glColor(Colors.getColor(ship), alpha);
        DrawUtils.drawTriangle(getTriangle(ship.getShieldCenterEvenIfNoShield(),
                ship.getVelocity(), getOffset(ship), getWidth(ship), getLength(ship)));
    }

    @Override
    public void render(final ViewportAPI viewport) {
        final CombatEngineAPI engine = Global.getCombatEngine();
        final float alpha = Colors.getAlpha(viewport.getViewMult());

        OpenGLUtils.setupRendering();
        for (final ShipAPI ship : engine.getShips()) if (isRequired(ship)) indicate(ship, alpha);
        OpenGLUtils.finishRendering();
    }
}
