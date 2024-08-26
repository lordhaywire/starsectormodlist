package RealisticCombat.settings;

import java.util.Random;

public final class RandomGenerator {
    private static final Random random = new Random(0);

    public static int nextInt(final int limit) {
        return random.nextInt(limit);
    }
}
