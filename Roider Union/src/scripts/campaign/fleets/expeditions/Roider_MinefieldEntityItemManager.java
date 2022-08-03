package scripts.campaign.fleets.expeditions;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.GenericFieldItemManager;
import com.fs.starfarer.api.impl.campaign.GenericFieldItemSprite;

/**
 * Author: SafariJohn
 */
public class Roider_MinefieldEntityItemManager extends GenericFieldItemManager {

    public static float MIN_MINE_SIZE = 8f;
    public static float MAX_MINE_SIZE = 24f;

	public String glowCategory, glowKey;

    public boolean fade = false;

    public Roider_MinefieldEntityItemManager(SectorEntityToken entity) {
        super(entity);

        cellSize = 88;

        minSize = MIN_MINE_SIZE;
        maxSize = MAX_MINE_SIZE;
    }

	protected void addPiecesToMax() {
        if (fade) return;

		while (items.size() < numPieces) {
			float size = minSize + (maxSize - minSize) * (float) Math.random();
			GenericFieldItemSprite item = new Roider_MinefieldItemSprite(entity, category, key, glowCategory, glowKey, cellSize, size,
									entity.getRadius() * 0.75f);
			items.add(item);
		}
	}

    public void fadeOut() {
        fade = true;

        for (GenericFieldItemSprite item : items) {
            ((Roider_MinefieldItemSprite) item).fadeOut();
        }
    }

    public boolean isFadedOut() {
        if (items == null) return false;

        for (GenericFieldItemSprite item : items) {
            if (!item.isDone()) return false;
        }

        return true;
    }
}
