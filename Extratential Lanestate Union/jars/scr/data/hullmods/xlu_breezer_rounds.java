package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lwjgl.util.vector.Vector2f;

public class xlu_breezer_rounds extends BaseHullMod {

        public static final float FRAG_BONUS = 1.2f;
        public static final float FRAG_RNG_BONUS = 1.15f;
        
        public static final float SMODIFIER = 0.05f;
        
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            boolean sMod = isSMod(stats);
            
            stats.getBallisticWeaponDamageMult().modifyMult(id, 1f + (sMod ? SMODIFIER : 0));
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (sMod ? SMODIFIER : 0));
            
            stats.addListener(new WeaponOPCostModifier() {
                @Override
                public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
                        if (weapon.getType().equals(WeaponType.BALLISTIC) && (weapon.getDamageType().equals(DamageType.FRAGMENTATION))) {
                            if (weapon.getSize().equals(WeaponSize.SMALL)) return (currCost + (int) 1); 
                            if (weapon.getSize().equals(WeaponSize.MEDIUM)) return (currCost + (int) 2);
                            if (weapon.getSize().equals(WeaponSize.LARGE)) return (currCost + (int) 4);
                        }
                    return currCost;
                }
            });
        }
        
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new xlu_BreezerRoundDamageDealtMod());
		ship.addListener(new WeaponRangeModifier() {
                        @Override
			public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
				return 0;
			}
                        @Override
			public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
				if (weapon.getType().equals(WeaponType.BALLISTIC) && weapon.getDamageType().equals(DamageType.FRAGMENTATION) &&
                                        weapon.getSpec().getMaxRange() <= 700) {
					return 1f * FRAG_RNG_BONUS;
				}
				return 1f;
			}
                        @Override
			public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
				return 0f;
			}
		});
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

	public static class xlu_BreezerRoundDamageDealtMod implements DamageDealtModifier {
                @Override
		public String modifyDamageDealt(Object param,
			CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean Hit) {
                            
			WeaponAPI weapon = null;
			if (param instanceof DamagingProjectileAPI) {
				weapon = ((DamagingProjectileAPI)param).getWeapon();
			} else if (param instanceof BeamAPI) {
				weapon = ((BeamAPI)param).getWeapon();
			} else if (param instanceof MissileAPI) {
				weapon = ((MissileAPI)param).getWeapon();
			}
                        
			if (weapon == null) return null;
			if (!weapon.getType().equals(WeaponType.BALLISTIC)) return null;
			if (!weapon.getDamageType().equals(DamageType.FRAGMENTATION)) return null;

                            String id = "xlu_breezer_frag";
                            damage.getModifier().modifyMult(id, FRAG_BONUS);
                        
                            //damArmor = damage.getType().getArmorMult()* FRAG_PEN;
                            //damage.getType().setArmorMult(damArmor);
                            //float damflux = damage.getFluxComponent() * FRAG_FLUX_PENALTY;
                            //damage.setFluxComponent(damflux);
                        
                            
			return id;
		}
	}
        
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("xlu_breezer_rounds")) return true; // can always add

		for (String slotId : ship.getVariant().getNonBuiltInWeaponSlots()) {
			WeaponSpecAPI spec = ship.getVariant().getWeaponSpec(slotId);
			if (spec.getType().equals(WeaponType.BALLISTIC) && spec.getDamageType().equals(DamageType.FRAGMENTATION)) return false;
		}
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		return "Ballistic Fragmentations must be remove before uninstalling the manufactories";
	}
        
        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
            if(index == 0) return "" + (int) (FRAG_BONUS * 100 - 100f)  + "%";
            if(index == 1) return "" + (float) FRAG_RNG_BONUS  + "x";
            if(index == 2) return "1/2/4";
            return null;
        }
        
        @Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
            if (index == 0) return "" + (int) Math.round(SMODIFIER * 100f) + "%";
            return null;
	}
	
}