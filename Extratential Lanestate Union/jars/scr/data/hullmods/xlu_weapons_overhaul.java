package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.XLU_HullMods;
import java.awt.Color;

public class xlu_weapons_overhaul extends BaseHullMod {

        public static final float BALLISTIC_BONUS = 25f;
        public static final float BALLISTIC_AMMO = 50f;
        public static final float BALLISTIC_FLUX = 25f;
        public static final float FLUX_PENALTY = 20f;
        public static final float BOOSTER_DAMAGE = 10f;
        //public static final float TURRET_TURN_BONUS = 33f;
        
        public static final float SHIELD_SHUNT_AMMO = 50f;
        
    private String ERROR = "xlu_missing";
    private String MOD = "xlu_weapons_overhaul";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
            float AmmoBonus = 0f;

            if (!stats.getVariant().getHullMods().contains("xlu_breezer_rounds")) AmmoBonus = AmmoBonus + BALLISTIC_AMMO;
            if (stats.getVariant().getHullMods().contains("shield_shunt") || 
                (stats.getVariant().getHullSpec().getShieldType().equals(ShieldAPI.ShieldType.NONE))) 
                AmmoBonus = AmmoBonus + SHIELD_SHUNT_AMMO;
            
            stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS);
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (BALLISTIC_FLUX / 100));
            stats.getFluxDissipation().modifyMult(id, 1f - (FLUX_PENALTY / 100));
            stats.getHitStrengthBonus().modifyPercent(id, BOOSTER_DAMAGE);
            
            stats.getBallisticAmmoBonus().modifyPercent(id, AmmoBonus);
    }
/*
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        
        for (String tmp : REQUIRED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) {                
                ship.getVariant().removeMod(MOD);      
                ship.getVariant().addMod(ERROR);
            }
        }
    }
*/	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if(index == 0) return "" + (int) BALLISTIC_BONUS + "%";
	if(index == 1) return "" + (int) BALLISTIC_AMMO + "%";
	if(index == 2) return "" + (int) BALLISTIC_FLUX + "%";
	if(index == 3) return "" + (int) FLUX_PENALTY + "%";
	if(index == 4) return "" + (int) BOOSTER_DAMAGE + "%";
	if(index == 5) return "Ammo capacity bonus will not be applied";
	if(index == 6) return "" + (int) SHIELD_SHUNT_AMMO + "%";
        
        return null;
    }

        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                Color gray = Misc.getGrayColor();
                Color xlu = new Color(140,160,255,255);
                float ammo_cap = BALLISTIC_AMMO;
                
                if (!(isForModSpec || ship == null)) {
                if (!ship.getVariant().getHullMods().contains("xlu_breezer_rounds") && 
                        (ship.getVariant().getHullMods().contains("shield_shunt") || ship.getVariant().getHullSpec().getShieldType() == ShieldAPI.ShieldType.NONE)) {
                        ammo_cap = BALLISTIC_AMMO * 2f;
                    } else {
                        ammo_cap = BALLISTIC_AMMO;
                    }
                }
                
                LabelAPI bullet;
                tooltip.addPara("\"Ever wondered why most of the stars are orange? You'll find out.\"", gray, opad);
                tooltip.addPara(" - Harold Masters XII, an XLU weapons meister", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Perks", Alignment.MID, opad);
                bullet = tooltip.addPara("Ballistic weapon firing speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) BALLISTIC_BONUS + "%" );
                bullet = tooltip.addPara("Ballistic weapon flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) BALLISTIC_FLUX + "%" );
                if (!(isForModSpec && ship == null)) {
                    if (!(ship.getVariant().getHullMods().contains("xlu_breezer_rounds") && 
                        !(ship.getVariant().getHullMods().contains("shield_shunt") || ship.getVariant().getHullSpec().getShieldType() == ShieldAPI.ShieldType.NONE))){
                    bullet = tooltip.addPara("Ballistic weapon ammo capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                        "+" + (int) ammo_cap + "%" );
                    }
                } else {
                    bullet = tooltip.addPara("Ballistic weapon ammo capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                        "+" + (int) ammo_cap + "%" );
                }
                
		tooltip.addSectionHeading("Quirks", Alignment.MID, opad);
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) FLUX_PENALTY + "%" );
                
		tooltip.addSectionHeading("Irks", Alignment.MID, opad);
                bullet = tooltip.addPara("Exclusive to XLU ships.%s", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), gray,
                    "" );
		bullet.setHighlight("XLU");
		bullet.setHighlightColors(xlu);
                bullet = tooltip.addPara("If ship has no Shield or Shield Shunt is installed, another Ballistic ammo bonus is incurred.%s", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), gray,
                    "" );
		bullet.setHighlight("Shield Shunt", "Initially unshielded ships receive the effect unconditionally.");
		bullet.setHighlightColors(h, gray);
                bullet = tooltip.addPara("If Lanestate Breezer Rounds is installed, the initial ammo bonus is removed.%s", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), gray,
                    "" );
		bullet.setHighlight("Lanestate Breezer Rounds");
		bullet.setHighlightColors(h);
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getVariant().hasHullMod(XLU_HullMods.XLU_ARMORCLAD) ||
                       ship.getVariant().hasHullMod(XLU_HullMods.XLU_LITHOFRAME)) && super.isApplicableToShip(ship);
        }
    

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || !ship.getVariant().getHullMods().contains("xlu_armorclad")) {
            return "XLU Armorclad Works hullmod Required";
        }
        
        return null;
    }
}
