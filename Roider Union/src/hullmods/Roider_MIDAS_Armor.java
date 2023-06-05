package hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import ids.Roider_Ids.Roider_Hullmods;
import java.util.*;
import org.magiclib.util.MagicIncompatibleHullmods;

// Copied from SCY_armorChild and modified

public class Roider_MIDAS_Armor extends BaseHullMod {

    private static final String ID = "roider_MIDAS_Armor";

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);

        ShipAPI parent = null;// ship.getParentStation();
        ShipVariantAPI variant = stats.getVariant();

        if (parent == null) {
            // Get parent's hullmods
            Set<String> mods = HULLMODS.get(PARENT_HULLS.get(variant.getHullSpec().getBaseHullId()));
            Set<String> sMods = S_HULLMODS.get(PARENT_HULLS.get(variant.getHullSpec().getBaseHullId()));
            if (mods == null) return;

            // Remove old hullmods
            Collection<String> toRemove = new ArrayList<>();
            for (String mod : variant.getHullMods()) {
                if (!mod.equals(Roider_Hullmods.MIDAS_ARMOR)) {
                    toRemove.add(mod);
                }
            }

            variant.getHullMods().removeAll(toRemove);

            // Add new hullmods
            for (String mod : mods) {
//                if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
                if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

                // Switch to armor module version if hullmod uses
                // size-dependant changes.
                if (HULLMOD_SWITCH.containsKey(mod)) {
                    variant.addPermaMod(HULLMOD_SWITCH.get(mod), sMods.contains(mod));
                    continue;
                }

                variant.addPermaMod(mod, sMods.contains(mod));
            }
        } else {
            // Get parent's hullmods
            Set<String> mods = new HashSet<>();
            Set<String> sMods = new HashSet<>();
            for (String mod : parent.getVariant().getNonBuiltInHullmods()) {
    //            if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
                if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

                mods.add(mod);
            }

            for (String mod : parent.getVariant().getPermaMods()) {
    //            if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
                if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

                HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(mod);

                if (modSpec.hasTag(Tags.HULLMOD_DAMAGE)) mods.add(mod);
            }

            for (String mod : parent.getVariant().getSMods()) {
    //            if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
                if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

                mods.add(mod);
                sMods.add(mod);
            }

            // Remove old hullmods
            Collection<String> toRemove = new ArrayList<>();
            for (String mod : variant.getHullMods()) {
                if (!mod.equals(Roider_Hullmods.MIDAS_ARMOR)) {
                    toRemove.add(mod);
                }
            }

            variant.getHullMods().removeAll(toRemove);

            // Add new hullmods
            for (String mod : mods) {
//                if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
                if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

                // Switch to armor module version if hullmod uses
                // size-dependant changes.
                if (HULLMOD_SWITCH.containsKey(mod)) {
                    variant.addPermaMod(HULLMOD_SWITCH.get(mod), sMods.contains(mod));
                    continue;
                }

                variant.addPermaMod(mod, sMods.contains(mod));
            }
        }

        variant.computeHullModOPCost();
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod(Roider_Hullmods.EXTREME_MODS)) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),
                    Roider_Hullmods.EXTREME_MODS, Roider_Hullmods.MIDAS_ARMOR);
        }

        if (ship.getVariant().hasHullMod(Roider_Hullmods.FIGHTER_CLAMPS)) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),
                    Roider_Hullmods.FIGHTER_CLAMPS, Roider_Hullmods.MIDAS_ARMOR);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="unused">
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

//        if (Global.getCombatEngine().getTotalElapsedTime(false) <= 2.05 && Global.getCombatEngine().getTotalElapsedTime(false) > 2) {
//            if (ship.getParentStation() != null && ship.getParentStation().isAlive()) {
//
//                ShipAPI parent = ship.getParentStation();
//                MutableShipStatsAPI sStats = ship.getMutableStats();
//                MutableShipStatsAPI pStats = parent.getMutableStats();
//
//                float hull = ship.getHullSpec().getHitpoints();
//                hull *= pStats.getHullBonus().getBonusMult();
//                hull += pStats.getHullBonus().getFlatBonus();
//                hull = ship.getHullSpec().getHitpoints() / hull;
//
//                sStats.getHullDamageTakenMult().modifyMult(ID + "_0", pStats.getHullDamageTakenMult().getModifiedValue());
//                sStats.getHullDamageTakenMult().modifyMult(ID + "_1", hull);
//
//                float armor = ship.getHullSpec().getArmorRating();
//                armor *= pStats.getArmorBonus().getBonusMult();
//                armor += pStats.getArmorBonus().getFlatBonus();
//                armor = ship.getHullSpec().getArmorRating() / armor;
//
//                sStats.getArmorDamageTakenMult().modifyMult(ID + "_0", pStats.getArmorDamageTakenMult().getModifiedValue());
//                sStats.getArmorDamageTakenMult().modifyMult(ID + "_1", armor);
//
////                sStats.getBallisticWeaponRangeBonus().modifyMult(ID, pStats.getBallisticWeaponRangeBonus().getBonusMult());
////                sStats.getBallisticWeaponRangeBonus().modifyFlat(ID, pStats.getBallisticWeaponRangeBonus().getFlatBonus());
////
////                sStats.getEnergyWeaponRangeBonus().modifyMult(ID, pStats.getEnergyWeaponRangeBonus().getBonusMult());
////                sStats.getEnergyWeaponRangeBonus().modifyFlat(ID, pStats.getEnergyWeaponRangeBonus().getFlatBonus());
////
////                sStats.getBeamWeaponRangeBonus().modifyMult(ID, pStats.getBeamWeaponRangeBonus().getBonusMult());
////                sStats.getBeamWeaponRangeBonus().modifyFlat(ID, pStats.getBeamWeaponRangeBonus().getFlatBonus());
////
////                sStats.getAutofireAimAccuracy().modifyMult(ID, pStats.getAutofireAimAccuracy().computeMultMod());
//
//                sStats.getFragmentationDamageTakenMult().modifyMult(ID, pStats.getFragmentationDamageTakenMult().getModifiedValue());
//                sStats.getEnergyDamageTakenMult().modifyMult(ID, pStats.getEnergyDamageTakenMult().getModifiedValue());
//                sStats.getKineticDamageTakenMult().modifyMult(ID, pStats.getKineticDamageTakenMult().getModifiedValue());
//                sStats.getHighExplosiveDamageTakenMult().modifyMult(ID, pStats.getHighExplosiveDamageTakenMult().getModifiedValue());
//                sStats.getEmpDamageTakenMult().modifyMult(ID, pStats.getEmpDamageTakenMult().getModifiedValue());
//            }
//        }
    }
//</editor-fold>


    // Ship id and list of hullmods
    // Probably needs to be cleared periodically
    public final static transient Map<String, Set> HULLMODS = new HashMap<>();
    public final static transient Map<String, Set> S_HULLMODS = new HashMap<>();

    public final static Map<String, String> PARENT_HULLS = new HashMap<>();
    static {
        PARENT_HULLS.put("roider_roach_armor", "roider_roach");
        PARENT_HULLS.put("roider_onager_armor", "roider_onager");
        PARENT_HULLS.put("roider_aurochs_armor", "roider_aurochs");
        PARENT_HULLS.put("roider_firestorm_left", "roider_firestorm");
        PARENT_HULLS.put("roider_firestorm_right", "roider_firestorm");
        PARENT_HULLS.put("roider_gambit_armor", "roider_gambit");
        PARENT_HULLS.put("roider_ranch_armor", "roider_ranch");
        PARENT_HULLS.put("roider_wrecker_armor", "roider_wrecker");
        PARENT_HULLS.put("roider_telamon_front", "roider_telamon");
        PARENT_HULLS.put("roider_telamon_left", "roider_telamon");
        PARENT_HULLS.put("roider_telamon_right", "roider_telamon");
    }

    // Some hullmods have size-dependant effects, so they need to be
    // switched to armor module versions
    public final static Map<String, String> HULLMOD_SWITCH = new HashMap<>();
    static {
        HULLMOD_SWITCH.put(HullMods.HEAVYARMOR, Roider_Hullmods.HEAVY_ARMOR);
//        HULLMOD_SWITCH.put("CHM_hegemony", Roider_Hullmods.HEG_COM_CREW);
//        HULLMOD_SWITCH.put("CHM_hegemony_xiv", Roider_Hullmods.HEG_XIV_COM_CREW);
//        HULLMOD_SWITCH.put("CHM_pirate_xiv", Roider_Hullmods.HEG_COM_CREW);
    }

//    public final static Set<String> ALLOWED = new HashSet<>();
//    static {
//        ALLOWED.add(HullMods.ARMOREDWEAPONS);
//        ALLOWED.add(HullMods.BLAST_DOORS);
//        ALLOWED.add(HullMods.COMP_ARMOR);
//        ALLOWED.add(HullMods.COMP_HULL);
//        ALLOWED.add(HullMods.COMP_STRUCTURE);
//        ALLOWED.add(HullMods.HEAVYARMOR);
//        ALLOWED.add(HullMods.INSULATEDENGINE);
//        ALLOWED.add(HullMods.MILITARIZED_SUBSYSTEMS);
//        ALLOWED.add(HullMods.REINFORCEDHULL);
//        ALLOWED.add(HullMods.SOLAR_SHIELDING);
//
//        // Commissioned Crews
//        ALLOWED.add("CHM_hegemony");
//        ALLOWED.add("CHM_hegemony_xiv");
//        ALLOWED.add("CHM_pirate_xiv");
//
//        // SCY
//        ALLOWED.add("SCY_lightArmor");
//        ALLOWED.add("SCY_reactiveArmor");
//
//        // Shadowyards
//        ALLOWED.add("ms_fluxLock");
//
//        // Underworld
//        ALLOWED.add("uw_cabal_upgrades");
//    }

    public final static Set<String> BLOCKED = new HashSet<>();
    static {
        BLOCKED.add(Roider_Hullmods.FIGHTER_CLAMPS);
        BLOCKED.add(Roider_Hullmods.MIDAS);
        BLOCKED.add(Roider_Hullmods.MIDAS_1);
        BLOCKED.add(Roider_Hullmods.MIDAS_2);
        BLOCKED.add(Roider_Hullmods.MIDAS_3);
        BLOCKED.add(HullMods.CONVERTED_BAY);
        BLOCKED.add(HullMods.CONVERTED_HANGAR);
        BLOCKED.add(HullMods.ACCELERATED_SHIELDS);
        BLOCKED.add(HullMods.ADDITIONAL_BERTHING);
        BLOCKED.add(HullMods.ADVANCEDOPTICS);
        BLOCKED.add(HullMods.ADVANCED_TARGETING_CORE);
        BLOCKED.add(HullMods.AUGMENTEDENGINES);
        BLOCKED.add(HullMods.AUTOMATED);
        BLOCKED.add(HullMods.AUTOREPAIR);
        BLOCKED.add(HullMods.AUXILIARY_FUEL_TANKS);
        BLOCKED.add(HullMods.AUXILIARY_THRUSTERS);
        BLOCKED.add(HullMods.CIVGRADE);
        BLOCKED.add(HullMods.COMP_STORAGE);
        BLOCKED.add(HullMods.DAMAGED_DECK);
        BLOCKED.add(HullMods.DEDICATED_TARGETING_CORE);
        BLOCKED.add(HullMods.DESTROYED_MOUNTS);
        BLOCKED.add(HullMods.ECCM);
        BLOCKED.add(HullMods.ECM);
        BLOCKED.add(HullMods.EFFICIENCY_OVERHAUL);
        BLOCKED.add(HullMods.ERRATIC_INJECTOR);
        BLOCKED.add(HullMods.EXPANDED_CARGO_HOLDS);
        BLOCKED.add(HullMods.EXPANDED_DECK_CREW);
        BLOCKED.add(HullMods.EXTENDED_SHIELDS);
        BLOCKED.add(HullMods.FAULTY_GRID);
        BLOCKED.add(HullMods.FLUXBREAKERS);
        BLOCKED.add(HullMods.FLUX_COIL);
        BLOCKED.add(HullMods.FLUX_DISTRIBUTOR);
        BLOCKED.add(HullMods.FRAGILE_SUBSYSTEMS);
        BLOCKED.add(HullMods.FRONT_SHIELD_CONVERSION);
        BLOCKED.add(HullMods.GLITCHED_SENSORS);
        BLOCKED.add(HullMods.HARDENED_SHIELDS);
        BLOCKED.add(HullMods.HARDENED_SUBSYSTEMS);
        BLOCKED.add(HullMods.ILL_ADVISED);
        BLOCKED.add(HullMods.INCREASED_MAINTENANCE);
        BLOCKED.add(HullMods.INTEGRATED_TARGETING_UNIT);
        BLOCKED.add(HullMods.MAGAZINES);
        BLOCKED.add(HullMods.MAKESHIFT_GENERATOR);
        BLOCKED.add(HullMods.MALFUNCTIONING_COMMS);
//        BLOCKED.add(HullMods.MILITARIZED_SUBSYSTEMS);
        BLOCKED.add(HullMods.MISSLERACKS);
        BLOCKED.add(HullMods.NAV_RELAY);
        BLOCKED.add(HullMods.OMNI_SHIELD_CONVERSION);
        BLOCKED.add(HullMods.OPERATIONS_CENTER);
        BLOCKED.add(HullMods.PHASE_FIELD);
        BLOCKED.add(HullMods.POINTDEFENSEAI);
        BLOCKED.add(HullMods.RECOVERY_SHUTTLES);
        BLOCKED.add(HullMods.SAFETYOVERRIDES);
        BLOCKED.add(HullMods.SHIELDED_CARGO_HOLDS);
        BLOCKED.add(HullMods.SOLAR_SHIELDING);
        BLOCKED.add(HullMods.STABILIZEDSHIELDEMITTER);
        BLOCKED.add(HullMods.SURVEYING_EQUIPMENT);
        BLOCKED.add(HullMods.TURRETGYROS);
        BLOCKED.add(HullMods.UNSTABLE_COILS);
        BLOCKED.add(HullMods.UNSTABLE_INJECTOR);
        BLOCKED.add("fluxshunt");
        BLOCKED.add("high_maintenance");
        BLOCKED.add("delicate");

//      // Commissioned Crews
        BLOCKED.add("CHM_commission");
        BLOCKED.add("CHM_commission2");
        BLOCKED.add("CHM_alliance");
        BLOCKED.add("CHM_alliance2");
        BLOCKED.add("CHM_persean_league");
        BLOCKED.add("CHM_tritachyon");
        BLOCKED.add("CHM_sindrian");
        BLOCKED.add("CHM_sindrian1");
        BLOCKED.add("CHM_sindrian2");
        BLOCKED.add("CHM_sindrian3");
        BLOCKED.add("CHM_pirate");
        BLOCKED.add("CHM_luddic_church");
        BLOCKED.add("CHM_pather");

        // Ship And Weapon Pack
        BLOCKED.add(Roider_Hullmods.EXTREME_MODS);
        BLOCKED.add("swp_shieldbypass");
    }
}
