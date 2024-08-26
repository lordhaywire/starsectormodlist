package RealisticCombat.listeners;

import RealisticCombat.calculation.Vector;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;

import static RealisticCombat.util.OpenGLUtils.glColor;
import static com.fs.starfarer.api.combat.WeaponAPI.WeaponType.SYSTEM;

public final class WeaponFacings implements AdvanceableListener {

    private static final HashMap<WeaponAPI.WeaponType, Color>
            WEAPON_TYPE_COLORS = new HashMap<WeaponAPI.WeaponType, Color>() {{
                put(WeaponAPI.WeaponType.BALLISTIC, new Color(255, 215, 0));
                put(WeaponAPI.WeaponType.MISSILE, new Color(0, 255, 0));
                put(WeaponAPI.WeaponType.ENERGY, new Color(0, 190, 255));
            }};

    private final ShipAPI ship;

    public WeaponFacings(final ShipAPI ship) { this.ship = ship; }

    private static boolean isProhibited() {
        return Global.getCombatEngine() == null
                || Global.getCombatEngine().getCombatUI() == null
                || Global.getCombatEngine().isUIShowingDialog()
                || !(Global.getCombatEngine().isSimulation()
                || Global.getCombatEngine().isUIShowingHUD())
                || Global.getCombatEngine().getCombatUI().isShowingCommandUI();
    }

    private static boolean isFacingHidden(final WeaponAPI weapon) {
        try { return (weapon.getSpec().getPrimaryRoleStr().contains("Point Defense"))
                      || weapon.getType() == SYSTEM
                      || weapon.isFiring();
        } catch (final NullPointerException npe) { return true; }
    }

    private static boolean isFacingHighlighted(final WeaponAPI weapon,
                                               final WeaponGroupAPI weaponGroup) {
        final AutofireAIPlugin autofire = weaponGroup.getAutofirePlugin(weapon);
        if (autofire == null) return false;
        final ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        if (autofire.getTargetShip() == playerShip) return true;
        return autofire.getTargetShip() == playerShip.getShipTarget();
    }

    /**
     * Custom rendering setup.
     */
    private static void setupRendering() {
        final ViewportAPI viewport = Global.getCombatEngine().getViewport();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor()),
                  height = (int) (Display.getHeight() * Display.getPixelScaleFactor());
        GL11.glViewport(0, 0, width, height);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(viewport.getLLX(), viewport.getLLX() + viewport.getVisibleWidth(),
                     viewport.getLLY(), viewport.getLLY() + viewport.getVisibleHeight(), -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);
    }

    /**
     * Custom finish rendering.
     */
    private static void finishRendering() {
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        GL11.glPopAttrib();
    }


    @Override
    public void advance(final float amount) {
        if (isProhibited() || ship == null || !ship.isAlive()) return;
        setupRendering();
        for (final WeaponGroupAPI weaponGroup : ship.getWeaponGroupsCopy())
            for (final WeaponAPI weapon : weaponGroup.getWeaponsCopy()) {
                if (isFacingHidden(weapon)) continue;
                glColor(WEAPON_TYPE_COLORS.get(weapon.getType()),
                        isFacingHighlighted(weapon, weaponGroup) ? 0.80f : 0.4f);
                drawWeaponFacing(weapon);
            }
        finishRendering();
    }

    private static void drawWeaponFacing(final WeaponAPI weapon) {
        if (weapon.isDisabled()) return;
        final Vector2f location = weapon.getLocation();
        final float cangle = weapon.getCurrAngle();
        Vector2f toRotate = new Vector2f(location.x + weapon.getRange(), location.y);
        final Vector2f end = new Vector2f(0, 0);
        Vector.rotateAroundPivot(toRotate, location, cangle, end);

        toRotate = new Vector2f(location.x + 5, location.y);
        Vector2f start = new Vector2f(0, 0);
        Vector.rotateAroundPivot(toRotate, location, cangle, start);

        final int segments = (int) (Misc.getDistance(start, end) / 15);
        final float X = (end.getX() - start.getX()) / segments,
                Y = (end.getY() - start.getY()) / segments;
        final Vector2f perpendicular = Misc.getPerp(Misc.getUnitVector(start, end));
        final float xPerpendicular = perpendicular.getX() * 10,
                    yPerpendicular = perpendicular.getY() * 10;
        for (int i = 0; i < segments; i+=15) {
            GL11.glBegin(GL11.GL_LINES);
            final float x = i * X + start.getX(), y = i * Y + start.getY();
            GL11.glVertex2f(x - xPerpendicular, y - yPerpendicular);
            GL11.glVertex2f(x + xPerpendicular, y + yPerpendicular);
            GL11.glEnd();
        }
    }
}