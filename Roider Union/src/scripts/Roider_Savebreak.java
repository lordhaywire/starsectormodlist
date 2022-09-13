package scripts;

import com.fs.starfarer.api.EveryFrameScript;

/**
 * Author: SafariJohn
 */
public class Roider_Savebreak implements EveryFrameScript {

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        // Does nothing
    }

}
