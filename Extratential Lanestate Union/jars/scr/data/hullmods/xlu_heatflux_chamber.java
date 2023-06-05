package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class xlu_heatflux_chamber extends BaseHullMod {

        public static final float BOOSTER_DAMAGE = 5f;
        public static final float FLUX_DAMAGE_BONUS = 15f;
        public static final float FLUX_PROJ_SPEED = 30f;
        
	public static final float MANEUVER_PENALTY = 10f;
        public static final float FLUX_FIRING_PENALTY = 20f;
        
        public static final float SMODIFIER = 0.05f;
        public static final float SMODIFIER2 = 20f;
        
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            boolean sMod = isSMod(stats);
            
            stats.getHitStrengthBonus().modifyPercent(id, BOOSTER_DAMAGE);
            
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - ((sMod ? SMODIFIER : 0) * 100f));
            stats.getBallisticProjectileSpeedMult().modifyPercent(id, (sMod ? SMODIFIER : 0));
            
            /*stats.getAcceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
            stats.getDeceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
            stats.getTurnAcceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
            stats.getMaxTurnRate().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);*/
        }
    
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (!ship.isAlive()) return;
            float Flux = ship.getFluxTracker().getFluxLevel();
            float InFlux = 0;
            ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
            MutableShipStatsAPI stats = ship.getMutableStats();
            
            if (Flux > 0.2 && Flux < 0.65) { InFlux = Flux - 0.2f; }
            else if (Flux >= 0.65) { InFlux = 0.5f; }
            else InFlux = 0f; 
            
            stats.getBallisticWeaponDamageMult().modifyPercent("xlu_breezer_rounds", FLUX_DAMAGE_BONUS * InFlux * 2);
            stats.getProjectileSpeedMult().modifyPercent("xlu_breezer_rounds", FLUX_PROJ_SPEED * InFlux * 2);
            stats.getBallisticRoFMult().modifyMult("xlu_breezer_rounds", 1f - (FLUX_FIRING_PENALTY * 0.01f * InFlux * 2));
            
            float DamBonus = FLUX_DAMAGE_BONUS;
            float FluxPenalty = FLUX_FIRING_PENALTY * 0.01f;
            if (ship == playerShip){
                if (Flux > 0.2) {
                    Global.getCombatEngine().maintainStatusForPlayerShip("xlu_brzr_rnd_1", "graphics/icons/hullsys/infernium_injector.png",
                    "Heatflux Charge: ","+" + Math.round(DamBonus * InFlux * 2f) + "% Ballistic Damage",false);
                    Global.getCombatEngine().maintainStatusForPlayerShip("xlu_brzr_rnd_2", "graphics/icons/hullsys/infernium_injector.png",
                    "Weapons Overheating: ","" + Math.round((1f - (FluxPenalty * InFlux * 2f)) * 100f) + "% Ballistic RoF",false); }
                else {}
            }
        }	
        

        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
		
            if(index == 0) return "" + (int) BOOSTER_DAMAGE + "%";
            if(index == 1) return "20" + "%";
            if(index == 2) return "65" + "%";
            if(index == 3) return "" + (int) FLUX_DAMAGE_BONUS + "%";
            if(index == 4) return "" + (int) FLUX_PROJ_SPEED + "%";
            if(index == 5) return "" + (int) FLUX_FIRING_PENALTY + "%";
            if(index == 6) return "" + (int) MANEUVER_PENALTY + "%";
            else {
                return null;
            }
        }
    
        @Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
            if (index == 0) return "" + (int) Math.round(SMODIFIER * 100f) + "%";
            if (index == 1) return "" + (int) SMODIFIER2  + "%";
            return null;
	}
	
}