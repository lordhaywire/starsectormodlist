package hullmods;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidImpact;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import ids.Roider_Ids.Roider_Hullmods;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // Resist impacts from asteroid belts and fields on the campaign map
    // Modified from MesoTroniK's TiandongRetrofit hullmod
    public void advanceInCampaign(CampaignFleetAPI fleet) {
        // Want to resist asteroid impacts
        // Must increase terrain mitigation stat during impact event
        // But not if in some other movement affecting terrain such as nebulae
        FleetDataAPI fleetData = fleet.getFleetData();
        MutableStat nav = fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT);
        MemoryAPI mem = fleet.getMemoryWithoutUpdate();

        boolean hadRecent = mem.is(RECENT_IMPACT, true);
        boolean recentlyNegated = mem.is(NEGATED_IMPACT, true);

        boolean impactInProgress = fleet.hasScriptOfClass(AsteroidImpact.class);

        // Skip if there is nothing to do this advance
        if (!hadRecent && !impactInProgress) {
            nav.unmodifyMult(MOD_ID);
            return;
        }

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
        float magnitudeMult = 1f - contribution * (1f - MAX_IMPACT_RESIST / 100f);


        // Reduce chance of damaging impact
        // by randomly setting RECENT_IMPACT to false based on
        // number of ships with MIDAS
        if (hadRecent && !recentlyNegated && !impactInProgress) {
            mem.set(RECENT_IMPACT, Math.random() > contribution);
            mem.set(NEGATED_IMPACT, true);
            return;
        } else if (hadRecent && recentlyNegated) {
            mem.set(NEGATED_IMPACT, false);
        }

        // Clean up impact mod
        if (!impactInProgress) {
            nav.unmodifyMult(MOD_ID);
            return;
        }

        // Reduce pushing effect of an impact
        nav.modifyMult(MOD_ID, magnitudeMult, "MIDAS");
        mem.set(NEGATED_IMPACT, false);
    }

    private boolean hasMIDAS(ShipVariantAPI variant) {
        return variant.hasHullMod(Roider_Hullmods.MIDAS)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_1)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_2)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_3);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEmpDamageTakenMult().modifyMult(MOD_ID, 1f - EMP_REDUCTION / 100f);

        ShipVariantAPI variant = stats.getVariant();

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
    }


    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) MAX_IMPACT_RESIST + "%";
        if (index == 1) return "" + (int) EMP_REDUCTION + "%";
        if (index == 2) return "" + (int) MASS_BONUS + "%";
        return null;
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
