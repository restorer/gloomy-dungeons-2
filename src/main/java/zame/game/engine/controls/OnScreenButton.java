package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.Labels;
import zame.game.engine.TextureLoader;

public class OnScreenButton extends OnScreenController {
	public static final int TYPE_TOGGLE_MAP = 1;
	public static final int TYPE_NEXT_WEAPON = 2;
	public static final int TYPE_GAME_MENU = 3;

	protected int type;
	protected float fromX;
	protected float fromY;
	protected float toX;
	protected float toY;
	protected boolean active;

	public OnScreenButton(int position, int type) {
		this.position = position;
		this.type = type;

		if (type == TYPE_TOGGLE_MAP) {
			this.helpLabelId = Labels.LABEL_HELP_MAP;
		} else if (type == TYPE_GAME_MENU) {
			this.helpLabelId = Labels.LABEL_HELP_MENU;
		}
	}

	@Override
	public void surfaceSizeChanged() {
		super.surfaceSizeChanged();

		float btnOffsetX = owner.iconSize * 0.55f;
		float btnOffsetY = owner.iconSize * 0.55f;
		float btnClickArea = owner.iconSize * 0.5f;

		if ((position & Controls.POSITION_TOP) != 0) {
			startY = btnOffsetY;
		} else {
			startY = (float)engine.height - 1.0f - btnOffsetY;
		}

		if ((position & Controls.POSITION_RIGHT) != 0) {
			startX = (float)engine.width - 1.0f - btnOffsetX;
		} else {
			startX = btnOffsetX;
		}

		fromX = startX - btnClickArea;
		fromY = startY - btnClickArea;
		toX = startX + btnClickArea;
		toY = startY + btnClickArea;
	}

	@Override
	public boolean pointerDown(float x, float y) {
		if (x >= fromX && x <= toX && y >= fromY && y <= toY) {
			if (type == TYPE_TOGGLE_MAP) {
				game.actionToggleMap = true;
				engine.interracted = true;
			} else if (type == TYPE_NEXT_WEAPON) {
				game.actionNextWeapon = true;
				engine.interracted = true;
			} else if (type == TYPE_GAME_MENU) {
				game.actionGameMenu = true;
			}

			active = true;
			return true;
		}

		return false;
	}

	@Override
	public void pointerUp() {
		super.pointerUp();
		active = false;

		if (type == TYPE_TOGGLE_MAP) {
			game.actionToggleMap = false;
		} else if (type == TYPE_NEXT_WEAPON) {
			game.actionNextWeapon = false;
		} else if (type == TYPE_GAME_MENU) {
			game.actionGameMenu = false;
		}
	}

	@Override
	public void render(GL10 gl, long elapsedTime) {
		int tex = 0;

		if (type == TYPE_TOGGLE_MAP) {
			tex = TextureLoader.ICON_MAP;
		} else if (type == TYPE_GAME_MENU) {
			tex = TextureLoader.ICON_MENU;
		}

		owner.drawIcon(startX, startY, tex, active, false, elapsedTime);
	}
}
