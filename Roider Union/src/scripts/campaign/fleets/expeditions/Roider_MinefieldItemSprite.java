package scripts.campaign.fleets.expeditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.GenericFieldItemSprite;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lwjgl.opengl.GL11;

/**
 * Author: SafariJohn
 */
public class Roider_MinefieldItemSprite extends GenericFieldItemSprite {

    public static float GLOW_FREQUENCY = 0.3f; // on/off cycles per second
    public static float COLOUR_CHANGE_RANGE = 300f;

    protected boolean pause = false;

    protected float phase = 0f;
    protected float freqMult = 1f;

    protected float rotation;

    public Roider_MinefieldItemSprite(SectorEntityToken entity,
                String spriteCat, String spriteKey,
                String glowCat, String glowKey,
                float cellSize, float size, float spawnRadius) {
        super(entity, spriteCat, spriteKey, cellSize, size, spawnRadius);

        freqMult = (float) (2f * Math.random());

        this.rotation = (float)Math.random() * 10.0F - 5.0F;
        this.facing = (float)Math.random() * 360.0F;

        glow = Global.getSettings().getSprite(glowCat, glowKey);

//        sprite.setSize(size, size);
//        sprite.setTexX(0);
//        sprite.setTexY(0);
//        sprite.setTexWidth(cellSize);
//        sprite.setTexHeight(cellSize);

		int cols = 1;
		int rows = 1;


		float cellX = (int) (Math.random() * cols);
		float cellY = (int) (Math.random() * rows);

		float ctw = sprite.getTextureWidth() / (float) cols;
		float cth = sprite.getTextureHeight() / (float) rows;

		if (glow != null) {
			glow.setTexX(cellX * ctw);
			glow.setTexY(cellY * cth);
			glow.setTexWidth(ctw);
			glow.setTexHeight(cth);
            glow.setSize(size, size);

            Color glowColor = new Color(255,30,0,255);
            glow.setColor(glowColor);
		}
    }

    public void fadeOut() {
        fader.fadeOut();
        glow.setColor(new Color(20,255,20,255));
    }

	public void render(float alphaMult) {

		//alphaMult *= fader.getBrightness();
		if (alphaMult <= 0) return;

		SectorEntityToken lightSource = entity.getLightSource();
		if (lightSource != null && entity.getLightColor() != null) {
			sprite.setColor(entity.getLightColor());
		} else {
			sprite.setColor(Color.white);
		}

		sprite.setAngle(facing - 90);
		sprite.setNormalBlend();
		sprite.setAlphaMult(alphaMult * fader.getBrightness());
		sprite.renderAtCenter(loc.x, loc.y);

        alphaMult *= entity.getSensorFaderBrightness();
        alphaMult *= entity.getSensorContactFaderBrightness();
        if (alphaMult <= 0f) return;

		if (lightSource != null && !entity.getLightSource().hasTag(Tags.AMBIENT_LS)) {
			float w = shadowMask.getWidth() * 1.41f;
			float h = w;

			// clear out destination alpha in area we care about
			GL11.glColorMask(false, false, false, true);
			GL11.glPushMatrix();
			GL11.glTranslatef(loc.x, loc.y, 0);
			Misc.renderQuadAlpha(0 - w/2f - 1f, 0 - h/2f - 1f, w + 2f, h + 2f, Misc.zeroColor, 0f);
			GL11.glPopMatrix();
			sprite.setBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
			sprite.renderAtCenter(loc.x, loc.y);

			float lightDir = Misc.getAngleInDegreesStrict(entity.getLocation(), lightSource.getLocation());
			shadowMask.setAlphaMult(alphaMult);
			shadowMask.setAngle(lightDir);
			shadowMask.setBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
			shadowMask.renderAtCenter(loc.x, loc.y);

			GL11.glColorMask(true, true, true, false);
			shadowMask.setBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
			shadowMask.renderAtCenter(loc.x, loc.y);
		}

        //if(!pause){
            float glowAlpha = 0f;
            if (phase < 0.5f) glowAlpha = phase * 2f;
            if (phase >= 0.5f) glowAlpha = (1f - (phase - 0.5f) * 2f);

            float glowAngle1 = (((phase * 1.3f) % 1) - 0.5f) * 12f;
            float glowAngle2 = (((phase * 1.9f) % 1) - 0.5f) * 12f;

//            glow.setSize(radius*2, radius*2);
            glow.setSize(width, height);
            glow.setAlphaMult(alphaMult * glowAlpha * fader.getBrightness());
            glow.setAdditiveBlend();

            glow.setAngle(facing - 90f + glowAngle1);
            glow.renderAtCenter(loc.x, loc.y);

            glow.setAngle(facing - 90f + glowAngle2);
            glow.setAlphaMult(alphaMult * glowAlpha * 0.5f * fader.getBrightness());
            glow.renderAtCenter(loc.x, loc.y);
       // }
    }

    public void advance(float days) {
        super.advance(days);

        float amount = Global.getSector().getClock().convertToSeconds(days);

        this.facing += this.rotation * amount;

        //---------

        phase += amount * GLOW_FREQUENCY * freqMult;
        while (phase > 1) {
            pause = !pause;
            phase--;
        }
    }

}
