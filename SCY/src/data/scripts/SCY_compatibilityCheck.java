package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;

class SCY_compatibilityCheck implements EveryFrameScript {
            
    @Override
    public void advance(float amount) {
    }    
    
    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
