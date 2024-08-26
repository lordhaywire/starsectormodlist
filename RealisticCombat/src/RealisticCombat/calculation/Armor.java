package RealisticCombat.calculation;

import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public final class Armor {

    /**
     * @param armorGrid {@link ArmorGridAPI}
     * <p></p>
     * @return {@code float} thickness of the thin surface armor around a
     *         {@link ArmorGridAPI}
     */
    public static float getSurfaceArmor(final ArmorGridAPI armorGrid) {
        return armorGrid.getMaxArmorInCell();
    }

    /**
     * @param armorGrid {@link ArmorGridAPI}
     * <p></p>
     * @return {@code float} thickness of the total armor protecting a
     *         {@link ShipAPI}
     */
    public static float getTotalArmor(final ArmorGridAPI armorGrid) {
        return armorGrid.getArmorRating();
    }

    /**
     * @param collisionAngle {@code float}
     * <p></p>
     * @return {@code float} factor whereby {@link ShipAPI}
     *         armor thickens with impact angle
     */
    public static float getAngleFactor(final float collisionAngle) {
        return 1 / (float) Math.sin(Math.toRadians(collisionAngle));
    }
}
