package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
//import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class xlu_rampart_integration extends BaseHullMod {

	public static final float COST_REDUCTION  = 2;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
		//stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
		//stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.addListener(new WeaponBaseRangeModifier() {
                @Override
			public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
				return 0;
			}
                        @Override
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			//if (weapon.getSize().equals(WeaponAPI.WeaponSize.SMALL) && weapon.getType().equals(WeaponAPI.WeaponType.BALLISTIC)) {
			//	return 1.2f;
			//}
			return 1f;
		}
                @Override
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSlot() == null) return 0f;
			if (weapon.getSize().equals(WeaponAPI.WeaponSize.SMALL) && weapon.getType().equals(WeaponAPI.WeaponType.BALLISTIC)) {
				return 200f;
			}
			return 0f;
		}
            });
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is affected by percentage modifiers from other hullmods and skills.", opad);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
		if (index == 1) return "200 units";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}








