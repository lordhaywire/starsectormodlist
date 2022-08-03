package scripts.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import java.util.Random;
import scripts.campaign.fleets.expeditions.Roider_ExpeditionFleetFactory;

public class Roider_ExpeditionTrap extends BaseSalvageSpecial {

	public static class Roider_ExpeditionTrapCreator implements SpecialCreator {
        public static void aliasAttributes(XStream x) {
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "random", "r");
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "faction", "f");
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "source", "s");
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "minPts", "mi");
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "maxPts", "ma");
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "chance", "c");
            x.aliasAttribute(Roider_ExpeditionTrapCreator.class, "fleetType", "t");
        }

		private Random random;
		private String faction;
        private String source;
		private int minPts;
		private int maxPts;
		private final float chance;
		private final String fleetType;

		public Roider_ExpeditionTrapCreator(Random random, float chance, String fleetType,
									String faction, String market, int min, int max, boolean major) {
			this.random = random;
			this.chance = chance;
			this.fleetType = fleetType;
			this.faction = faction;
            this.source = market;
			this.minPts = min;
			this.maxPts = max;
		}

		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {


			Roider_ExpeditionTrapData data = new Roider_ExpeditionTrapData();
			data.prob = chance;

			data.nearbyFleetFaction = faction;
			data.useAllFleetsInRange = true;

			if (fleetType != null) {
				int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);
				combatPoints *= 5;

				FleetParamsV3 params = new FleetParamsV3(
						Global.getSector().getEconomy().getMarket(source),
						entity.getLocationInHyperspace(),
						faction,
						null,
						fleetType,
						combatPoints, // combatPts
						0f, // freighterPts
						0f, // tankerPts
						0f, // transportPts
						0f, // linerPts
						0f, // utilityPts
						0f // qualityMod
						);
				data.params = params;
			}

			return data;
		}
	}


	public static class Roider_ExpeditionTrapData implements SalvageSpecialData {
        public static void aliasAttributes(XStream x) {
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "prob", "p");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "fleetId", "f");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "nearbyFleetFaction", "n");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "useClosestFleetInRange", "c");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "useAllFleetsInRange", "a");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "params", "pa");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "minRange", "mi");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "maxRange", "ma");
            x.aliasAttribute(Roider_ExpeditionTrapData.class, "major", "m");
        }

		public float prob = 0.5f;

		public String fleetId;

		public String nearbyFleetFaction = null;
		public Boolean useClosestFleetInRange = null;
		public Boolean useAllFleetsInRange = null;

		public FleetParamsV3 params;

		public float minRange = 2500;
		public float maxRange = 5000;

        public boolean major = false;

		public Roider_ExpeditionTrapData() {
		}

		public Roider_ExpeditionTrapData(FleetParamsV3 params) {
			this.params = params;
		}

		public Roider_ExpeditionTrapData(float prob, FleetParamsV3 params) {
			this.prob = prob;
			this.params = params;
		}

		public SalvageSpecialPlugin createSpecialPlugin() {
			return new Roider_ExpeditionTrap();
		}
	}

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ExpeditionTrap.class, "data", "d");

        x.alias("roider_expTrapData", Roider_ExpeditionTrapData.class);
        Roider_ExpeditionTrapData.aliasAttributes(x);
    }

	private Roider_ExpeditionTrapData data;

	public Roider_ExpeditionTrap() {
	}


	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);

		data = (Roider_ExpeditionTrapData) specialData;

		initEntityLocation();
	}

	private void initEntityLocation() {
        if (data.major) {
            addText("Near the $shortName, sensors pick up a mine left over from the minefield. "
                        + "It appears to have been disabled by the same signal as the others.");
        }

		if (random.nextFloat() > data.prob) {

			if (random.nextFloat() > 0.5f) {
				addText("Your salvage crews discover a transmitter set to send a signal when " +
						"tripped by an alarm system, but it doesn't appear to be functional. " +
						"Closer examination indicates it was probably set many cycles ago.");
			} else {
				addText("Your salvage crews discover a transmitter set to send a signal when " +
						"tripped by an alarm system. The alarm went off as intended, but the transmitter " +
						"was fried by a power surge before it could do its job.");
			}

			setDone(true);
			setEndWithContinue(true);
			setShowAgain(false);
			return;
		}

		if (entity instanceof PlanetAPI) {
			addText("As your salvage crews begin their work, a transmitter hidden somewhere planetside " +
					"sends out an encrypted, broadwave signal. Whatever destination it's meant for, " +
					"it has to be nearby.");
		} else {
			addText("As your salvage crews begin their work, a transmitter inside the $shortName " +
					"sends out an encrypted, broadwave signal. Whatever destination it's meant for, " +
					"it has to be nearby.");
		}

		transmitterActivated();

		setDone(true);
		setEndWithContinue(true);
		setShowAgain(false);
	}


	public void transmitterActivated() {
		if (data == null) return;
		if (entity == null) return;

		if (data.fleetId != null) {
			SectorEntityToken found = Global.getSector().getEntityById(data.fleetId);
			if (found instanceof CampaignFleetAPI) {
				CampaignFleetAPI fleet = (CampaignFleetAPI) found;
				FleetMemberAPI flagship = fleet.getFlagship();
				boolean makeAggressive = false;
				if (flagship != null) {
					makeAggressive = flagship.getVariant().hasHullMod(HullMods.AUTOMATED);
				}
				makeFleetInterceptPlayer(fleet, makeAggressive, true, 30f);
			}
			return;
		}

		if (data.useAllFleetsInRange != null && data.useAllFleetsInRange) {
			boolean foundSomeFleets = false;
			for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
				if (data.nearbyFleetFaction != null &&
						!data.nearbyFleetFaction.equals(fleet.getFaction().getId())) {
					continue;
				}

                if (fleet.getFaction().isPlayerFaction()) continue;

				if (fleet.isStationMode()) continue;

				if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET)) continue;

				float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
				if (dist < data.maxRange) {
					FleetMemberAPI flagship = fleet.getFlagship();
					boolean makeAggressive = false;
					if (flagship != null) {
						makeAggressive = flagship.getVariant().hasHullMod(HullMods.AUTOMATED);
					}
					makeFleetInterceptPlayer(fleet, makeAggressive, true, 30f);
					foundSomeFleets = true;
				}
			}
			if (foundSomeFleets) return;
		}

		if (data.useClosestFleetInRange != null && data.useClosestFleetInRange) {
			CampaignFleetAPI closest = null;
			float minDist = Float.MAX_VALUE;
			for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
				if (data.nearbyFleetFaction != null &&
						!data.nearbyFleetFaction.equals(fleet.getFaction().getId())) {
					continue;
				}

                if (fleet.getFaction().isPlayerFaction()) continue;

				if (fleet.isStationMode()) continue;

				if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET)) continue;

				float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
				if (dist < data.maxRange && dist < minDist) {
					closest = fleet;
					minDist = dist;
				}
			}
			if (closest != null) {
				FleetMemberAPI flagship = closest.getFlagship();
				boolean makeAggressive = false;
				if (flagship != null) {
					makeAggressive = flagship.getVariant().hasHullMod(HullMods.AUTOMATED);
				}
				makeFleetInterceptPlayer(closest, makeAggressive, true, 30f);
				return;
			}
		}

		if (data.params != null) {

			float range = data.minRange + random.nextFloat() * (data.maxRange - data.minRange);
			Vector2f loc = Misc.getPointAtRadius(entity.getLocation(), range);

            String type = data.params.fleetType;
            MarketAPI source = data.params.source;
            boolean pirate = data.params.factionId.equals(Factions.INDEPENDENT)
                        && random.nextBoolean();

			CampaignFleetAPI fleet = Roider_ExpeditionFleetFactory
                        .createExpedition(type, entity.getLocationInHyperspace(), source, pirate, random);

			if (fleet == null || fleet.isEmpty()) return;

            fleet.setTransponderOn(false);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);

			entity.getContainingLocation().addEntity(fleet);
			fleet.setLocation(loc.x, loc.y);

			FleetMemberAPI flagship = fleet.getFlagship();
			boolean makeAggressive = false;
			if (flagship != null) {
				makeAggressive = flagship.getVariant().hasHullMod(HullMods.AUTOMATED);
			}
			makeFleetInterceptPlayer(fleet, makeAggressive, true, 30f);


//			SectorEntityToken despawnLoc = entity.getContainingLocation().createToken(20000, 0);
//			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, despawnLoc, 10000f);
			Misc.giveStandardReturnToSourceAssignments(fleet, false);
			return;
		}
	}




	public static void makeFleetInterceptPlayer(CampaignFleetAPI fleet,
                boolean makeAggressive, boolean makeLowRepImpact,
                float interceptDays) {
		makeFleetInterceptPlayer(fleet, makeAggressive, makeLowRepImpact, true, interceptDays);
	}
	public static void makeFleetInterceptPlayer(CampaignFleetAPI fleet,
                boolean makeAggressive, boolean makeLowRepImpact,
                boolean makeHostile, float interceptDays) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		if (fleet.getAI() == null) {
			fleet.setAI(Global.getFactory().createFleetAI(fleet));
			fleet.setLocation(fleet.getLocation().x, fleet.getLocation().y);
		}

		if (makeAggressive) {
			float expire = fleet.getMemoryWithoutUpdate().getExpire(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true, Math.max(expire, interceptDays));
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, true, Math.max(expire, interceptDays));
		}

		if (makeHostile) {
			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, interceptDays);
		}
		fleet.getMemoryWithoutUpdate().set(FleetAIFlags.PLACE_TO_LOOK_FOR_TARGET, new Vector2f(playerFleet.getLocation()), interceptDays);

		if (makeLowRepImpact) {
			Misc.makeLowRepImpact(playerFleet, "ttSpecial");
		}

		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			((ModularFleetAIAPI)fleet.getAI()).getTacticalModule().setTarget(playerFleet);
		}

		fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, playerFleet, interceptDays, null);
	}


	@Override
	public void optionSelected(String optionText, Object optionData) {
		super.optionSelected(optionText, optionData);
	}


	public static void main(String[] args) {
		Boolean b = null;//new Boolean(true);
		System.out.println(b == true);
	}
}






