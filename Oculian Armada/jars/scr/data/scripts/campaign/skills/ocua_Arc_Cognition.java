package data.scripts.campaign.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ocua_Arc_Cognition {
	
	public static int SUPPLY_BONUS = 1;
	public static int DEMAND_BONUS = 2;
	public static float ACCESS = 0.1f;
	public static float FLEET_SIZE = 25f;
	public static float QUALITY_SHIP = 50f;
	public static int DEFEND_BONUS = 50;
	public static float STABILITY_BONUS = 1;
	
	
	public static class Level1 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).modifyFlat(id, SUPPLY_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "All industries supply " + SUPPLY_BONUS + " more unit of all the commodities they produce";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level2 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.DEMAND_REDUCTION_MOD).modifyFlat(id, DEMAND_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.DEMAND_REDUCTION_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "All industries demand " + DEMAND_BONUS + " less unit of all the commodities they consume";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level3 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, QUALITY_SHIP / 100f, "Mother-cognition");
		}
		
		public void unapply(MarketAPI market, String id) {
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)Math.round(QUALITY_SHIP) + "% ship quality";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level4 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, FLEET_SIZE / 100f, "Mother-cognition");
		}
		
		public void unapply(MarketAPI market, String id) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			//return "" + (int)Math.round(FLEET_SIZE) + "% larger fleets";
			return "+" + (int)Math.round(FLEET_SIZE) + "% fleet size";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level5 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + DEFEND_BONUS * 0.01f, "Mother-cognition");
		}

		public void unapply(MarketAPI market, String id) {
			//market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyPercent(id);
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DEFEND_BONUS) + "% effectiveness of ground defenses";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level6 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStability().modifyFlat(id, STABILITY_BONUS, "Mother-cognition");
		}

		public void unapply(MarketAPI market, String id) {
			market.getStability().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)STABILITY_BONUS + " stability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}


