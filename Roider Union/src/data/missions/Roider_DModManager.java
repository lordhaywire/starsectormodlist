package data.missions;

import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import static com.fs.starfarer.api.impl.campaign.DModManager.getModsWithTags;
import static com.fs.starfarer.api.impl.campaign.DModManager.getModsWithoutTags;
import static com.fs.starfarer.api.impl.campaign.DModManager.getNumDMods;
import static com.fs.starfarer.api.impl.campaign.DModManager.removeModsAlreadyInVariant;
import static com.fs.starfarer.api.impl.campaign.DModManager.removeUnsuitedMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Author: SafariJohn
 */
public class Roider_DModManager extends DModManager {
    public static FleetMemberAPI addDShipToFleet(int numDMods,
                FleetSide side, String variantId, String name,
                boolean isFlagship, Random random,
                MissionDefinitionAPI api) {

		FleetMemberAPI member;

        if (name == null) {
            member = api.addToFleet(side, variantId,
                    FleetMemberType.SHIP, isFlagship);
        } else {
            member = api.addToFleet(side, variantId,
                    FleetMemberType.SHIP, name, isFlagship);
        }
//        Roider_DModManager.addDMods(member, false, numDMods, random);
//        Roider_DModManager.setDHull(member.getVariant());

        return member;
    }

	public static void addDMods(FleetMemberAPI member, boolean canAddDestroyedMods, int num, Random random) {
		if (random == null) random = new Random();


		ShipVariantAPI variant = member.getVariant();

//		if (member.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)) {
//			int added = addAllPermaModsWithTags(variant, Tags.HULLMOD_CIV_ALWAYS);
//			if (added > 0) {
//				num -= added;
//				if (num <= 0) return;
//			}
//		}

		List<HullModSpecAPI> potentialMods = getModsWithTags(Tags.HULLMOD_DAMAGE);
		if (canAddDestroyedMods) potentialMods.addAll(getModsWithTags(Tags.HULLMOD_DESTROYED_ALWAYS));

		removeUnsuitedMods(variant, potentialMods);

		boolean hasStructDamage = getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0;
		if (hasStructDamage) {
			potentialMods = getModsWithoutTags(potentialMods, Tags.HULLMOD_DAMAGE_STRUCT);
		}

		if (variant.getHullSpec().getFighterBays() > 0) {
		//if (variant.getHullSpec().getFighterBays() > 0 || variant.isCarrier()) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_FIGHTER_BAY_DAMAGE));
		}
		if (variant.getHullSpec().getDefenseType() == ShieldAPI.ShieldType.PHASE) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_DAMAGE_PHASE));
		}

		if (variant.isCarrier()) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_CARRIER_ALWAYS));
		}

		potentialMods = new ArrayList<HullModSpecAPI>(new HashSet<HullModSpecAPI>(potentialMods));

		removeUnsuitedMods(variant, potentialMods);
		removeModsAlreadyInVariant(variant, potentialMods);

//		System.out.println("");
//		System.out.println("Adding: ");
		WeightedRandomPicker<HullModSpecAPI> picker = new WeightedRandomPicker<HullModSpecAPI>(random);
		picker.addAll(potentialMods);
		int added = 0;
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			HullModSpecAPI pick = picker.pickAndRemove();
			if (pick != null) {
				if (pick.hasTag(Tags.HULLMOD_DAMAGE_STRUCT) && getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0) {
					i--;
					continue;
				}
				variant.addPermaMod(pick.getId());
				//System.out.println("Mod: " + pick.getId());
				added++;
//                i--;
			}
		}
//		if (getNumDMods(variant) < 5) {
//			System.out.println("ewfwefew");
//		}
	}
}
