package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;

// borrowed with permission from Nia
// Thanks for the script!

public class sfcamabbaflash implements BeamEffectPlugin {

    private IntervalUtil flashInterval = new IntervalUtil(0.1f,0.2f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        flashInterval.advance(engine.getElapsedInLastFrame());
        if (flashInterval.intervalElapsed()) {
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), beam.getWidth(), 0.8f, 0.2f, beam.getCoreColor());
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), 2f, 0.8f, 0.2f, beam.getFringeColor().brighter());
            engine.addHitParticle(beam.getTo(), beam.getSource().getVelocity(), 2f * 3f, 0.8f, 0.2f, beam.getFringeColor());
        }

    }
}
