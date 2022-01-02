package hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.RepairGantry;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

/**
 * Author: SafariJohn
 */
public class Roider_ArgosSalvage extends BaseHullMod {

    public static final Float ARGOS_SALVAGE_BONUS = 40f;

	public static final float BATTLE_SALVAGE_MULT = .2f;
	public static final float MIN_CR = 0.1f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SALVAGE_VALUE_MULT_MOD)
                    .modifyFlat(id, ARGOS_SALVAGE_BONUS * 0.01f);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ARGOS_SALVAGE_BONUS.intValue() + "%";
		if (index == 1) return "" + (int) Math.round(BATTLE_SALVAGE_MULT * 100f) + "%";

		return null;
	}


	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize,
                ShipAPI ship, boolean isForModSpec) {
		return true;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip,
                HullSize hullSize, ShipAPI ship, float width,
                boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();

		tooltip.addPara("Each additional ship with a salvage gantry provides diminishing returns. " +
				"The higher the highest recovery bonus from a single ship in the fleet, the later diminishing returns kick in.", opad);

		if (isForModSpec || ship == null) return;
		if (Global.getSettings().getCurrentState() == GameState.TITLE) return;

		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		float fleetMod = RepairGantry.getAdjustedGantryModifier(fleet, null, 0f);
		float currShipMod = ARGOS_SALVAGE_BONUS * 0.01f;

		float fleetModWithOneMore = RepairGantry.getAdjustedGantryModifier(fleet, null, currShipMod);
		float fleetModWithoutThisShip = RepairGantry.getAdjustedGantryModifier(fleet, ship.getFleetMemberId(), 0f);

		tooltip.addPara("The total resource recovery bonus for your fleet is %s.", opad, h,
				"" + (int)Math.round(fleetMod * 100f) + "%");

		float cr = ship.getCurrentCR();
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getId().equals(ship.getFleetMemberId())) {
				cr = member.getRepairTracker().getCR();
			}
		}

		if (cr < MIN_CR) {
			LabelAPI label = tooltip.addPara("This ship's combat readiness is below %s " +
					"and the gantry can not be utilized. Bringing this ship into readiness " +
					"would increase the fleetwide bonus to %s.",
					opad, h,
					"" + (int) Math.round(MIN_CR * 100f) + "%",
					"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			label.setHighlightColors(bad, h);
			label.setHighlight("" + (int) Math.round(MIN_CR * 100f) + "%", "" + (int)Math.round(fleetModWithOneMore * 100f) + "%");

//			tooltip.addPara("Bringing this ship into readiness " +
//					"would increase the fleet's bonus to %s.", opad, h,
//					"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
		} else {
			if (fleetMod > currShipMod) {
				tooltip.addPara("Removing this ship would decrease it to %s. Adding another ship of the same type " +
						"would increase it to %s.", opad, h,
						"" + (int)Math.round(fleetModWithoutThisShip * 100f) + "%",
						"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			} else {
				tooltip.addPara("Adding another ship of the same type " +
						"would increase it to %s.", opad, h,
						"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			}
		}

		tooltip.addPara("The fleetwide post-battle salvage bonus is %s.", opad, h,
				"" + (int) Math.round(RepairGantry.getAdjustedGantryModifierForPostCombatSalvage(fleet) * 100f) + "%");

	}

}
