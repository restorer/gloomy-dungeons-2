package zame.game.engine.controls;

import android.util.FloatMath;
import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.Labels;
import zame.game.engine.TextureLoader;

public class OnScreenPad extends OnScreenController {
	protected float fromX;
	protected float fromY;
	protected float toX;
	protected float toY;
	protected float minDist;
	protected float maxDist;
	protected boolean active;

	public boolean dynamic;

	public OnScreenPad(int position, boolean dynamic) {
		this.position = position;
		this.dynamic = dynamic;
		this.helpLabelId = Labels.LABEL_HELP_MOVE;
	}

	@Override
	public void surfaceSizeChanged() {
		super.surfaceSizeChanged();

		fromY = (dynamic ? ((float)engine.height * 0.25f) : ((float)engine.height - owner.iconSize * 3.0f));
		toY = (float)engine.height - 1.0f;
		startY = toY - owner.iconSize * 1.25f;

		if ((position & Controls.POSITION_RIGHT) != 0) {
			fromX = (dynamic ? ((float)engine.width * 0.6f) : ((float)engine.width - owner.iconSize * 3.0f));
			toX = (float)engine.width - 1.0f;
			startX = toX - owner.iconSize * 1.25f;
		} else {
			fromX = 0.0f;
			toX = (dynamic ? ((float)engine.width * 0.4f) : (owner.iconSize * 3.0f));
			startX = fromX + owner.iconSize * 1.25f;
		}

		minDist = owner.iconSize * 0.1f;
		maxDist = owner.iconSize * 1.0f;
	}

	@Override
	public boolean pointerDown(float x, float y) {
		if (x >= fromX && x <= toX && y >= fromY && y <= toY) {
			if (dynamic) {
				startX = x;
				startY = y;
			}

			active = false;
			return true;
		}

		return false;
	}

	@Override
	public boolean pointerMove(float x, float y) {
		if (!super.pointerMove(x, y)) {
			return false;
		}

		float dist = FloatMath.sqrt(offsetX * offsetX + offsetY * offsetY);

		if (dist > minDist) {
			active = true;
		}

		if (dist > maxDist) {
			offsetX = offsetX / dist * maxDist;
			offsetY = offsetY / dist * maxDist;
		}

		return true;
	}

	@Override
	public void pointerUp() {
		super.pointerUp();
		active = false;
	}

	@Override
	public void render(GL10 gl, long elapsedTime) {
		owner.drawBack(startX, startY, TextureLoader.BASE_BACKS, active, false, elapsedTime);
		owner.drawIcon(startX + offsetX, startY + offsetY, TextureLoader.ICON_JOY, active, false, elapsedTime);
	}

	@Override
	public void updateHero() {
		if (!active) {
			return;
		}

		float accelX = offsetX / maxDist * 0.2f;
		float accelY = offsetY / maxDist * 0.2f;

		if (Math.abs(accelX) > 0.005f) {
			game.updateHeroPosition(engine.heroSn, engine.heroCs, accelX * config.strafeSpeed);
			engine.interracted = true;
		}

		if (Math.abs(accelY) > 0.005f) {
			game.updateHeroPosition(-engine.heroCs, engine.heroSn, accelY * config.moveSpeed);
			engine.interracted = true;
		}
	}
}
