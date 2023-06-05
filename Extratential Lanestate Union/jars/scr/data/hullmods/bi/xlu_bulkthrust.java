package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

import java.util.HashMap;
import java.util.Map;

public class xlu_bulkthrust extends BaseHullMod {
    
    //Times between moving in a straight line versus turning
    private static final Map<HullSize, Float> straight_move_dur = new HashMap<HullSize, Float>();
    static {
        straight_move_dur.put(HullSize.FRIGATE, 0.8f);
        straight_move_dur.put(HullSize.DESTROYER, 1.4f);
        straight_move_dur.put(HullSize.CRUISER, 2f);
        straight_move_dur.put(HullSize.CAPITAL_SHIP, 3f);
    }
    private static final Map<HullSize, Float> turn_reduct_dur = new HashMap<HullSize, Float>();
    static {
        turn_reduct_dur.put(HullSize.FRIGATE, 1.2f);
        turn_reduct_dur.put(HullSize.DESTROYER, 1.4f);
        turn_reduct_dur.put(HullSize.CRUISER, 1.6f);
        turn_reduct_dur.put(HullSize.CAPITAL_SHIP, 2.0f);
    }
    private static final Map<HullSize, Float> bonus_top_speed = new HashMap<HullSize, Float>();
    static {
        bonus_top_speed.put(HullSize.FRIGATE, 25f);
        bonus_top_speed.put(HullSize.DESTROYER, 30f);
        bonus_top_speed.put(HullSize.CRUISER, 40f);
        bonus_top_speed.put(HullSize.CAPITAL_SHIP, 40f);
    }
    //private static final float TOP_SPEED_BONUS = 40f;
    private static final float FLUX_DIS_BONUS = 20f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
    }
    
    private final Color color = new Color(195,255,75,255);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        float longtime = straight_move_dur.get(ship.getHullSize());
        float turnMult = 1f / turn_reduct_dur.get(ship.getHullSize());
        float gainMult = 1f;
        
        float bulk_bonus = bonus_top_speed.get(ship.getHullSize());
        float flux_bonus = (FLUX_DIS_BONUS / 100f);
        float flux_dis = FLUX_DIS_BONUS;
        if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
            bulk_bonus = bonus_top_speed.get(ship.getHullSize()) * 0.5f;
            flux_bonus = (FLUX_DIS_BONUS / 100f) * 0.5f;
            flux_dis = FLUX_DIS_BONUS;
            longtime = straight_move_dur.get(ship.getHullSize()) * 0.5f;
            turnMult = turn_reduct_dur.get(ship.getHullSize()) * 0.5f;
        }
        
        ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").modifyFlat("SRD_NullspaceConduitsNullifierID", -1f);

        boolean turns = false;
        if (ship.getAngularVelocity() > (0.1f * ship.getMaxTurnRate()) || 
                ship.getAngularVelocity() < (-0.1f * ship.getMaxTurnRate())) {
            turns = true;
        }
        //if (ship.getEngineController().isTurningLeft() || 
        //        ship.getEngineController().isTurningRight() || 

        if (turns) {
            ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").modifyFlat("xlu_bulkthrust_ID",
                    ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").getModifiedValue() - (amount * turnMult));
        } else if (ship.getEngineController().isAccelerating()) {
            ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").modifyFlat("xlu_bulkthrust_ID",
                    ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").getModifiedValue() + (amount * gainMult));
        }

        if (ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").getModifiedValue() > longtime) {
            ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").modifyFlat("xlu_bulkthrust_ID", longtime);
        } else if (ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").getModifiedValue() < 0f) {
            ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").modifyFlat("xlu_bulkthrust_ID", 0f);
        }

        float boostvalue = ship.getMutableStats().getDynamic().getStat("xlu_bulkthrust_count").getModifiedValue() / longtime;
	float noLossTime = ship.getMutableStats().getPeakCRDuration().computeEffective(ship.getHullSpec().getNoCRLossTime());
        
        if (ship.getHullLevel() > 0.5f && (noLossTime - ship.getTimeDeployedForCRReduction() > 0)) {
            ship.getMutableStats().getMaxSpeed().modifyPercent("xlu_bulkthrust_ID", bulk_bonus * boostvalue);
            ship.getMutableStats().getAcceleration().modifyPercent("xlu_bulkthrust_ID", bulk_bonus * boostvalue);
            ship.getMutableStats().getFluxDissipation().modifyMult("xlu_bulkthrust_ID", 1f + (flux_bonus * boostvalue));
            ship.getEngineController().extendFlame(this, 
                1.0f * boostvalue, 
                0.25f * boostvalue, 
                0.25f * boostvalue);
            if (boostvalue > 0.2f) {
                ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.5f);
            }
            if (ship == playerShip) {
                if (boostvalue > 0) {
                    Global.getCombatEngine().maintainStatusForPlayerShip("xlu_bulkthrust_ui1", "graphics/icons/hullsys/burn_drive.png",
                        "Voccor: " + Math.round((bulk_bonus * boostvalue * ship.getMutableStats().getMaxSpeed().getBaseValue()) / 100f) + " Top Speed", 
                        "+" + Math.round(flux_bonus * boostvalue * 100f) + "% Flux Dissipation",false);
                    //Global.getCombatEngine().maintainStatusForPlayerShip("xlu_bulkthrust_ui1", "graphics/icons/hullsys/burn_drive.png",
                    //    "Bulk Thrusters:", "+" + Math.round((bulk_bonus * boostvalue * ship.getMutableStats().getMaxSpeed().getBaseValue()) / 100f) + " Top Speed",false);
                }
            }
        } else {
            ship.getMutableStats().getMaxSpeed().unmodify("xlu_bulkthrust_ID");
            ship.getMutableStats().getFluxDissipation().unmodify("xlu_bulkthrust_ID");
        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	//if(index == 0) return "" + (int) TOP_SPEED_BONUS + "%";
        
	if(index == 0) {
            if(hullSize == HullSize.DESTROYER) return "" + (bonus_top_speed.get(HullSize.DESTROYER)).intValue() + "%";
            else if(hullSize == HullSize.CRUISER) return "" + (bonus_top_speed.get(HullSize.CRUISER)).intValue() + "%";
            else if(hullSize == HullSize.CAPITAL_SHIP) return "" + (bonus_top_speed.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
            else return "" + (int) (bonus_top_speed.get(HullSize.FRIGATE)).intValue() + "%";
        }
	if(index == 1) return "" + (int) FLUX_DIS_BONUS + "%";
	if(index == 2) return "Safety Overrides";
	if(index == 3) return "0.5x";
	if(index == 4) return "duration of gaining and losing additional speed is halved";
	if(index == 5) return "50%";
        
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
