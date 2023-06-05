package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class ocua_mi_mod extends BaseHullMod {
    protected static final float LOAD_OF_BULL = 3f;
	
	//public static final float RATE_BONUS = 1.25f;
	//public static final float FLUX_BONUS = 20f;
	//public static final float SUPPLY_USE_MULT = 10f;
	//public static final float SUP_USE_CAP = 20f;
	//public static final float SUPPLY_USE_FLAT = 2f;
    
	public static final float EFFICIENCY_BONUS = 10f;
	public static final float SYSTEMS_BONUS = 25f;
        
	public static final float DEGRADE_INCREASE_PERCENT = 100f;
        
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS);
		//stats.getFluxDissipation().modifyPercent(id, (FLUX_BONUS / 2));
		
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (EFFICIENCY_BONUS / 100)));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, (1 - (EFFICIENCY_BONUS / 100)));
                
		stats.getEngineHealthBonus().modifyMult(id, (1 + (SYSTEMS_BONUS / 100)));
		stats.getWeaponHealthBonus().modifyMult(id, (1 + (SYSTEMS_BONUS / 100)));

		stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -1);
                stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -2);
                stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -3);
		stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -1);
                stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -2);
                stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -3);
                
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
        }
    
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                Color gray = Misc.getGrayColor();
                Color ocua = new Color(250,100,175,255);
                Color ocua_mi = new Color(180,50,90,255);
		
                LabelAPI bullet;
                tooltip.addPara("\"Mother's upscale program has been distributed throughout the network. But for how long this assault would continue remains unsure, sisters.\"", gray, opad);
                tooltip.addPara(" - 01-S-Prime \"Aya\"", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Main Features", Alignment.MID, opad);
                bullet = tooltip.addPara("Can install %s %s %s that can merge into more powerful versions.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "2", "Oculian", "configuration cores" );
		bullet.setHighlight("2", "Oculian", "configuration cores");
		bullet.setHighlightColors(good, ocua, h);
                bullet = tooltip.addPara("Can also install the %s, as substitute for an %s %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Catalyst Core", "Oculian", "configuration core" );
		bullet.setHighlight("Catalyst Core", "Oculian", "configuration core");
		bullet.setHighlightColors(h, ocua, h);
                tooltip.setBulletedListMode(null);
                bullet = tooltip.addPara("%s: Purposely S-modded/Built-in %s will be removed once interacted with another core.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Note", "single configuration cores" );
		bullet.setHighlight("Note", "single configuration cores");
		bullet.setHighlightColors(h, h);
                
                tooltip.setBulletedListMode(" • ");
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Flux generation for Energy and Missile weapons %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) EFFICIENCY_BONUS + "%" );
                bullet = tooltip.addPara("Engine and Turret health %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) SYSTEMS_BONUS + "%" );
                bullet = tooltip.addPara("Oculian Weapon discount effect is %s for Mikanate weapons.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "doubled" );
		bullet.setHighlight("Oculian", "doubled", "Mikanate");
		bullet.setHighlightColors(ocua, good, ocua_mi);
                bullet = tooltip.addPara("Mikanate Wing LPCs discount effect is set to %s instead of %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "35%", "20%" );
		bullet.setHighlight("Mikanate", "35%", "20%");
		bullet.setHighlightColors(ocua_mi, good, gray);
                bullet = tooltip.addPara("Additional Ordinance cost reduction %s for Energy and Missile weapons, depending on size.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-1/-2/-3" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair bonus from Oculian Hull is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "nullified" );
		bullet.setHighlight("Oculian", "nullified");
		bullet.setHighlightColors(ocua, bad);
                bullet = tooltip.addPara("CR decay %s faster, once Peak Readiness is depleted.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "" + (int) DEGRADE_INCREASE_PERCENT + "%" );
                
            tooltip.setBulletedListMode(null);
	}
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	//if(index == 0) return "accept 2 core configurations";
	//if(index == 1) return "Catalyst Core";
	//if(index == 2) return "" + (int) (EFFICIENCY_BONUS) + "%";
	//if(index == 3) return "" + (int) (SYSTEMS_BONUS) + "%";
	//if(index == 4) return "1/2/3";
	//if(index == 5) return "double";
        return null;
        
	//if(index == 0) return "" + (int) (FLUX_BONUS) + "%";
	//if(index == 6) {
        //    if (hullSize == HullSize.FRIGATE) return "" + (int) (SUPPLY_USE_FLAT / 2);
        //    else if(hullSize == HullSize.DESTROYER) return "" + (int) (SUPPLY_USE_FLAT * 1.5);
        //    else if(hullSize == HullSize.CRUISER) return "" + (int) (SUPPLY_USE_FLAT * 2.5);
        //    else if(hullSize == HullSize.CAPITAL_SHIP) return "" + (int) (SUPPLY_USE_FLAT * 4);
        //    else return "" + (int) SUPPLY_USE_FLAT;
        //}
	//if(index == 7) {
        //    //if (hullSize == HullSize.CAPITAL_SHIP) return "" + (int) (SUP_USE_CAP) + "%";
        //    return "" + (int) (SUPPLY_USE_MULT) + "%";
        //}
	//if(index == 4) return "" + (int) (SUPPLY_USE_FLAT / 2) + "/" + (int) (SUPPLY_USE_FLAT * 1.5) + "/" + (int) (SUPPLY_USE_FLAT * 2.5) + "/" + (int) (SUPPLY_USE_FLAT * 4) + " + " + (int) (SUPPLY_USE_MULT) + "%";

    }
    
    @Override
    public boolean affectsOPCosts() {
    	return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
