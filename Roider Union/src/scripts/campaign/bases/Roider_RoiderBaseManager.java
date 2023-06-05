package scripts.campaign.bases;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import scripts.campaign.bases.Roider_RoiderBaseIntelV2.RoiderBaseTier;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Settings;
import org.magiclib.util.MagicSettings;
import scripts.world.Roider_Gen;

public class Roider_RoiderBaseManager extends BaseEventManager {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_RoiderBaseManager.class, "start", "s");
        x.aliasAttribute(Roider_RoiderBaseManager.class, "extraDays", "e");
        x.aliasAttribute(Roider_RoiderBaseManager.class, "numDestroyed", "d");
        x.aliasAttribute(Roider_RoiderBaseManager.class, "random", "r");
    }

	public static final String KEY = "$roider_roiderBaseManager";
    public static final String RUMOR_HERE = "$roider_baseRumorHere";

	public static final float CHECK_DAYS = 10f;
	public static final float CHECK_PROB = 0.5f;


	public static Roider_RoiderBaseManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (Roider_RoiderBaseManager) test;
	}

	protected long start = 0;
	protected float extraDays = 0;

	protected int numDestroyed = 0;
	protected Random random = new Random();

	public Roider_RoiderBaseManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		start = Global.getSector().getClock().getTimestamp();
	}

	@Override
	protected int getMinConcurrent() {
		return MagicSettings.getInteger(Roider_Settings.MAGIC_ID,
                    Roider_Settings.MIN_INDIE_BASES);
	}
	@Override
	protected int getMaxConcurrent() {
		return MagicSettings.getInteger(Roider_Settings.MAGIC_ID,
                    Roider_Settings.MAX_INDIE_BASES);
	}

	@Override
	protected float getBaseInterval() {
		return CHECK_DAYS;
	}


	@Override
	public void advance(float amount) {
		super.advance(amount);
	}





	@Override
	protected EveryFrameScript createEvent() {
		if (random.nextFloat() < CHECK_PROB) return null;

		StarSystemAPI system = Roider_Gen.pickSystemForRoiderBase();
		if (system == null) return null;

		RoiderBaseTier tier = pickTier();

		String factionId = Factions.INDEPENDENT;

		Roider_RoiderBaseIntelV2 intel = new Roider_RoiderBaseIntelV2(system, factionId, tier);
        intel.init();
		if (intel.isDone()) intel = null;

		return intel;
	}

	public float getDaysSinceStart() {
		float days = Global.getSector().getClock().getElapsedDaysSince(start) + extraDays;
		if (Misc.isFastStartExplorer()) {
			days += 180f - 30f;
		} else if (Misc.isFastStart()) {
			days += 180f + 60f;
		}
		return days;
	}

	/**
	 * 0 at six months (depending on start option chosen), goes up to 1 two years later.
	 * @return
	 */
	public float getStandardTimeFactor() {
		float timeFactor = (Roider_RoiderBaseManager.getInstance().getDaysSinceStart() - 180f) / (365f * 2f);
		if (timeFactor < 0) timeFactor = 0;
		if (timeFactor > 1) timeFactor = 1;
		return timeFactor;
	}

	public float getExtraDays() {
		return extraDays;
	}

	public void setExtraDays(float extraDays) {
		this.extraDays = extraDays;
	}

	protected RoiderBaseTier pickTier() {
		float days = getDaysSinceStart();

		days += numDestroyed * 200;

		WeightedRandomPicker<RoiderBaseTier> picker = new WeightedRandomPicker<>();

		if (days < 360) {
			picker.add(RoiderBaseTier.TIER_1_1MODULE, 10f);
			picker.add(RoiderBaseTier.TIER_2_1MODULE, 10f);
		} else if (days < 720f) {
			picker.add(RoiderBaseTier.TIER_2_1MODULE, 10f);
			picker.add(RoiderBaseTier.TIER_3_2MODULE, 10f);
		} else if (days < 1080f) {
			picker.add(RoiderBaseTier.TIER_3_2MODULE, 10f);
			picker.add(RoiderBaseTier.TIER_4_3MODULE, 10f);
		} else {
			picker.add(RoiderBaseTier.TIER_3_2MODULE, 10f);
			picker.add(RoiderBaseTier.TIER_4_3MODULE, 10f);
			picker.add(RoiderBaseTier.TIER_5_3MODULE, 10f);
		}


//		if (true) {
//			picker.clear();
//			picker.add(PirateBaseTier.TIER_1_1MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_5_3MODULE, 10f);
//		}


		return picker.pick();
	}

	public static float genBaseUseTimeout() {
		return 120f + 60f * (float) Math.random();
	}
	public static void markRecentlyUsedForBase(StarSystemAPI system) {
		if (system != null && system.getCenter() != null) {
			system.getCenter().getMemoryWithoutUpdate().set(PirateBaseManager.RECENTLY_USED_FOR_BASE, true, genBaseUseTimeout());
		}
	}

	public int getNumDestroyed() {
		return numDestroyed;
	}

	public void setNumDestroyed(int numDestroyed) {
		this.numDestroyed = numDestroyed;
	}

	public void incrDestroyed() {
		numDestroyed++;
	}

}















