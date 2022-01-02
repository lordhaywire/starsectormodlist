package hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import ids.Roider_Ids.Roider_Hullmods;
import java.awt.Color;

/**
 * Author: SafariJohn
 */
public class Roider_SheriffDrones extends BaseHullMod {
	public static final float EW_PENALTY_MULT = 0.5f;
    public static final float ENGAGEMENT_REDUCTION_MULT = 0f;

//    public static final Color DRONE_ENGINE_COLOR = new Color(255,255,0,0);
    public static final Color DRONE_ENGINE_COLOR = new Color(170,220,222,255);
//    public static final Color DRONE_ENGINE_COLOR = new Color(255,125,25,255);

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - ENGAGEMENT_REDUCTION_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round((1f - EW_PENALTY_MULT) * 100f) + "%";
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterWingRange().modifyMult(id, ENGAGEMENT_REDUCTION_MULT, "Drone Network");
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine() == null) return;
        if (Global.getCombatEngine().getShips() == null) return;

        float numDronesAlive = 0;
        FighterWingAPI wing = null;
        for (ShipAPI s : Global.getCombatEngine().getShips()) {
            if (!s.isFighter()) continue;
            if (!s.isAlive()) continue;

            FighterWingAPI w = s.getWing();

            if (w == null) continue;

            if (w.getSourceShip() == ship) {
                numDronesAlive++;
                wing = w;

                s.getEngineController().fadeToOtherColor(this, DRONE_ENGINE_COLOR, null, 1f, 1.5f);
//                s.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
            }
        }

        if (wing != null) {
            float mult = numDronesAlive / (float) wing.getSpec().getNumFighters();
            ship.getMutableStats().getDynamic().getStat(
                        Stats.ELECTRONIC_WARFARE_PENALTY_MULT)
                        .modifyMult(Roider_Hullmods.SHERIFF_DRONES + ship.getId(),
                            1f - (EW_PENALTY_MULT * mult));
        }
    }

	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		MutableShipStatsAPI stats = fighter.getMutableStats();

		stats.getEngineDamageTakenMult().modifyMult(id, 0f);
		stats.getAutofireAimAccuracy().modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
    }

}
