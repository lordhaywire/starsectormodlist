package hullmods;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids.Roider_Hullmods;
import java.awt.Color;
import java.util.*;
import org.magiclib.terrain.MagicAsteroidBeltTerrainPlugin;
import scripts.campaign.cleanup.Roider_MadMIDASHealer;
import scripts.campaign.rulecmd.Roider_MadRockpiper;

/**
 * Author: SafariJohn
 */
public class Roider_MIDAS extends BaseHullMod implements HullModFleetEffect {
    public static final String MOD_ID = "roider_MIDAS_impact";
    public static final String RECENT_IMPACT = "$recentImpact";
    public static final String NEGATED_IMPACT = "$roider_MIDAS_negatation";

    public static final float MAX_IMPACT_RESIST = 90f;
    public static final float EMP_REDUCTION = 15f;
    public static final float MASS_BONUS = 10f;

    public static final float MODULE_ARMOR_MULT = 1.5f;
    public static final float MODULE_HULL_MULT = 0.2f;

    // Resist impacts from asteroid belts and fields on the campaign map
    // Heavily modified from MesoTroniK's TiandongRetrofit hullmod
    public void advanceInCampaign(CampaignFleetAPI fleet) {
        FleetDataAPI fleetData = fleet.getFleetData();
//        MutableStat nav = fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT);
//        MemoryAPI mem = fleet.getMemoryWithoutUpdate();
//
//        boolean hadRecent = mem.is(RECENT_IMPACT, true);
//        boolean recentlyNegated = mem.is(NEGATED_IMPACT, true);
//
//        boolean impactInProgress = fleet.hasScriptOfClass(MagicAsteroidImpact.class)
//                    || fleet.hasScriptOfClass(AsteroidImpact.class);
//
//        // Skip if there is nothing to do this advance
//        if (!hadRecent && !impactInProgress) {
//            nav.unmodifyMult(MOD_ID);
//            return;
//        }

        //<editor-fold defaultstate="collapsed" desc="unused">
//        HashMap<String, StatMod> mods = fleetStats.getFleetwideMaxBurnMod().getMultBonuses();
//        float magnitude = 1f;

//        for (Map.Entry<String, StatMod> mod : mods.entrySet()) {
//            StatMod statMod = mod.getValue();
//            if (statMod.desc == null) continue;
//
//            if (statMod.desc.contentEquals("Inside asteroid belt")
//                        || statMod.desc.contentEquals("Inside asteroid field")) {
//                asteroidMod = statMod;
//            } else {
//                continue;
//            }
//
//            magnitude *= asteroidMod.getValue();
//        }
//</editor-fold>

        float contributingSize = 0f;
        float totalSize = 0f;
        for (FleetMemberAPI fleetMember : fleetData.getMembersListCopy()) {
            // Lurking NoClassDefFoundError...
            // No idea what is going on, so just going to catch it and continue.
            try {
                float size;
                switch (fleetMember.getHullSpec().getHullSize()) {
                    case FIGHTER:
                    case FRIGATE:
                    default:
                        size = 1f;
                        break;
                    case DESTROYER:
                        size = 2f;
                        break;
                    case CRUISER:
                        size = 4f;
                        break;
                    case CAPITAL_SHIP:
                        size = 8f;
                        break;
                }
                totalSize += size;
                if (hasMIDAS(fleetMember.getVariant())) {
                    contributingSize += size;
                }
            } catch (NoClassDefFoundError er) {
                Global.getLogger(Roider_MIDAS.class).error(er);
            }
        }

        float contribution = contributingSize / totalSize;
        float magnitudeMult = 1f - Math.min(contribution, (MAX_IMPACT_RESIST / 100f));

        // MagicAsteroids time
        // Reduce chance of damaging impact
        MutableStat chanceMod = fleet.getCommanderStats().getDynamic().getStat(MagicAsteroidBeltTerrainPlugin.IMPACT_DAMAGE_CHANCE);
        if (chanceMod != null) {
            chanceMod.modifyMult(MOD_ID, 1f - contribution);
        }

        // Reduce pushing effect of impact
        MutableStat pushMod = fleet.getCommanderStats().getDynamic().getStat(MagicAsteroidBeltTerrainPlugin.IMPACT_FORCE);
        if (pushMod != null) {
            pushMod.modifyMult(MOD_ID, magnitudeMult);
        }


//        // Reduce chance of damaging impact
//        // by randomly setting RECENT_IMPACT to false based on
//        // number of ships with MIDAS
//        if (hadRecent && !recentlyNegated && !impactInProgress) {
//            mem.set(RECENT_IMPACT, Math.random() > contribution);
//            mem.set(NEGATED_IMPACT, true);
//            return;
//        } else if (hadRecent && recentlyNegated) {
//            mem.set(NEGATED_IMPACT, false);
//        }
//
//        // Clean up impact mod
//        if (!impactInProgress) {
//            nav.unmodifyMult(MOD_ID);
//            return;
//        }
//
//        // Reduce pushing effect of an impact
//        nav.modifyMult(MOD_ID, magnitudeMult, "MIDAS");
//        mem.set(NEGATED_IMPACT, false);
    }

    public static boolean hasMIDASStatic(ShipVariantAPI variant) {
        return variant.hasHullMod(Roider_Hullmods.MIDAS)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_1)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_2)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_3);
    }

    private boolean hasMIDAS(ShipVariantAPI variant) {
        return hasMIDASStatic(variant);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEmpDamageTakenMult().modifyMult(MOD_ID, 1f - EMP_REDUCTION / 100f);

        ShipVariantAPI variant = stats.getVariant();

        // Reduce campaign asteroid impact damage
//        MutableStat memberDamageMod = stats.getDynamic().getStat(MagicAsteroidBeltTerrainPlugin.IMPACT_DAMAGE);
//        if (memberDamageMod != null) {
//            memberDamageMod.modifyMult(id, 1f - MAX_IMPACT_RESIST / 100f);
//        }

        // Built-in wing switching for Sheriff, Cowboy, and Ranch is handled here
        String hullId = variant.getHullSpec().getBaseHullId();
        if (Roider_TrackerSwap.WINGS_PER_SHIP.containsKey(hullId)) {
            String wingId = "roider_breaker_wing";

            if (variant.hasHullMod(Roider_Hullmods.TRACKER_SWAP)) {
                wingId = "roider_tracker_wing";
            }

            if (variant.hasHullMod(Roider_Hullmods.GLITZ_SWITCH)) {
                wingId = "roider_glitz_wing";
            }

            int wingCount = Roider_TrackerSwap.WINGS_PER_SHIP.get(hullId);

            if (stats.getNumFighterBays().getModifiedInt() == 0) wingCount = 0;

            List wings = variant.getWings();
            if (wings.size() < wingCount) wingCount = wings.size();

            for (int i = 0; i < wingCount; i++) {
                wings.set(i, wingId);
            }
        }

        // Armor module hullmod transfer logic below
        if (variant.hasHullMod(Roider_Hullmods.MIDAS)) return;

        // Saving hullmods for transfer to armor module
        Set<String> mods = new HashSet<>();
        Set<String> sMods = new HashSet<>();
        for (String mod : variant.getNonBuiltInHullmods()) {
//            if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
            if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

            mods.add(mod);
        }

        for (String mod : variant.getPermaMods()) {
//            if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
            if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(mod);

            if (modSpec.hasTag(Tags.HULLMOD_DAMAGE)) mods.add(mod);
        }

        for (String mod : variant.getSMods()) {
//            if (!Roider_MIDAS_Armor.ALLOWED.contains(mod)) continue;
            if (Roider_MIDAS_Armor.BLOCKED.contains(mod)) continue;

            mods.add(mod);
            sMods.add(mod);
        }

        Roider_MIDAS_Armor.HULLMODS.put(variant.getHullSpec().getBaseHullId(), mods);
        Roider_MIDAS_Armor.S_HULLMODS.put(variant.getHullSpec().getBaseHullId(), sMods);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.setMass(ship.getMass() * (1f + MASS_BONUS / 100f));

        if (Roider_MadMIDASHealer.isMadMidas(ship.getFleetMemberId())) {
            ShipVariantAPI variant = ship.getVariant();

            if (variant != null && !Roider_MadRockpiper.hasBuiltInMIDAS(variant)) {
                if (variant.getSMods().contains(Roider_Hullmods.MIDAS)) variant.removePermaMod(Roider_Hullmods.MIDAS);
                variant.addPermaMod(Roider_Hullmods.MIDAS, false);
            }
        }
    }


    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) MAX_IMPACT_RESIST + "%";
        if (index == 1) return "" + (int) EMP_REDUCTION + "%";
        if (index == 2) return "" + (int) MASS_BONUS + "%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (ship == null) return;
        if (ship.getVariant() == null) return;
        if (!hasMIDAS(ship.getVariant())) return;
        if (ship.getVariant().hasHullMod(Roider_Hullmods.MIDAS)) return;
        if (ship.getVariant().hasHullMod(Roider_Hullmods.MIDAS_ARMOR)) return;

        float pad = 10f;

        String hullId = ship.getHullSpec().getBaseHullId();

        // Handle special armor and hull cases
        float armorMult = MODULE_ARMOR_MULT;
        if (hullId.equals("roider_telamon")) armorMult = 1.6f;

        float hullMult = MODULE_HULL_MULT;
        if (hullId.equals("roider_firestorm")) hullMult = 0.12f;
        if (hullId.equals("roider_telamon")) hullMult = 0.15625f;

        List<String> hl = new ArrayList<>();
        List<Color> hlColors = new ArrayList<>();

        // Get armor bonus
        StatBonus armorBonus = ship.getMutableStats().getArmorBonus().createCopy();
        float baseArmor = ship.getHullSpec().getArmorRating() * armorMult;
        float modArmor = armorBonus.computeEffective(baseArmor);
        hl.add("" + Math.round(modArmor));
        hlColors.add(Misc.getHighlightColor());

        Color armorHL = modArmor > baseArmor ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
        if (modArmor != baseArmor) {
            if (modArmor > baseArmor) hl.add("(+" + Math.round(modArmor - baseArmor) + ")");
            else hl.add("(" + Math.round(modArmor - baseArmor) + ")");
            hlColors.add(armorHL);
        }

        // Get hull bonus
        StatBonus hullBonus = ship.getMutableStats().getHullBonus().createCopy();
        float baseHull = ship.getHullSpec().getHitpoints() * hullMult;
        float modHull = hullBonus.computeEffective(baseHull);
        hl.add("" + Math.round(modHull));
        hlColors.add(Misc.getHighlightColor());

        Color hullHL = modHull > baseHull ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
        if (modHull != baseHull) {
            if (modHull > baseHull) hl.add("(+" + Math.round(modHull - baseHull) + ")");
            else hl.add("(" + Math.round(modHull - baseHull) + ")");
            hlColors.add(hullHL);
        }

        if (hullId.equals("roider_telamon")) {
            float armorMult2 = 1.44f;
            float hullMult2 = 0.09375f;

            // Get second armor bonus
            StatBonus armorBonus2 = ship.getMutableStats().getArmorBonus().createCopy();
            float baseArmor2 = ship.getHullSpec().getArmorRating() * armorMult2;
            float modArmor2 = armorBonus2.computeEffective(baseArmor2);
            hl.add("" + Math.round(modArmor2));
            hlColors.add(Misc.getHighlightColor());

            if (modArmor2 != baseArmor2) {
                if (modArmor2 > baseArmor2) hl.add("(+" + Math.round(modArmor2 - baseArmor2) + ")");
                else hl.add("(" + Math.round(modArmor2 - baseArmor2) + ")");
                hlColors.add(armorHL);
            }

            // Get second hull bonus
            StatBonus hullBonus2 = ship.getMutableStats().getHullBonus().createCopy();
            float baseHull2 = ship.getHullSpec().getHitpoints() * hullMult2;
            float modHull2 = hullBonus2.computeEffective(baseHull2);
            hl.add("" + Math.round(modHull2));
            hlColors.add(Misc.getHighlightColor());

            if (modHull2 != baseHull2) {
                if (modHull2 > baseHull2) hl.add("(+" + Math.round(modHull2 - baseHull2) + ")");
                else hl.add("(" + Math.round(modHull2 - baseHull2) + ")");
                hlColors.add(hullHL);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Three armor modules form a tough outer shell.")
                        .append(" The main module has %s");

            if (modArmor != baseArmor) sb.append(" %s");

            sb.append(" armor and %s");

            if (modHull != baseHull) sb.append(" %s");

            sb.append(" hull.");

            sb.append("The sponson modules have %s");

            if (modArmor2 != baseArmor2) sb.append(" %s");

            sb.append(" armor and %s");

            if (modHull2 != baseHull2) sb.append(" %s");

            sb.append(" hull.");

            tooltip.addPara(sb.toString(), pad, hlColors.toArray(new Color[hlColors.size()]),
                        hl.toArray(new String[hl.size()]));
        } else if (hullId.equals("roider_firestorm")) {
            StringBuilder sb = new StringBuilder();
            sb.append("Two armor modules provide increased survivability, each with %s");

            if (modArmor != baseArmor) sb.append(" %s");

            sb.append(" armor and %s");

            if (modHull != baseHull) sb.append(" %s");

            sb.append(" hull.");

            tooltip.addPara(sb.toString(), pad, hlColors.toArray(new Color[hlColors.size()]),
                        hl.toArray(new String[hl.size()]));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("This ship also has an armor module with %s");

            if (modArmor != baseArmor) sb.append(" %s");

            sb.append(" armor and %s");

            if (modHull != baseHull) sb.append(" %s");

            sb.append(" hull.");

            tooltip.addPara(sb.toString(), pad, hlColors.toArray(new Color[hlColors.size()]),
                        hl.toArray(new String[hl.size()]));
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(Roider_Hullmods.MIDAS)) return true;

        return !hasMIDAS(ship.getVariant());
    }

    @Override
    public void onFleetSync(CampaignFleetAPI fleet) {
    }

    @Override
    public boolean withAdvanceInCampaign() {
        return true;
    }

    @Override
    public boolean withOnFleetSync() {
        return false;
    }
}
