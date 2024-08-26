package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class aDroneShield extends BaseHullMod {
	
	public static final float MIN_DR_CR = 0.254f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getMaxSpeed().modifyFlat(id, (Float) mag.get(hullSize) * -1f);
		stats.getMaxCombatReadiness().modifyFlat(id, MIN_DR_CR, "Directorate Drone");
	}



	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			return "Can maintain minimal combat readiness without the automated ship skill.";
		}
		
		return null;
	}

    @Override
	public static boolean isAutomatedNoPenalty(MutableShipStatsAPI stats) {
		if (stats == null) return false;
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return false;
				member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
		return member.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY) ||
				member.getVariant().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY);
	}

    @Override	
	public static boolean isAutomatedNoPenalty(ShipAPI ship) {
		if (ship == null) return false;
		FleetMemberAPI member = ship.getFleetMember();
		if (member == null) return false;
				member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
		return member.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY) ||
				member.getVariant().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY);
	}

    @Override	
	public static boolean isAutomatedNoPenalty(FleetMemberAPI member) {
		if (member == null) return false;
				member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
		return member.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY) ||
				member.getVariant().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY);
	}

}
