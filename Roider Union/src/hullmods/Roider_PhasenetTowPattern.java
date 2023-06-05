package hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import static com.fs.starfarer.api.impl.hullmods.DriveFieldStabilizer.BURN_BONUS;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import ids.Roider_Ids.Roider_Hullmods;
import java.awt.Color;
import java.util.*;

/**
 * Author: SafariJohn
 */
public class Roider_PhasenetTowPattern implements HullModEffect {

	public static final String HULLMOD_ID = Roider_Hullmods.PHASENET_TOW_PATTERN;

	public void init(HullModSpecAPI spec) {

	}

	public static class Roider_PhasenetTowPatternBuff implements BuffManagerAPI.Buff {
		//public boolean wasApplied = false;
		private String buffId;
        private int bonus;
		private int frames = 0;

		public Roider_PhasenetTowPatternBuff(String buffId) {
			this.buffId = buffId;
            this.bonus = 1;
		}

        public void setBonus(int bonus) {
            this.bonus = bonus;
        }

		public boolean isExpired() {
			return frames >= 2;
		}
		public String getId() {
			return buffId;
		}
		public void apply(FleetMemberAPI member) {
			// this ensures the buff lasts for exactly one frame unless wasApplied is reset (as it is later)
			//wasApplied = true;
			member.getStats().getMaxBurnLevel().modifyFlat(buffId, bonus);
		}
		public void advance(float days) {
			frames++;
		}
	};


	public Roider_PhasenetTowPattern() {

	}

	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		if (member.getFleetData() == null) return;
		if (member.getFleetData().getFleet() == null) return;
		if (!member.getFleetData().getFleet().isPlayerFleet()) {
            member.getStats().getDynamic().getMod(Stats.FLEET_BURN_BONUS).modifyFlat(HULLMOD_ID, BURN_BONUS);
            return;
        }


		if (!isTowShip(member)) {
			cleanUpPhasenetTowPatternBuffBy(member);
			return;
		}

		if (!member.canBeDeployedForCombat()) {
			cleanUpPhasenetTowPatternBuffBy(member);
			return;
		}

		FleetDataAPI data = member.getFleetData();
		List<FleetMemberAPI> all = data.getMembersListCopy();

        // Get tow ships
        List<FleetMemberAPI> towShips = new ArrayList<>();
        for (FleetMemberAPI curr : all) {
			if (isTowShip(curr)) {
                if (!curr.canBeDeployedForCombat()) continue;

                towShips.add(curr);
            }
        }

        // If there are no phasenets working, clean up
		if (towShips.isEmpty()) {
			cleanUpPhasenetTowPatternBuffBy(member);
			return;
		}

        // Sort tow ships fastest to slowest
        Collections.sort(towShips, new Comparator<FleetMemberAPI>() {
            @Override
            public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
                return Math.round(getMaxBurnWithoutPhasenet(o2)
                            - getMaxBurnWithoutPhasenet(o1));
            }
        });

        // Sort all ships fastest to slowest
        Collections.sort(all, new Comparator<FleetMemberAPI>() {
            @Override
            public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
                return Math.round(getMaxBurnWithoutPhasenet(o2)
                            - getMaxBurnWithoutPhasenet(o1));
            }
        });

        // Assign each tow ship to another ship
		Roider_PhasenetTowPatternBuff buff = getPhasenetTowPatternBuffBy(member, true);
        Map<FleetMemberAPI, FleetMemberAPI> towed = new HashMap<>();
        FleetMemberAPI towTarget = null;
        for (FleetMemberAPI towShip : towShips) {

            // Apply towing buff to tow ship if it is being towed
            for (Buff b : towShip.getBuffManager().getBuffs()) {
                if (b.getId().startsWith(HULLMOD_ID)) {
                    b.apply(towShip);
                    break;
                }
            }

            int cutoffSpeed = towShip.getStats().getMaxBurnLevel().getModifiedInt();

            int index = all.indexOf(towShip);
            all.remove(towShip);
            FleetMemberAPI slowest = getSlowest(all, cutoffSpeed, towed);
            if (index < all.size()) all.add(index, towShip);
            else all.add(towShip);

            if (slowest == null) continue;

            towed.put(towShip, slowest);

            if (towShip == member) {
                towTarget = slowest;

                int bonus = Math.min(getMaxBonusForSize(slowest.getHullSpec().getHullSize()),
                            (int) (cutoffSpeed - getMaxBurnWithoutPhasenet(slowest)));

                buff.setBonus(bonus);

                BuffManagerAPI.Buff existing = slowest.getBuffManager().getBuff(buff.getId());
                if (existing == buff) {
                    buff.frames = 0;
                    addTowedHullmod(slowest);
                } else {
                    buff.frames = 0;
                    slowest.getBuffManager().addBuff(buff);
                    addTowedHullmod(slowest);
                }
                break;
            }
        }

		for (FleetMemberAPI curr : all) {
			if (curr != towTarget) {
                if (curr.getBuffManager().getBuff(buff.getId()) != null) {
                    curr.getBuffManager().removeBuff(buff.getId());
                    curr.getVariant().removePermaMod(Roider_Hullmods.PHASENET_TOW);
                }
			}
		}
	}

	private FleetMemberAPI getSlowest(List<FleetMemberAPI> all, int speedCutoff, Map<FleetMemberAPI, FleetMemberAPI> towed) {
		FleetMemberAPI slowest = null;
		float minLevel = Float.MAX_VALUE;
		for (FleetMemberAPI curr : all) {
			if (!isSuitable(curr)) continue;
            if (towed.containsValue(curr)) continue;

			float baseBurn = getMaxBurnWithoutPhasenet(curr);

			if (baseBurn >= speedCutoff) continue;

			float boostedBurn = baseBurn + getMaxBonusForSize(curr.getHullSpec().getHullSize());

			if (boostedBurn > speedCutoff) boostedBurn = speedCutoff;

            if (boostedBurn == minLevel) {
                if (boostedBurn == speedCutoff) {
                    if (isTowShip(curr) && !isTowShip(slowest)) {
                        slowest = curr;
                    }
                } else {
                    if (isSmaller(curr, slowest)) {
                        slowest = curr;
                    } else if (isTowShip(curr) && !isTowShip(slowest)) {
                        slowest = curr;
                    }
                }
            } else if (boostedBurn < minLevel) {
				minLevel = boostedBurn;
				slowest = curr;
			}
		}

		return slowest;
	}

    private boolean isTowShip(FleetMemberAPI member) {
        if (member == null) return false;

        return member.getVariant().getHullMods().contains(HULLMOD_ID);
    }

	private float getMaxBurnWithoutPhasenet(FleetMemberAPI member) {
		MutableStat burn = member.getStats().getMaxBurnLevel();
		float val = burn.getModifiedValue();
		float sub = 0;
		for (MutableStat.StatMod mod : burn.getFlatMods().values()) {
			if (mod.getSource().startsWith(HULLMOD_ID)) sub = mod.value;
		}
		return Math.max(0, val - sub);
	}

	private boolean isSuitable(FleetMemberAPI member) {
		return !member.isFighterWing();
	}

    public static int getMaxBonusForSize(HullSize size) {
        switch (size) {
            case CAPITAL_SHIP: return 1;
            case CRUISER: return 1;
            case DESTROYER: return 2;
            default: return 4;
        }
    }

    private boolean isSmaller(FleetMemberAPI m1, FleetMemberAPI m2) {
        int m1Size = 5, m2Size = 5;
        switch (m1.getHullSpec().getHullSize()) {
            case FRIGATE: m1Size--;
            case DESTROYER: m1Size--;
            case CRUISER: m1Size--;
            case CAPITAL_SHIP: m1Size--;
        }
        switch (m2.getHullSpec().getHullSize()) {
            case FRIGATE: m2Size--;
            case DESTROYER: m2Size--;
            case CRUISER: m2Size--;
            case CAPITAL_SHIP: m2Size--;
        }

        return m1Size < m2Size;
    }

	private void cleanUpPhasenetTowPatternBuffBy(FleetMemberAPI member) {
		if (member.getFleetData() == null) return;
		FleetDataAPI data = member.getFleetData();
		Roider_PhasenetTowPatternBuff buff = getPhasenetTowPatternBuffBy(member, false);
		if (buff != null) {
			for (FleetMemberAPI curr : data.getMembersListCopy()) {
				curr.getBuffManager().removeBuff(buff.getId());
			}
		}
	}

	/**
	 * One instance of the buff object per ship with a Phasenet.
	 */
	public static final String ROIDER_PHASENET_TOW_KEY = "Roider_PhasenetTow_PersistentBuffs";

	@SuppressWarnings("unchecked")
	private Roider_PhasenetTowPatternBuff getPhasenetTowPatternBuffBy(FleetMemberAPI member, boolean createIfMissing) {
		Map<FleetMemberAPI, Roider_PhasenetTowPatternBuff> buffs;
		if (Global.getSector().getPersistentData().containsKey(ROIDER_PHASENET_TOW_KEY)) {
			buffs = (Map<FleetMemberAPI, Roider_PhasenetTowPatternBuff>) Global.getSector().getPersistentData().get(ROIDER_PHASENET_TOW_KEY);
		} else {
			buffs = new HashMap<FleetMemberAPI, Roider_PhasenetTowPatternBuff>();
			Global.getSector().getPersistentData().put(ROIDER_PHASENET_TOW_KEY, buffs);
		}

		//new HashMap<FleetMemberAPI, Roider_PhasenetTowPatternBuff>();
		Roider_PhasenetTowPatternBuff buff = buffs.get(member);
		if (buff == null && createIfMissing) {
			String id = HULLMOD_ID + "_" + member.getId();
			buff = new Roider_PhasenetTowPatternBuff(id);
			buffs.put(member, buff);
		}
		return buff;
	}

    private void addTowedHullmod(FleetMemberAPI member) {
//        ShipVariantAPI variant = member.getVariant();
//
//        if (!variant.hasHullMod(Roider_Hullmods.PHASENET_TOW)) {
//            member.getVariant().addPermaMod(Roider_Hullmods.PHASENET_TOW);
//        }
    }


	public void advanceInCombat(ShipAPI ship, float amount) {
	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
	}
	public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "+4";
        if (index == 1) return "+2";
        return "+1";
    }

	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}

	public boolean affectsOPCosts() {
		return false;
	}

	public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
		return getDescriptionParam(index, hullSize);
	}

	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		return true;
	}

	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		return null;
	}

	public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

	}

	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {

	}

	public Color getBorderColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getNameColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDisplaySortOrder() {
		return 100;
	}

	public int getDisplayCategoryIndex() {
		return -1;
	}

    @Override
    public boolean hasSModEffectSection(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {

    }

    @Override
    public void addSModEffectSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {}

    @Override
    public boolean hasSModEffect() {
        return false;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        return null;
    }

    @Override
    public float getTooltipWidth() {
		return 369f;
    }

    @Override
    public boolean isSModEffectAPenalty() {
        return false;
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return false;
    }

}
