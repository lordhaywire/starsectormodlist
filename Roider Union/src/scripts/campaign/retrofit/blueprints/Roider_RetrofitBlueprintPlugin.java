package scripts.campaign.retrofit.blueprints;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import static com.fs.starfarer.api.campaign.impl.items.ShipBlueprintItemPlugin.pickShip;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Fitters;
import java.awt.Color;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import scripts.campaign.retrofit.Roider_RetrofitVerifier;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_RetrofitBlueprintPlugin extends BaseSpecialItemPlugin implements Roider_RetrofitVerifier {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_RetrofitBlueprintPlugin.class, "ship", "s");
        x.aliasAttribute(Roider_RetrofitBlueprintPlugin.class, "sources", "o");
    }

	protected ShipHullSpecAPI ship;
    protected List<String> sources;

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);
		ship = Global.getSettings().getHullSpec(stack.getSpecialDataIfSpecial().getData());
        sources = getSources(ship);
    }

	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		float cx = x + w/2f;
		float cy = y + h/2f;

		float blX = cx - 22f;
		float blY = cy - 13f;
		float tlX = cx - 13f;
		float tlY = cy + 25f;
		float trX = cx + 26f;
		float trY = cy + 25f;
		float brX = cx + 19f;
		float brY = cy - 14f;

		String hullId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsShip(hullId);

		float mult = 1f;
		//if (known) mult = 0.5f;

		Color bgColor = Global.getSector().getPlayerFaction().getDarkUIColor();
//		Color bgColor = Global.getSector().getFaction(Factions.PIRATES).getBrightUIColor();
		bgColor = Misc.setAlpha(bgColor, 255);

		//float b = Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.25f;
		renderer.renderBGWithCorners(bgColor, blX, blY, tlX, tlY, trX, trY, brX, brY,
				 alphaMult * mult, glowMult * 0.5f * mult, false);
		renderer.renderShipWithCorners(hullId, null, blX, blY, tlX, tlY, trX, trY, brX, brY,
				alphaMult * mult, glowMult * 0.5f * mult, !known);


		SpriteAPI overlay = Global.getSettings().getSprite("ui", "bpOverlayShip");
		overlay.setColor(Color.green);
		overlay.setColor(Global.getSector().getPlayerFaction().getBrightUIColor());
		overlay.setAlphaMult(alphaMult);
		overlay.setNormalBlend();
		renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);


		if (known) {
			renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY,
					alphaMult * 0.5f, 0f, false);
		}


		overlay.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
	}

	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		if (ship != null) {
			float base = super.getPrice(market, submarket);
			return (int)(base + ship.getBaseValue() * getItemPriceMult() / 2f);
		}
		return super.getPrice(market, submarket) / 2;
	}

    public String getProvidedShip() {
        return ship.getHullId();
    }

	@Override
	public String getName() {
		if (ship != null) {
			return ship.getNameWithDesignationWithDashClass() + " Retrofit Template";
		}
		return super.getName();
	}

	@Override
	public String getDesignType() {
		if (ship != null) {
			return ship.getManufacturer();
		}
		return null;
	}

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded,
                CargoTransferHandlerAPI transferHandler, Object stackSource) {
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color c = Misc.getTextColor();
		Color g = Misc.getGrayColor();
		Color p = Misc.getPositiveHighlightColor();
		Color n = Misc.getNegativeHighlightColor();

		tooltip.addTitle(getName());

		String design = getDesignType();
		Misc.addDesignTypePara(tooltip, design, 10f);

		if (!spec.getDesc().isEmpty()) {
			tooltip.addPara(spec.getDesc(), g, opad);
		}



		String hullId = stack.getSpecialDataIfSpecial().getData();
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();

		boolean known = playerFaction.knowsShip(hullId);

        boolean sourceKnown = sources.isEmpty();
        for (String source : sources) {
            if (playerFaction.knowsShip(source)) {
                sourceKnown = true;
                break;
            }
        }

		List<String> hulls = new ArrayList<String>();
		hulls.add(hullId);
		addShipList(tooltip, "Ship hulls:", hulls, 1, opad);
		Description desc = Global.getSettings().getDescription(ship.getDescriptionId(), Description.Type.SHIP);

		String prefix = "";
		if (ship.getDescriptionPrefix() != null) {
			prefix = ship.getDescriptionPrefix() + "\n\n";
		}
		tooltip.addPara(prefix + desc.getText1FirstPara(), opad);

		if (!sources.isEmpty()) addSourceShips(tooltip, "Source hulls:", sources, 10, opad);

		addCostLabel(tooltip, opad, transferHandler, stackSource);

		if (known) {
			tooltip.addPara("Already known", g, opad);
		} else if (sourceKnown) {
			tooltip.addPara("Right-click to learn", p, opad);
		} else {
			tooltip.addPara("Need to know at least one source hull", n, opad);
        }
    }

	@Override
	public boolean hasRightClickAction() {
		return true;
	}

    @Override
    public boolean shouldRemoveOnRightClickAction() {
		String hullId = stack.getSpecialDataIfSpecial().getData();

        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        if (playerFaction.knowsShip(hullId)) return false;

        for (String source : sources) {
            if (playerFaction.knowsShip(source)) return true;
        }

        return sources.isEmpty();
    }

    @Override
    public void performRightClickAction() {
		String hullId = stack.getSpecialDataIfSpecial().getData();
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();

		boolean known = playerFaction.knowsShip(hullId);

        boolean sourceKnown = false;
        for (String source : sources) {
            if (playerFaction.knowsShip(source)) {
                sourceKnown = true;
                break;
            }
        }

        MessageDisplayAPI display = Global.getSector().getCampaignUI().getMessageDisplay();

		if (known) {
			display.addMessage("" + ship.getNameWithDesignationWithDashClass()
                        + ": conversion process already known");
		} else if (sourceKnown) {
			Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
			playerFaction.addKnownShip(hullId, true);

			display.addMessage( "Acquired conversion process: "
                        + ship.getNameWithDesignationWithDashClass());
		} else {
			display.addMessage("You must know at least one source"
                        + " hull of the " + ship.getNameWithDesignationWithDashClass()
                        + " to learn its conversion process");
        }
    }

	@Override
	public String resolveDropParamsToSpecificItemData(String params, Random random) throws JSONException {
		if (params == null || params.isEmpty()) return null;


		JSONObject json = new JSONObject(params);

		Set<String> tags = new HashSet<String>();
		if (json.has("tags")) {
			JSONArray tagsArray = json.getJSONArray("tags");
			for (int i = 0; i < tagsArray.length(); i++) {
				tags.add(tagsArray.getString(i));
			}
		}

		return pickShip(tags, random);
	}

    public List<String> getSources(ShipHullSpecAPI ship) {
        Set<String> src = new HashSet<>();

        List<RetrofitData> allData = Roider_RetrofitsKeeper.getRetrofits(this, Roider_Fitters.FULL);

        for (RetrofitData data : allData) {
            src.add(data.sourceHull);
        }

        return new ArrayList<>(src);
    }

    @Override
    public RetrofitData verifyData(String id, String fitter,
                String source, String target, double cost,
                double time, RepLevel rep, boolean commission) {
        if (!target.equals(ship.getBaseHullId())) return null;

        return new RetrofitData(id, Roider_Fitters.ALL, source,
                    target, cost, time, rep, commission);
    }

    public void addSourceShips(TooltipMakerAPI tooltip, String title, List<String> ids, int max, float opad) {
		Color grey = Misc.getGrayColor();
		Color positive = Misc.getButtonTextColor();

        BlueprintLister lister = new BlueprintLister() {
			public boolean isKnown(String id) {
				return Global.getSector().getPlayerFaction().knowsShip(id);
			}
			public String getNoun(int num) {
				if (num == 1) return "hull";
				return "hulls";
			}
			public String getName(String id) {
				ShipHullSpecAPI spec = Global.getSettings().getHullSpec(id);
				return spec.getNameWithDesignationWithDashClass();
			}
		};

		tooltip.addPara(title, opad);

		String tab = "        ";
		float small = 5f;
		float pad = small;

		int left = ids.size();

        Collections.sort(ids, Collections.reverseOrder());

//        ids.sort(Collections.reverseOrder());

		List<String> copy = new ArrayList<String>();
		for (String id : ids) {
			if (!lister.isKnown(id)) copy.add(id);
		}
		for (String id : ids) {
			if (lister.isKnown(id)) copy.add(id);
		}

		ids = copy;
		for (String id : ids) {
			boolean known = lister.isKnown(id);

			if (known) {
				tooltip.addPara(tab + lister.getName(id), positive, pad);
			} else {
				tooltip.addPara(tab + lister.getName(id) + " (unknown)", grey, pad);
			}
			left--;
			pad = 3f;
			if (ids.size() - left >= max - 1) break;
		}
		if (ids.isEmpty()) {
			tooltip.addPara(tab + "None", pad);
		}
		if (left > 0) {
			String noun = lister.getNoun(left);
			tooltip.addPara(tab + "... and %s other " + noun + "", pad, Misc.getHighlightColor(), "" + left);
		}
    }
}
