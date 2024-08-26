package RealisticCombat.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;

import java.util.List;

public final class Indication extends BaseEveryFrameCombatPlugin {

    private static boolean toggled = true, prohibited;


    private static boolean isHidingCombat(final CombatEngineAPI engine) {
        return engine.getCombatUI() == null
                || engine.getCombatUI().isShowingDeploymentDialog()
                || engine.getCombatUI().isShowingCommandUI()
                || engine.isUIShowingDialog();
    }

    private static boolean isZoomedTooClose(final ViewportAPI viewport) {
        return viewport.getViewMult() < RealisticCombat.settings.Indication.getZoomCutoff();
    }

    private static boolean isGone(final ShipAPI ship) {
        return ship == null
                || !(Global.getCombatEngine().isEntityInPlay(ship) && ship.isAlive())
                || ship.getLocation() == null;
    }

    private static boolean isProhibited() {
        if (Global.getCurrentState() != GameState.COMBAT) return true;
        final CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return true;
        final ViewportAPI viewport = engine.getViewport();
        if (viewport == null) return true;
        if (isHidingCombat(engine) || isZoomedTooClose(viewport)) return true;
        return isGone(engine.getPlayerShip());
    }

    private static boolean isToggleKeyPress(final InputEventAPI event) {
        return event.getEventType() == InputEventType.KEY_DOWN
                && event.getEventValue() == RealisticCombat.settings.Indication.getToggleKey();
    }

    public static boolean isVisible() { return toggled && !prohibited; }

    @Override
    public void advance(final float amount, final List<InputEventAPI> events) {
        prohibited = isProhibited();
    }

    @Override
    public void processInputPreCoreControls(final float amount, final List<InputEventAPI> events) {
        for (InputEventAPI event : events)
            if (!event.isConsumed() && isToggleKeyPress(event)) {
                toggled = !toggled; event.consume(); break;
            }
    }

    @Override
    public void renderInWorldCoords(final ViewportAPI viewport) {
        if (!isProhibited() && toggled)
            for (final RealisticCombat.renderers.Indication indication
                    : RealisticCombat.settings.Indication.getIndications())
                indication.render(viewport);
    }
}
