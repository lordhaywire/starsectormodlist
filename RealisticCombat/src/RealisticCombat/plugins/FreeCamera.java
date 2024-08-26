package RealisticCombat.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public final class FreeCamera extends BaseEveryFrameCombatPlugin {

    private boolean beingToggled = false, cameraTethered = true;

    private boolean isToggleNewlyPressed() {
        if (Keyboard.isKeyDown(RealisticCombat.settings.FreeCamera.getToggle())) {
            if (!beingToggled) {
                beingToggled = true;
                return true;
            }
        } else beingToggled = false;
        return false;
    }

    private static boolean isProhibited() {
        return Global.getCurrentState() != GameState.COMBAT
                || Global.getCombatEngine() == null
                || Global.getCombatEngine().isUIShowingDialog();
    }

    private static float getMoveSpeedInputFactor(final float distanceFromCenter,
                                                 final float threshold,
                                                 final float limit)
    {
        return Math.min(1, (distanceFromCenter - threshold) / (limit - threshold));
    }

    private static void updatePosition(final ViewportAPI viewport) {
        final float viewMult = viewport.getViewMult(),
                    halfWidth = viewport.getVisibleWidth() / viewMult / 2,
                    halfHeight = viewport.getVisibleHeight() / viewMult / 2;
        final Vector2f mousePosition = new Vector2f(Mouse.getX(), Mouse.getY()),
                       screenCenter = new Vector2f(halfWidth, halfHeight),
                       direction = Misc.getUnitVector(screenCenter, mousePosition);
        final float
                distance = Misc.getDistance(screenCenter, mousePosition),
                halfHeightFraction =
                        RealisticCombat.settings.FreeCamera.getScreenHalfHeightThresholdFraction(),
                threshold = halfHeightFraction * halfHeight;
        if (distance < threshold) return;
        final float
                scaleLimit = halfHeight
                    * (1 + RealisticCombat.settings.FreeCamera.getScreenHalfHeightScaleFraction()),
                inputFactor = getMoveSpeedInputFactor(distance, threshold, scaleLimit),
                speed = inputFactor * viewMult * RealisticCombat.settings.FreeCamera.getBaseSpeed();
        viewport.setCenter(new Vector2f(
                viewport.getCenter().getX() + speed * direction.getX(),
                viewport.getCenter().getY() + speed * direction.getY()));
    }

    private static void zoomIn(final ViewportAPI viewport, final float factor) {
        final float llx = viewport.getLLX() + factor * viewport.getVisibleWidth() / 2,
                    lly = viewport.getLLY() + factor * viewport.getVisibleHeight() / 2,
                    visibleWidth = (1 - factor) * viewport.getVisibleWidth(),
                    visibleHeight = (1 - factor) * viewport.getVisibleHeight();
        viewport.set(llx, lly, visibleWidth, visibleHeight);
    }

    private static void zoomOut(final ViewportAPI viewport, final float factor) {
        final float LLX = viewport.getLLX() - factor * viewport.getVisibleWidth() / 2,
                LLY = viewport.getLLY() - factor * viewport.getVisibleHeight() / 2,
                visibleWidth = (1 + factor) * viewport.getVisibleWidth(),
                visibleHeight = (1 + factor) * viewport.getVisibleHeight();
        viewport.set(LLX, LLY, visibleWidth, visibleHeight);
    }

    @Override
    public void advance(final float amount, final List<InputEventAPI> events) {
        if (isProhibited()) return;
        if (isToggleNewlyPressed()) cameraTethered = !cameraTethered;

        final ViewportAPI viewport = Global.getCombatEngine().getViewport();
        if (cameraTethered) {
            viewport.setExternalControl(false);
            return;
        }
        viewport.setExternalControl(true);

        updatePosition(viewport);
        if (Keyboard.isKeyDown(RealisticCombat.settings.FreeCamera.getZoomIn()))
            zoomIn(viewport, RealisticCombat.settings.FreeCamera.getZoomFactor());
        else if (Keyboard.isKeyDown(RealisticCombat.settings.FreeCamera.getZoomOut()))
            zoomOut(viewport, RealisticCombat.settings.FreeCamera.getZoomFactor());
    }
}
