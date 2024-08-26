package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import java.awt.Color;

public class CHM_Commission_this extends BaseHullMod {

	public static final float FIX_THIS = 20f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - FIX_THIS * 0.01f);
	}
    
    public String getDescriptionParam(int index, HullSize hullSize) {;
		if (index == 0) return "" + (int) FIX_THIS + "%";
        return null;
    }

	//Oh these are cool colors below introduced in 0.95a, to match with your tech type and stuff. Just nice to have!

    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(245,150,30,255);
    }
}
