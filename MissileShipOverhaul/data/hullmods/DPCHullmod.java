package data.hullmods;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class DPCHullmod extends BaseHullMod {

	public static final float BEAM_RANGE_BONUS = 100f;
	public static float FLUX_CAPACITY_PERCENT = 0.7f;
	public static float FLUX_DISSIPATION_PERCENT = 0.9f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponRangeBonus().modifyFlat(id, BEAM_RANGE_BONUS);
		stats.getFluxCapacity().modifyMult(id, FLUX_CAPACITY_PERCENT);
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_PERCENT);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new DPCHullmodDamageDealt(ship));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BEAM_RANGE_BONUS;
		if (index == 1) return "" + (int)Math.round((1f - FLUX_CAPACITY_PERCENT) * 100f) + "%";
		if (index == 2) return "" + (int)Math.round((1f - FLUX_DISSIPATION_PERCENT) * 100f) + "%";
		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if ( ship.getVariant().getHullMods().contains(HullMods.HIGH_SCATTER_AMP) || ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS) ){
			return false;
		}else{
			return true;
		}
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains(HullMods.HIGH_SCATTER_AMP)) {
			return "Incompatible with High Scatter Amplifier";
		}else if (ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS)){
			return "Incompatible with Advanced Optics";
		}
		return null;
	}
	
	public static class DPCHullmodDamageDealt implements DamageDealtModifier {
		protected ShipAPI ship;
		public DPCHullmodDamageDealt(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			
			if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
				damage.setForceHardFlux(true);
			}
			return null;
		}
	}
	


}
