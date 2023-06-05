package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.OCUA_HullMods;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ocua_ocutek_mod extends BaseHullMod {
    protected static final float LOAD_OF_BULL = 3f;
	
	public static final float EMP_REDUCTION = 10f;
	public static final float AUTO_AIM_BONUS = 25f;
	public static final float SHIELD_BONUS_MOVEMENT = 1.5f;
	public static final float ENERGY_DAMAGE_MULT = 0.1f;
	//public static final float ANTIMISSILE_DAMAGE_MULT = 1.25f;
        
	//public static final float LOSS_PENALTY = 1.5f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        //BLOCKED_HULLMODS.add("safetyoverrides");
        //BLOCKED_HULLMODS.add("expanded_deck_crew");
        //BLOCKED_HULLMODS.add("converted_hangar");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldTurnRateMult().modifyMult(id, SHIELD_BONUS_MOVEMENT);
		stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_BONUS_MOVEMENT);
		stats.getAutofireAimAccuracy().modifyPercent(id, AUTO_AIM_BONUS);
		stats.getEmpDamageTakenMult().modifyPercent(id, -EMP_REDUCTION);
		stats.getEnergyShieldDamageTakenMult().modifyMult(id, (1 - ENERGY_DAMAGE_MULT));
		//stats.getDamageToMissiles().modifyMult(id, (ANTIMISSILE_DAMAGE_MULT));
                
		//stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, LOSS_PENALTY);
                
		stats.addListener(new FighterOPCostModifier() {
                        @Override
			public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
				if (fighter.getId().contains("ocua_")) {
					return (int) (currCost * 0.8);
				}
				return currCost;
			}
		});
                
		stats.addListener(new WeaponOPCostModifier() {
                        @Override
			public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
				if (weapon.getWeaponId().contains("ocua_") && weapon.hasTag("ocua")) {
                                    if (weapon.getSize().equals(WeaponSize.SMALL)) return (currCost - (int) 1); 
                                    if (weapon.getSize().equals(WeaponSize.MEDIUM)) return (currCost - (int) 2);
                                    if (weapon.getSize().equals(WeaponSize.LARGE)) return (currCost - (int) 4);
				}
				return currCost;
			}
		});
    }

        @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
            
            List weapons = ship.getAllWeapons();
            Iterator iter = weapons.iterator();
            while (iter.hasNext()) {
                    WeaponAPI weapon = (WeaponAPI)iter.next();
//                    float turn = weapon.getTurnRate();
//                  if (weapon.hasAIHint(AIHints.PD)) {
//				weapon.get
//                  }
                    if ((weapon.getSize() == WeaponSize.SMALL || 
                            weapon.getSize() == WeaponSize.MEDIUM) && (weapon.isBeam() || 
                            weapon.getId().equals("ocua_blita") || 
                            weapon.getId().equals("ocua_vira")) && (weapon.getType() != WeaponType.MISSILE)) {
                    	weapon.setPD(true);
                    }
            }
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
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
		
                LabelAPI bullet;
                tooltip.addPara("\"Aaaaaaaaaaaaarg.\"", gray, opad);
                tooltip.addPara(" - Some pirate", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Innate Features", Alignment.MID, opad);
                bullet = tooltip.addPara("Shield Energy damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 *(ENERGY_DAMAGE_MULT)) + "%" );
                bullet.setHighlight("-" + (int) (100 *(ENERGY_DAMAGE_MULT)) + "%");
                bullet.setHighlightColors(good);
                bullet = tooltip.addPara("Shield Fold/Unfold speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 *(SHIELD_BONUS_MOVEMENT - 1)) + "%" );
                bullet = tooltip.addPara("EMP damage resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) EMP_REDUCTION + "%" );
                bullet = tooltip.addPara("Auto-Aim accuracy %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) AUTO_AIM_BONUS + "%" );
                bullet = tooltip.addPara("Oculian %s, %s and %s have full point-defense capabilities.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h, 
                        "Blita", "Vira", "small/medium beam weapons" );
		bullet.setHighlight("Oculian","Blita", "Vira", "small/medium beam weapons");
		bullet.setHighlightColors(ocua, h, h, h);
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Ordinance cost of Oculian Weapons %s, depending on slot size.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-1/-2/-4" );
		bullet.setHighlight("Oculian", "-1/-2/-4" );
		bullet.setHighlightColors(ocua, good);
                bullet = tooltip.addPara("Ordinance cost of Oculian LPCs %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-20%" );
		bullet.setHighlight("Oculian", "-20%");
		bullet.setHighlightColors(ocua, good);
                bullet = tooltip.addPara("Cannot install Oculian Core modules.%s", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "" );
		bullet.setHighlight("Cannot install", "Oculian", "Core modules");
		bullet.setHighlightColors(bad, ocua, h);
                
            tooltip.setBulletedListMode(null);
	}
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	/*if(index == 0) return "" + (int) AUTO_AIM_BONUS + "%";
	if(index == 1) return "" + (int) EMP_REDUCTION + "%";
	if(index == 2) return "" + (int) (100 *(SHIELD_BONUS_MOVEMENT - 1)) + "%";
	if(index == 3) return "" + (int) (100 *(ENERGY_DAMAGE_MULT)) + "%";
	if(index == 4) return "" + (int) (100 *(ANTIMISSILE_DAMAGE_MULT - 1)) + "%";
	if(index == 5) return "1/2/4";
	if(index == 6) return "20%";
                
	if(index == 7) return "is incapable of installing Oculian core modules";*/
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
