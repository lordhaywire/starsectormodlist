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
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import data.scripts.ids.OCUA_HullMods;

public class ocua_oculus_mod extends BaseHullMod {
    protected static final float LOAD_OF_BULL = 3f;
	
	public static final float EMP_REDUCTION = 25f;
	public static final float TURRET_SPEED_BONUS = 33f;
	public static final float AUTO_AIM_BONUS = 50f;
	public static final float SHIELD_BONUS_MOVEMENT = 2.0f;
	public static final float ENERGY_DAMAGE_MULT = 0.25f;
	public static final float ANTIMISSILE_DAMAGE_MULT = 1.5f;
        
	public static final float KINETIC_ARMOR_TAKEN_MULT = 0.5f;
	public static final float EXPLOSIVE_TAKEN_MULT = 0.25f;
        
	public static final float SUPPLY_USE_MULT = 0.67f;
        
	public static final float LOSS_PENALTY = 1.5f;
        
	public static final float CARRIER_HANGAR = -1f; // Allows Quantix built-ins to install the extra hangar.

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("expanded_deck_crew");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("advancedoptics");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            //int bays = (int) stats.getNumFighterBays().getBaseValue();
            //if (bays >= 1 && (!(hullSize == HullSize.FRIGATE) || (hullSize == HullSize.FIGHTER))) {
            //    stats.getNumFighterBays().modifyFlat(id, CARRIER_HANGAR);
            //}
        
            float Shield_Move_Bonus = SHIELD_BONUS_MOVEMENT;
            float EMP_Resist_Bonus = 1;

            if (stats.getVariant().getHullMods().contains("ocua_clockoverdrive")) Shield_Move_Bonus = (1 + Shield_Move_Bonus) / 2;
            if (!stats.getVariant().getHullMods().contains("ocua_clockoverdrive")) EMP_Resist_Bonus = EMP_REDUCTION;
            
		stats.getShieldTurnRateMult().modifyMult(id, Shield_Move_Bonus);
		stats.getShieldUnfoldRateMult().modifyMult(id, Shield_Move_Bonus);
		stats.getAutofireAimAccuracy().modifyPercent(id, AUTO_AIM_BONUS);
		stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		stats.getEmpDamageTakenMult().modifyMult(id, 1 - (EMP_Resist_Bonus / 100));
		stats.getEnergyShieldDamageTakenMult().modifyMult(id, (1 - ENERGY_DAMAGE_MULT));
                
		//stats.getKineticArmorDamageTakenMult().modifyMult(id, (1 + KINETIC_ARMOR_TAKEN_MULT));
		//stats.getHighExplosiveDamageTakenMult().modifyMult(id, (1 - EXPLOSIVE_TAKEN_MULT));
		//stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		//stats.getDamageToMissiles().modifyMult(id, (ANTIMISSILE_DAMAGE_MULT));
                
            if (!stats.getVariant().getHullMods().contains("ocua_mi_mod")) {
                stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLY_USE_MULT);
            }
            
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, LOSS_PENALTY);
                
                
		stats.addListener(new WeaponOPCostModifier() {
                        @Override
			public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
				if (weapon.getWeaponId().contains("ocua_") && weapon.hasTag("ocua")) {
                                    if (weapon.getWeaponId().contains("ocua_mi_") && weapon.hasTag("ocua_mikanate")) {
                                        if (stats.getVariant().getHullMods().contains("ocua_mi_mod")){
                                            if (weapon.getSize().equals(WeaponSize.SMALL)) currCost = currCost - (int) 1; //return (currCost - (int) 2); 
                                            if (weapon.getSize().equals(WeaponSize.MEDIUM)) currCost = currCost - (int) 2; //return (currCost - (int) 4);
                                            if (weapon.getSize().equals(WeaponSize.LARGE)) currCost = currCost - (int) 4; //return (currCost - (int) 8);
                                        }
                                    }// else {
                                        if (weapon.getSize().equals(WeaponSize.SMALL)) return (currCost - (int) 1); 
                                        if (weapon.getSize().equals(WeaponSize.MEDIUM)) return (currCost - (int) 2);
                                        if (weapon.getSize().equals(WeaponSize.LARGE)) return (currCost - (int) 4);
                                    //}
				}
				if (!weapon.getWeaponId().contains("ocua_") && !stats.getVariant().getHullMods().contains("ocua_clockoverdrive")) {
                                    if (weapon.getSize().equals(WeaponSize.SMALL)) return (currCost + (int) 3);
                                    if (weapon.getSize().equals(WeaponSize.MEDIUM)) return (currCost + (int) 6);
                                    if (weapon.getSize().equals(WeaponSize.LARGE)) return (currCost + (int) 10);
				}
				return currCost;
			}
		});
                
		stats.addListener(new FighterOPCostModifier() {
                        @Override
			public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
				if (fighter.getId().contains("ocua_")) {
                                    if (fighter.hasTag("ocua_mikanate") && stats.getVariant().getHullMods().contains("ocua_mi_mod")) {
                                        return (int) (currCost * 0.65);
                                    }
                                    return (int) (currCost * 0.8);
                                }
				if (!fighter.getId().contains("ocua_")) return (currCost + (int) 10);
				return currCost;
			}
		});
    }

        @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
            if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)) {
                if (ship.getVariant().getHullMods().contains("ocua_core_chemical") && ship.getVariant().getHullMods().contains("ocua_core_catalyst")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CH);
                    ship.getVariant().removeMod("ocua_core_chemical"); ship.getVariant().removeMod("ocua_core_catalyst"); 
                    ship.getVariant().removePermaMod("ocua_core_chemical"); ship.getVariant().removePermaMod("ocua_core_catalyst"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_chemical") && ship.getVariant().getHullMods().contains("ocua_core_crystalline")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CHCR);
                    ship.getVariant().removeMod("ocua_core_chemical"); ship.getVariant().removeMod("ocua_core_crystalline");
                    ship.getVariant().removePermaMod("ocua_core_chemical"); ship.getVariant().removePermaMod("ocua_core_crystalline"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_chemical") && ship.getVariant().getHullMods().contains("ocua_core_quantix")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CHQU);
                    ship.getVariant().removeMod("ocua_core_chemical"); ship.getVariant().removeMod("ocua_core_quantix");
                    ship.getVariant().removePermaMod("ocua_core_chemical"); ship.getVariant().removePermaMod("ocua_core_quantix"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_chemical") && ship.getVariant().getHullMods().contains("ocua_core_pulse")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CHPL);
                    ship.getVariant().removeMod("ocua_core_chemical"); ship.getVariant().removeMod("ocua_core_pulse");
                    ship.getVariant().removePermaMod("ocua_core_chemical"); ship.getVariant().removePermaMod("ocua_core_pulse"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_chemical") && ship.getVariant().getHullMods().contains("ocua_core_vapor")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CHVP);
                    ship.getVariant().removeMod("ocua_core_chemical"); ship.getVariant().removeMod("ocua_core_vapor");
                    ship.getVariant().removePermaMod("ocua_core_chemical"); ship.getVariant().removePermaMod("ocua_core_vapor"); }
                
                if (ship.getVariant().getHullMods().contains("ocua_core_crystalline") && ship.getVariant().getHullMods().contains("ocua_core_catalyst")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CR);
                    ship.getVariant().removeMod("ocua_core_crystalline"); ship.getVariant().removeMod("ocua_core_catalyst");
                    ship.getVariant().removePermaMod("ocua_core_crystalline"); ship.getVariant().removePermaMod("ocua_core_catalyst"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_crystalline") && ship.getVariant().getHullMods().contains("ocua_core_quantix")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CRQU);
                    ship.getVariant().removeMod("ocua_core_crystalline"); ship.getVariant().removeMod("ocua_core_quantix");
                    ship.getVariant().removePermaMod("ocua_core_crystalline"); ship.getVariant().removePermaMod("ocua_core_quantix"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_crystalline") && ship.getVariant().getHullMods().contains("ocua_core_pulse")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CRPL);
                    ship.getVariant().removeMod("ocua_core_crystalline"); ship.getVariant().removeMod("ocua_core_pulse");
                    ship.getVariant().removePermaMod("ocua_core_crystalline"); ship.getVariant().removePermaMod("ocua_core_pulse"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_crystalline") && ship.getVariant().getHullMods().contains("ocua_core_vapor")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_CRVP);
                    ship.getVariant().removeMod("ocua_core_crystalline"); ship.getVariant().removeMod("ocua_core_vapor");
                    ship.getVariant().removePermaMod("ocua_core_crystalline"); ship.getVariant().removePermaMod("ocua_core_vapor"); }
                
                if (ship.getVariant().getHullMods().contains("ocua_core_quantix") && ship.getVariant().getHullMods().contains("ocua_core_catalyst")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_QU);
                    ship.getVariant().removeMod("ocua_core_quantix"); ship.getVariant().removeMod("ocua_core_catalyst");
                    ship.getVariant().removePermaMod("ocua_core_quantix"); ship.getVariant().removePermaMod("ocua_core_catalyst"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_quantix") && ship.getVariant().getHullMods().contains("ocua_core_pulse")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_QUPL);
                    ship.getVariant().removeMod("ocua_core_quantix"); ship.getVariant().removeMod("ocua_core_pulse");
                    ship.getVariant().removePermaMod("ocua_core_quantix"); ship.getVariant().removePermaMod("ocua_core_pulse"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_quantix") && ship.getVariant().getHullMods().contains("ocua_core_vapor")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_QUVP);
                    ship.getVariant().removeMod("ocua_core_quantix"); ship.getVariant().removeMod("ocua_core_vapor");
                    ship.getVariant().removePermaMod("ocua_core_quantix"); ship.getVariant().removePermaMod("ocua_core_vapor"); }
                
                if (ship.getVariant().getHullMods().contains("ocua_core_pulse") && ship.getVariant().getHullMods().contains("ocua_core_catalyst")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_PL);
                    ship.getVariant().removeMod("ocua_core_pulse"); ship.getVariant().removeMod("ocua_core_catalyst");
                    ship.getVariant().removePermaMod("ocua_core_pulse"); ship.getVariant().removePermaMod("ocua_core_catalyst"); }
                if (ship.getVariant().getHullMods().contains("ocua_core_pulse") && ship.getVariant().getHullMods().contains("ocua_core_vapor")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_PLVP);
                    ship.getVariant().removeMod("ocua_core_pulse"); ship.getVariant().removeMod("ocua_core_vapor");
                    ship.getVariant().removePermaMod("ocua_core_pulse"); ship.getVariant().removePermaMod("ocua_core_vapor"); }
                
                if (ship.getVariant().getHullMods().contains("ocua_core_vapor") && ship.getVariant().getHullMods().contains("ocua_core_catalyst")){
                    ship.getVariant().addMod(OCUA_HullMods.OCUA_C_MI_VP);
                    ship.getVariant().removeMod("ocua_core_vapor"); ship.getVariant().removeMod("ocua_core_catalyst");
                    ship.getVariant().removePermaMod("ocua_core_vapor"); ship.getVariant().removePermaMod("ocua_core_catalyst"); }
            }
            
            if (ship.getVariant().getHullMods().contains("civgrade")) {
                if (!ship.getVariant().getHullMods().contains("militarized_subsystems") && 
                    (ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_CORE_PULSE) || 
                    ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_C_MI_CHPL) || 
                    ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_C_MI_CRPL) || 
                    ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_C_MI_QUPL) || 
                    ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_C_MI_PL) || 
                    ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_C_MI_PLVP))     ){
                ship.getVariant().removeMod(OCUA_HullMods.OCUA_CORE_PULSE); 
                ship.getVariant().removeMod(OCUA_HullMods.OCUA_C_MI_CHPL);
                ship.getVariant().removeMod(OCUA_HullMods.OCUA_C_MI_CRPL);
                ship.getVariant().removeMod(OCUA_HullMods.OCUA_C_MI_QUPL);
                ship.getVariant().removeMod(OCUA_HullMods.OCUA_C_MI_PL);
                ship.getVariant().removeMod(OCUA_HullMods.OCUA_C_MI_PLVP);
                ship.getVariant().removePermaMod(OCUA_HullMods.OCUA_CORE_PULSE); 
                ship.getVariant().removePermaMod(OCUA_HullMods.OCUA_C_MI_CHPL);
                ship.getVariant().removePermaMod(OCUA_HullMods.OCUA_C_MI_CRPL);
                ship.getVariant().removePermaMod(OCUA_HullMods.OCUA_C_MI_QUPL);
                ship.getVariant().removePermaMod(OCUA_HullMods.OCUA_C_MI_PL);
                ship.getVariant().removePermaMod(OCUA_HullMods.OCUA_C_MI_PLVP);
                }
            }
                
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
                //if (ship.getVariant().getHullMods().contains("ocua_core_quantix") && !ship.getVariant().getHullMods().contains("ocua_baseless_module")) {
                    //OCUA_QuantixHullmodScript.showQuantixMod(ship);
                    //ship.getVariant().addMod("ocua_quantix_strike_hub");
                //}
                //else {
                    //ship.getVariant().removeMod("ocua_quantix_strike_hub");
                //}
            
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
                tooltip.addPara("\"I'll never get used to seeing these ships out of the assembly block. Usually.\"", gray, opad);
                tooltip.addPara(" - Arc Mother-01 EI", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Innate Features", Alignment.MID, opad);
                if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)){
                    bullet = tooltip.addPara("Can install %s %s %s. Mikanate override detected.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "2", "Oculian", "configuration cores");
                    bullet.setHighlight("2", "Oculian", "configuration cores", "Mikanate");
                    bullet.setHighlightColors(good, ocua, h, ocua_mi);
                } else {
                    bullet = tooltip.addPara("Can install %s %s %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "1", "Oculian", "configuration core" );
                    bullet.setHighlight("1", "Oculian", "configuration core");
                    bullet.setHighlightColors(good, ocua, h);
                }
                
                bullet = tooltip.addPara("EMP damage resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) EMP_REDUCTION + "%" );
                bullet = tooltip.addPara("Shield Energy damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 *(ENERGY_DAMAGE_MULT)) + "%" );
                //bullet.setHighlight("-" + (int) (100 *(ENERGY_DAMAGE_MULT)) + "%");
                //bullet.setHighlightColors(good);
                //bullet = tooltip.addPara("High Explosive damage taken %s, before modifiers.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                //    (int) (100 - (100 * (EXPLOSIVE_TAKEN_MULT))) + "%" );
                //bullet = tooltip.addPara("Total Kinetic Armor damage taken %s, before modifiers.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                //    (int) ((100 + (100 * KINETIC_ARMOR_TAKEN_MULT)) / 2) + "%" );
		//bullet.setHighlight("Armor", (int) ((100 + (100 * KINETIC_ARMOR_TAKEN_MULT)) / 2) + "%");
		//bullet.setHighlightColors(h, bad);
                bullet = tooltip.addPara("Auto-Aim accuracy %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) AUTO_AIM_BONUS + "%" );
                bullet = tooltip.addPara("Beam Turret turn rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) TURRET_SPEED_BONUS + "%" );
                bullet = tooltip.addPara("Shield Fold/Unfold speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 *(SHIELD_BONUS_MOVEMENT - 1)) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 * (SUPPLY_USE_MULT))) + "%" );
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
                bullet = tooltip.addPara("Ordinance cost of Non-Oculian Weapons %s, depending on slot size.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+3/+6/+10" );
		bullet.setHighlight("Oculian", "+3/+6/+10");
		bullet.setHighlightColors(ocua, bad);
                bullet = tooltip.addPara("Ordinance cost of Non-Oculian LPCs %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+10" );
		bullet.setHighlight("Oculian", "+10");
		bullet.setHighlightColors(ocua, bad);
                bullet = tooltip.addPara("Cannot install Expanded Deck Crew, Safety Overrides, Converted Hangars and Advanced Optics hullmods.%s", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "" );
		bullet.setHighlight("Expanded Deck Crew", "Safety Overrides", "Converted Hangars", "Advanced Optics");
		bullet.setHighlightColors(h, h, h);
                
            tooltip.setBulletedListMode(null);
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
		//if(index == 0) return "" + (int) AUTO_AIM_BONUS + "%";
		//if(index == 1) return "" + (int) TURRET_SPEED_BONUS + "%";
	//if(index == 1) return "" + (int) EMP_REDUCTION + "%";
	//if(index == 2) return "" + (int) (100 *(SHIELD_BONUS_MOVEMENT - 1)) + "%";
	//if(index == 3) return "" + (int) (100 *(ENERGY_DAMAGE_MULT)) + "%";
	//if(index == 4) return "1/2/4";
	//if(index == 5) return "20%";
		//if(index == 5) return "" + (int) ((100 * (ANTIMISSILE_DAMAGE_MULT)) - 100) + "%";
                
	//if(index == 6) return "" + (int) (100 - (100 * (SUPPLY_USE_MULT))) + "%";
                
	//if(index == 7) return "" + "Expanded Deck Crew, Safety Overrides and Converted Hangars";
	//if(index == 8) return "3/6/10";
	//if(index == 9) return "10";
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
