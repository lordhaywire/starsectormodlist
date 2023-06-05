package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import data.scripts.ids.OCUA_HullMods;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ocua_converted_matrix extends BaseHullMod {

	public static final int CREW_REQ_PER_BAY = 10;
	public static final int MAX_CREW = 50;
	public static final int CARGO_PER_BAY = 20;
	
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            BLOCKED_HULLMODS.add("converted_fighterbay");
        }
    
	public static final float FLUX_CAP_FRIGATE = 200f;
	public static final float FLUX_CAP_DESTROYER = 300f;
	public static final float FLUX_CAP_CRUISER = 400f;
	public static final float FLUX_CAP_CAPITAL = 500f;
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		int bays = (int) Math.round(stats.getNumFighterBays().getBaseValue());
		stats.getNumFighterBays().modifyFlat(id, -bays);

		int crewReduction = CREW_REQ_PER_BAY * bays;
		if (crewReduction > MAX_CREW) crewReduction = MAX_CREW;
		int cargo = CARGO_PER_BAY * bays;
		int flux = (int) FLUX_CAP_FRIGATE * bays;
		
                if (hullSize == HullSize.FRIGATE) {
                    flux = (int) FLUX_CAP_FRIGATE * bays;
                } else if (hullSize == HullSize.DESTROYER) {
                    flux = (int) FLUX_CAP_DESTROYER * bays;
                } else if (hullSize == HullSize.CRUISER) {
                    flux = (int) FLUX_CAP_CRUISER * bays;
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    flux = (int) FLUX_CAP_CAPITAL * bays;
                }
                
		stats.getFluxCapacity().modifyFlat(id, flux);
		stats.getMinCrewMod().modifyPercent(id, -crewReduction);
		stats.getCargoMod().modifyFlat(id, cargo);
	}
	
        @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
            
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
	}
	
        @Override
	public boolean isApplicableToShip(ShipAPI ship) {
		int builtIn = ship.getHullSpec().getBuiltInWings().size();
		int bays = (int) Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
		if (builtIn <= 0 || bays > builtIn) return false;
                if (ship.getVariant().getHullMods().contains("converted_fighterbay")) return false;
                if (!ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD)) return false;
		return true;
	}
	
        @Override
	public String getUnapplicableReason(ShipAPI ship) {
		int builtIn = ship.getHullSpec().getBuiltInWings().size();
		int bays = (int) Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
                if (!ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD)) return "Must be an Oculian hull";
		if (builtIn <= 0 || bays > builtIn) return "Requires built-in fighter wings only";
                if (ship.getVariant().getHullMods().contains("converted_fighterbay")) return "Converted Fighter Bay already exists";
                return null;
	}
	
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Flux Capacity %s per deck.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) (FLUX_CAP_FRIGATE) + "/" + (int) (FLUX_CAP_DESTROYER) + "/" + (int) (FLUX_CAP_CRUISER) + "/" + (int) (FLUX_CAP_CAPITAL) );
		bullet.setHighlight("" + (int) (FLUX_CAP_FRIGATE) + "/" + (int) (FLUX_CAP_DESTROYER) + "/" + (int) (FLUX_CAP_CRUISER) + "/" + (int) (FLUX_CAP_CAPITAL),
                    "per deck");
		bullet.setHighlightColors(good, h);
                bullet = tooltip.addPara("Minimum crew %s per deck, up to a maximum of %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) CREW_REQ_PER_BAY + "%", "" + (int) MAX_CREW + "%" );
		bullet.setHighlight("-" + (int) CREW_REQ_PER_BAY + "%", "per deck", "" + (int) MAX_CREW + "%");
		bullet.setHighlightColors(good, h, h);
                bullet = tooltip.addPara("Cargo capacity %s per deck, up to a maximum of 50%.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) CREW_REQ_PER_BAY + " units" );
		bullet.setHighlight("" + (int) CREW_REQ_PER_BAY + " units", "per deck");
		bullet.setHighlightColors(good, h);
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Removes %s hangar bays.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad, "all built-in" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		//if (index == 0) return "" + CARGO_PER_BAY;
		//if (index == 1) return "" + CREW_REQ_PER_BAY + "%";
		//if (index == 2) return "" + MAX_CREW + "%";
		return null;
	}
	
}



