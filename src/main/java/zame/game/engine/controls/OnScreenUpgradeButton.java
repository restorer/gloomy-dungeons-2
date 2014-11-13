package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.Game;
import zame.game.engine.TextureLoader;

public class OnScreenUpgradeButton extends OnScreenController {
	protected float fromX;
	protected float fromY;
	protected float toX;
	protected float toY;
	protected boolean active;

	public OnScreenUpgradeButton(int position) {
		this.position = position;
		this.renderModeMask = Game.RENDER_MODE_GAME_OVER;
	}

	@Override
	public void surfaceSizeChanged() {
		super.surfaceSizeChanged();

		startY = (float)engine.height - 1.0f - owner.iconSize * 1.0f;
		fromY = startY - owner.iconSize * 0.5f;
		toY = startY + owner.iconSize * 0.5f;

		if ((position & Controls.POSITION_RIGHT) != 0) {
			startX = (float)engine.width - 1.0f - owner.iconSize * 3.0f;
		} else {
			startX = owner.iconSize * 3.0f;
		}

		fromX = startX - owner.iconSize * 2.5f;
		toX = startX + owner.iconSize * 2.5f;
	}

	@Override
	public boolean pointerDown(float x, float y) {
		if (x >= fromX && x <= toX && y >= fromY && y <= toY) {
			game.actionUpgradeButton = true;
			active = true;
			return true;
		}

		return false;
	}

	@Override
	public void pointerUp() {
		super.pointerUp();

		game.actionUpgradeButton = false;
		active = false;
	}

	@Override
	public void render(GL10 gl, long elapsedTime) {
		owner.drawBtn(startX, startY, TextureLoader.BASE_BACKS + 2, active, true, elapsedTime);
	}
}
