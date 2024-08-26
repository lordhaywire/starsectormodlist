package RealisticCombat.renderers;

import RealisticCombat.calculation.Beam;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.OpenGLUtils;

public final class Diffraction implements Indication {

    private static boolean isDiffractionIrrelevant(final ShipAPI player) {
        return player.isShuttlePod()
                || player.getShipTarget() == null
                || player.getSelectedGroupAPI() == null
                || player.getSelectedGroupAPI().getWeaponsCopy().isEmpty();
    }

    public static float getAverageDiffraction(final WeaponGroupAPI group, final Vector2f location) {
        float totalDiffraction = 0;
        for (final WeaponAPI weapon : group.getWeaponsCopy()) {
            if (!(weapon.isBeam())) continue;
            float initialIntensity = weapon.getSpec().getDerivedStats().getDps(),
                  distance = Misc.getDistance(weapon.getLocation(), location),
                  diffractedIntensity = Beam.getDiffractedIntensity(initialIntensity, distance);
            totalDiffraction += diffractedIntensity / initialIntensity;
        } return totalDiffraction / group.getWeaponsCopy().size();
    }

    private static void renderSidebar(final Vector2f center, final float thickness) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(center.getX(), center.getY() - thickness);
        GL11.glVertex2f(center.getX(), center.getY() + thickness);
        GL11.glEnd();
    }

    private static void renderDiffractionBar(final Vector2f start, final Vector2f end) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(start.getX(), start.getY());
        GL11.glVertex2f(end.getX(), end.getY());
        GL11.glEnd();
    }

    @Override
    public void render(final ViewportAPI viewport) {
        final ShipAPI player = Global.getCombatEngine().getPlayerShip();
        if (isDiffractionIrrelevant(player)) return;

        final float averageDiffraction = getAverageDiffraction(player.getSelectedGroupAPI(),
                player.getShipTarget().getLocation());
        if (averageDiffraction <= 0) return;

        final float
                offsetX = RealisticCombat.settings.Diffraction.getOffsetX(),
                offsetY = RealisticCombat.settings.Diffraction.getOffsetY(),
                thickness = RealisticCombat.settings.Diffraction.getThickness(),
                length = offsetX * (float) Math.pow(averageDiffraction, 0.333333);
        final Vector2f
                left = new Vector2f(Mouse.getX() - offsetX, Mouse.getY() + offsetY),
                right = new Vector2f(Mouse.getX() + offsetX, Mouse.getY() + offsetY),
                start = new Vector2f(Mouse.getX() - length / 2, left.getY()),
                end = new Vector2f(Mouse.getX() + length / 2, left.getY());

        OpenGLUtils.setupRendering();
        OpenGLUtils.glColor(java.awt.Color.WHITE, Colors.getAlpha(viewport.getViewMult()));
        GL11.glLineWidth(thickness / 2);
        renderSidebar(left, thickness);
        renderSidebar(right, thickness);
        GL11.glLineWidth(thickness);
        renderDiffractionBar(start, end);
        OpenGLUtils.finishRendering();
    }
}
