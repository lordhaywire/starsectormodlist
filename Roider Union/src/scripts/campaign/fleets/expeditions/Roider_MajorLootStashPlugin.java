package scripts.campaign.fleets.expeditions;

import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids.Roider_Settings;

/**
 * Author: SafariJohn
 */
public class Roider_MajorLootStashPlugin extends BaseCustomEntityPlugin {

//	private CustomCampaignEntityAPI entity;
	private transient Roider_MinefieldEntityItemManager manager;

	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		//this.entity = (CustomCampaignEntityAPI) entity;
		readResolve();
	}

	Object readResolve() {
		manager = new Roider_MinefieldEntityItemManager(entity);
		manager.category = Roider_Settings.LOOT_STASH_MINE_IMAGES;
		manager.key = Roider_Settings.LOOT_STASH_MINE;
		manager.glowCategory = Roider_Settings.LOOT_STASH_MINE_IMAGES;
		manager.glowKey = Roider_Settings.LOOT_STASH_MINE_GLOW;
		manager.cellSize = 88;

		manager.minSize = 2;
		manager.maxSize = 6;

		//manager.initDebrisIfNeeded();
		//manager.numPieces = 15;

		return this;
	}

	public void advance(float amount) {
        if (manager.isFadedOut()) {
            Misc.fadeAndExpire(entity);
            return;
        }

		if (entity.isInCurrentLocation()) {
			float totalCapacity = entity.getRadius();
			int minPieces = 5;
			int numPieces = (int) (totalCapacity / 4);
			if (numPieces < minPieces) numPieces = minPieces;
			if (numPieces > 40) numPieces = 40;

			manager.numPieces = numPieces;
		}

		manager.advance(amount);
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		manager.render(layer, viewport);
	}

    public void fadeOut() {
        manager.fadeOut();
}

    public boolean isFadedOut() {
        return manager.isFadedOut();
    }
}
