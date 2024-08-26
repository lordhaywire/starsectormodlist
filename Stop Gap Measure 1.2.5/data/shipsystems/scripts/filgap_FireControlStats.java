package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class filgap_FireControlStats extends BaseShipSystemScript {

	public static final float WEAPON_RANGE_PCT = 20f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float weaponRangePercent = WEAPON_RANGE_PCT * effectLevel;

		stats.getBallisticWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float weaponRangePercent = WEAPON_RANGE_PCT * effectLevel;
		if (index == 0) return new StatusData("weapon range +" + (int) weaponRangePercent + "%", false);
		return null;
	}
}