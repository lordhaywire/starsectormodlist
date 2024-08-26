package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import java.awt.Color;

public class CHM_Mission_to_commission extends BaseHullMod {

	public static final float ON_A_MISSION_TO_DIE = 25f;
	public static final float PERMISSION_TO_DIE_DENIED = 20f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCRLossPerSecondPercent().modifyMult(id, 1f - ON_A_MISSION_TO_DIE * 0.01f);
		stats.getCrewLossMult().modifyMult(id, 1f - PERMISSION_TO_DIE_DENIED * 0.01f);
        
	}
    
    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ON_A_MISSION_TO_DIE + "%";
		if (index == 1) return "" + (int) PERMISSION_TO_DIE_DENIED + "%";
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
