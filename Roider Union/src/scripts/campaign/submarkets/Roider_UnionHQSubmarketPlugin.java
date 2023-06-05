package scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.procgen.DropGroupRow;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.PlayerFactionStore;
import exerelin.utilities.NexUtilsFaction;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Fitters;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Items;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_MemFlags;
import java.awt.Color;
import java.util.*;
import org.magiclib.util.MagicSettings;
import scripts.Roider_ModPlugin;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;
import scripts.campaign.retrofit.Roider_UnionHQRetrofitManager;

/**
 * Author: SafariJohn
 */
public class Roider_UnionHQSubmarketPlugin extends MilitarySubmarketPlugin {
    private transient Map<String, RepLevel> reqReps = null;
    private transient Map<String, Boolean> reqCom = null;


    public static void aliasAttributes(XStream x) {
    }

    @Override
	public void init(SubmarketAPI submarket) {
		super.init(submarket);

        loadShipRequirements();
	}

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (!submarket.getFaction().getId().equals(Roider_Factions.ROIDER_UNION)) {
            submarket.setFaction(Global.getSector().getFaction(Roider_Factions.ROIDER_UNION));
        }
    }

    protected void loadShipRequirements() {
        reqReps = new HashMap<>();
        reqCom = new HashMap<>();

        // Load retrofit requirements
        List<RetrofitData> retrofits = new Roider_UnionHQRetrofitManager(
                    Roider_Fitters.FULL,
                    market.getPrimaryEntity(),
                    getRoiders(),
                    false)
                    .getRetrofits();

        for (RetrofitData data : retrofits) {
            RepLevel rep = data.reputation;
            boolean commission = data.commission;

            // If unknown or required, put new value in
            if (!reqCom.containsKey(data.targetHull)
                        || reqCom.get(data.targetHull)) {
                reqCom.put(data.targetHull, commission);
            }

            // If unknown or more strict, put new value in
            if (!reqReps.containsKey(data.targetHull)
                        || reqReps.get(data.targetHull).isAtBest(rep)) {
                reqReps.put(data.targetHull, rep);
            }
        }

        // Load requirements set in modSettings.json
        // Overrides retrofit values
        Map<String, Float> reps = MagicSettings.getFloatMap(Roider_Settings.MAGIC_ID,
                    Roider_Settings.UNION_HQ_SHIP_REP_REQ);
        for (String ship : reps.keySet()) {
            RepLevel rep;

            int req = (int) (reps.get(ship) + 0.01f);
            switch (req) {
                case 4: rep = RepLevel.COOPERATIVE; break;
                case 3: rep = RepLevel.FRIENDLY; break;
                case 2: rep = RepLevel.WELCOMING; break;
                default: rep = RepLevel.FAVORABLE; break;
            }

            reqReps.put(ship, rep);
        }
        List<String> coms = MagicSettings.getList(Roider_Settings.MAGIC_ID,
                    Roider_Settings.UNION_HQ_SHIP_COM_NOT_REQ);
        for (String ship : coms) {
            reqCom.put(ship, false);
        }

    }

    @Override
    public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;

		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;

			pruneWeapons(0f);

			int weapons = 4 + Math.max(0, market.getSize() - 3) * 2;
			int fighters = 2 + Math.max(0, market.getSize() - 3);

			addWeapons(weapons, weapons + 2, 3, submarket.getFaction().getId());
			addFighters(fighters, fighters + 2, 3, submarket.getFaction().getId());

			float stability = market.getStabilityValue();
			float sMult = Math.max(0.1f, stability / 10f);
			getCargo().getMothballedShips().clear();
            loadShipRequirements();
			addShips(submarket.getFaction().getId(),
					200f * sMult, // combat
					15f, // freighter
					10f, // tanker
					20f, // transport
					10f, // liner
					10f, // utilityPts
					null, // qualityOverride
					0f, // qualityMod
					null,
					null);

			addHullMods(4, 2 + itemGenRandom.nextInt(4));

            addBlueprints(1 + itemGenRandom.nextInt(2));
		}

//        if (market.getFactionId().equals(Roider_Factions.ROIDER_UNION)) {
            getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(Items.INDUSTRY_BP, Roider_Industries.UNION_HQ), 1);
            getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(Roider_Items.UNION_HQ_BP, Roider_Industries.UNION_HQ), 1);
            getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(Items.INDUSTRY_BP, Roider_Industries.SHIPWORKS), 1);
            getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(Roider_Items.SHIPWORKS_BP, Roider_Industries.SHIPWORKS), 1);

            if (!Global.getSector().getPlayerFaction().knowsIndustry(Roider_Industries.UNION_HQ)) {
                getCargo().addSpecial(new SpecialItemData(Roider_Items.UNION_HQ_BP, Roider_Industries.UNION_HQ), 1);
            }

            if (!Global.getSector().getPlayerFaction().knowsIndustry(Roider_Industries.SHIPWORKS)) {
                getCargo().addSpecial(new SpecialItemData(Roider_Items.SHIPWORKS_BP, Roider_Industries.SHIPWORKS), 1);
            }
//        }

		getCargo().sort();
    }

    protected void addBlueprints(int num) {
		CargoAPI cargo = getCargo();
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (stack.isSpecialStack() && (isSpecialId("roider_retrofit_bp", stack)
                        || isSpecialId("roider_bp", stack))) {
				cargo.removeStack(stack);
			}
		}

        WeightedRandomPicker<DropGroupRow> picker = DropGroupRow.getPicker("roider_market_blueprints");
        picker.setRandom(itemGenRandom);

        int added = 0;
        int tries = 0;
        Set<String> alreadyAdded = new HashSet<>();
        while (added < num) {
            tries++;
            if (tries > num * 3) break;

            DropGroupRow pick = picker.pick();

            if (pick.isMultiValued()) {
                pick = pick.resolveToSpecificItem(itemGenRandom);
            }
            if (pick.isNothing()) continue;

            String dataId = pick.getSpecialItemData();

            if (dataId == null) continue;

            if (Global.getSector().getPlayerFaction().knowsShip(dataId)) continue;
            if (Global.getSector().getPlayerFaction().knowsFighter(dataId)) continue;
            if (Global.getSector().getPlayerFaction().knowsWeapon(dataId)) continue;

            if (dataId.equals(Roider_Items.ROIDER_PACKAGE)) {
                BlueprintProviderItem bpi = (BlueprintProviderItem) pick.getSpecialItemSpec().getNewPluginInstance(null);
                if (allBPsKnown(bpi)) continue;
            }

            if (dataId.isEmpty() && alreadyAdded.contains(pick.getSpecialItemId())) continue;
            else if (alreadyAdded.contains(dataId)) continue;


            SpecialItemData data = new SpecialItemData(pick.getSpecialItemId(), dataId);

            cargo.addItems(CargoItemType.SPECIAL, data, 1);
            added++;

            alreadyAdded.add(dataId);

        }
    }

    private boolean isSpecialId(String id, CargoStackAPI stack) {
        return stack.getSpecialItemSpecIfSpecial().getId().equals(id);
    }

    private boolean allBPsKnown(BlueprintProviderItem bpi) {
        FactionAPI faction = Global.getSector().getPlayerFaction();
        if (bpi.getProvidedFighters() != null) {
            for (String id : bpi.getProvidedFighters()) {
                if (!faction.knowsFighter(id)) return false;
            }
        }
        if (bpi.getProvidedWeapons() != null) {
            for (String id : bpi.getProvidedWeapons()) {
                if (!faction.knowsWeapon(id))  return false;
            }
        }
        if (bpi.getProvidedShips() != null) {
            for (String id : bpi.getProvidedShips()) {
                if (!faction.knowsShip(id)) return false;
            }
        }
        if (bpi.getProvidedIndustries() != null) {
            for (String id : bpi.getProvidedIndustries()) {
                if (!faction.knowsIndustry(id)) return false;
            }
        }

        return true;
    }

	@Override
	protected boolean hasCommission() {
        if (Roider_ModPlugin.hasNexerelin) {
            String commissionFaction = NexUtilsFaction.getCommissionFactionId();
            if (commissionFaction != null && AllianceManager.areFactionsAllied(commissionFaction, Roider_Factions.ROIDER_UNION)) {
                return true;
            }
            if (AllianceManager.areFactionsAllied(PlayerFactionStore.getPlayerFactionId(), Roider_Factions.ROIDER_UNION)) {
                return true;
            }
        }

		return submarket.getFaction().getId().equals(Misc.getCommissionFactionId());
	}

    protected boolean requiresCommission(FleetMemberAPI member, RepLevel req) {
        // Use requirement for retrofit, if exists.
        if (reqCom == null) loadShipRequirements();
        if (reqCom.containsKey(member.getHullSpec().getBaseHullId())) {
            return reqCom.get(member.getHullSpec().getBaseHullId());
        }

        return requiresCommission(req);
    }

    @Override
    protected boolean requiresCommission(RepLevel req) {

		return req.isAtWorst(RepLevel.WELCOMING);
    }

    @Override
    public String getName() {
        return "Roider Union";
    }

    @Override
    public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
        // If market has roider resource, Roider submarket should have it.
        if (com.getId().equals(Commodities.ORE)) return true;
        if (com.getId().equals(Commodities.RARE_ORE)) return true;
        if (com.getId().equals(Commodities.VOLATILES)) return true;
        if (com.getId().equals(Commodities.ORGANICS)) return true;

        return super.shouldHaveCommodity(com);
    }

    @Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		//boolean illegal = submarket.getFaction().isIllegal(commodityId);
		boolean illegal = market.isIllegal(commodityId);
		RepLevel req = getRequiredLevelAssumingLegal(commodityId, action);

		if (req == null) return illegal;

		RepLevel level = getRoiders().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
		boolean legal = level.isAtWorst(req);
		if (requiresCommission(req)) {
			legal &= hasCommission();
		}
		return !legal;
	}

    @Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (stack.isCommodityStack()) {
			return isIllegalOnSubmarket((String) stack.getData(), action);
		}

		RepLevel req = getRequiredLevelAssumingLegal(stack, action);
		if (req == null) return false;

		RepLevel level = getRoiders().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));

		boolean legal = level.isAtWorst(req);
		if (requiresCommission(req)) {
			legal &= hasCommission();
		}

		return !legal;
	}

    @Override
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(stack, action);

		if (req != null) {
			if (requiresCommission(req)) {
				return "Req: " + getRoiders().getDisplayName() + " - " + req.getDisplayName().toLowerCase() + ", "
						+ submarket.getFaction().getDisplayName() + " commission";
			}
			return "Req: " + getRoiders().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
		}

		return "Illegal to trade in " + stack.getDisplayName() + " here";
	}


    @Override
	public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(stack, action);
		if (req != null) {
			Color c = Misc.getNegativeHighlightColor();
			Highlights h = new Highlights();
			RepLevel level = getRoiders().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
			if (!level.isAtWorst(req)) {
				h.append(getRoiders().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
			}
			if (requiresCommission(req) && !hasCommission()) {
				h.append(submarket.getFaction().getDisplayName() + " commission", c);
			}
			return h;
		}
		return null;
	}

	private RepLevel getRequiredLevelAssumingLegal(CargoStackAPI stack, TransferAction action) {
		int tier = -1;
		if (stack.isWeaponStack()) {
			WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
			tier = spec.getTier();
		} else if (stack.isSpecialStack() && stack.getSpecialDataIfSpecial().getId().equals(Items.MODSPEC)) {
			HullModSpecAPI spec = stack.getHullModSpecIfHullMod();
			tier = spec.getTier();
		} else if (stack.isFighterWingStack()) {
			FighterWingSpecAPI spec = stack.getFighterWingSpecIfWing();
			tier = spec.getTier();
		} else if (stack.isSpecialStack()) {
            SpecialItemData data = stack.getSpecialDataIfSpecial();

            switch (data.getId()) {
                case Roider_Items.UNION_HQ_BP: tier = 0; break;
                case Roider_Items.SHIPWORKS_BP: tier = 1; break;
                case Roider_Items.RETROFIT_BP:
                    tier = (int) Global.getSettings().getHullSpec(data.getData()).getRarity();
                    break;
                case Items.FIGHTER_BP:
                    FighterWingSpecAPI fSpec = Global.getSettings().getFighterWingSpec(data.getData());
                    if (fSpec.hasTag("roider")) tier = fSpec.getTier();
                    break;
                case Items.WEAPON_BP:
                    WeaponSpecAPI wSpec = Global.getSettings().getWeaponSpec(data.getData());
                    if (wSpec.hasTag("roider")) tier = wSpec.getTier();
                    break;
            }
        }

		if (tier >= 0) {
			if (action == TransferAction.PLAYER_BUY) {
				switch (tier) {
				case 0: return RepLevel.FAVORABLE;
				case 1: return RepLevel.WELCOMING;
				case 2: return RepLevel.FRIENDLY;
				case 3: return RepLevel.COOPERATIVE;
				}
			}
			return RepLevel.VENGEFUL;
		}

		if (!stack.isCommodityStack()) return null;
		return getRequiredLevelAssumingLegal((String) stack.getData(), action);
	}

	private RepLevel getRequiredLevelAssumingLegal(String commodityId, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) {
			//return null;
			return RepLevel.VENGEFUL;
		}

		CommodityOnMarketAPI com = market.getCommodityData(commodityId);
		boolean isMilitary = com.getCommodity().getTags().contains(Commodities.TAG_MILITARY);
		if (isMilitary) {
			if (com.isPersonnel()) {
				return RepLevel.COOPERATIVE;
			}
			return RepLevel.FAVORABLE;
		}
		return null;
	}

    @Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req == null) return false;

		RepLevel level = getRoiders().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));

		boolean legal = level.isAtWorst(req);
		if (requiresCommission(member, req)) {
			legal &= hasCommission();
		}

		return !legal;
	}

    @Override
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req != null) {
			String str = "";
			RepLevel level = getRoiders().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
			if (!level.isAtWorst(req)) {
				str += "Req: " + getRoiders().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
			}
			if (requiresCommission(member, req) && !hasCommission()) {
				if (!str.isEmpty()) str += "\n";
				str += "Req: " + submarket.getFaction().getDisplayName() + " - " + "commission";
			}
			return str;
		}

		if (action == TransferAction.PLAYER_BUY) {
			return "Illegal to buy"; // this shouldn't happen
		} else {
			return "Illegal to sell";
		}
	}

    @Override
	public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req != null) {
			Color c = Misc.getNegativeHighlightColor();
			Highlights h = new Highlights();
			RepLevel level = getRoiders().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
			if (!level.isAtWorst(req)) {
				h.append("Req: " + getRoiders().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
			}
			if (requiresCommission(member, req) && !hasCommission()) {
				h.append("Req: " + submarket.getFaction().getDisplayName() + " - commission", c);
			}
			return h;
		}
		return null;
	}

	private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_BUY) {
            // Use required reputation for retrofit, if exists.
            if (reqReps == null) loadShipRequirements();
            if (reqReps.containsKey(member.getHullSpec().getBaseHullId())) {
                return reqReps.get(member.getHullSpec().getBaseHullId());
            }

			int fp = member.getFleetPointCost();
			ShipAPI.HullSize size = member.getHullSpec().getHullSize();

			if (size == ShipAPI.HullSize.CAPITAL_SHIP || fp > 15) return RepLevel.COOPERATIVE;
			if (size == ShipAPI.HullSize.CRUISER || fp > 10) return RepLevel.FRIENDLY;
			if (size == ShipAPI.HullSize.DESTROYER || fp > 5) return RepLevel.WELCOMING;
			return RepLevel.FAVORABLE;
		}
		return null;
	}

	private final RepLevel roider_MinStanding = RepLevel.FAVORABLE;
    @Override
	public boolean isEnabled(CoreUIAPI ui) {
		if (!submarket.getMarket().getMemory().getBoolean(Roider_MemFlags.UNION_HQ_FUNCTIONAL)) return false;

		return super.isEnabled(ui);
	}

    @Override
	public String getTooltipAppendix(CoreUIAPI ui) {
		if (!submarket.getMarket().getMemory().getBoolean(Roider_MemFlags.UNION_HQ_FUNCTIONAL)) {
            return "Requires: functional Union HQ";
        }
		if (!isEnabled(ui)) {
			return "Requires: " + getRoiders().getDisplayName() + " - " + roider_MinStanding.getDisplayName().toLowerCase();
		}
		if (ui.getTradeMode() == CampaignUIAPI.CoreUITradeMode.SNEAK) {
			return "Requires: proper docking authorization";
		}
		return null;
	}

    private FactionAPI getRoiders() {
        return Global.getSector().getFaction(Roider_Factions.ROIDER_UNION);
    }
}
