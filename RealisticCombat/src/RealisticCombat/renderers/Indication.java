package RealisticCombat.renderers;

import com.fs.starfarer.api.combat.ViewportAPI;

/**
 * Common interface for classes that indicate
 */
public interface Indication {

    /**
     * Called every frame.
     */
    void render(final ViewportAPI viewport);
}
