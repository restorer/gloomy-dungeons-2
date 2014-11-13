package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.MyApplication;
import zame.game.engine.Game;
import zame.game.engine.Labels;
import zame.game.engine.TextureLoader;

public class OnScreenFireAndRotate extends OnScreenController {
	protected float fromX;
	protected float toX;
	protected float fireBtnX;
	protected float fireBtnY;
	protected float fireFromX;
	protected float fireFromY;
	protected float fireToX;
	protected float fireToY;
	protected float heroA;
	protected float heroVertA;
	protected boolean fireActive;

	public OnScreenFireAndRotate(int position) {
		this.position = position;
		this.renderModeMask = Game.RENDER_MODE_ALL;
		this.helpLabelId = Labels.LABEL_HELP_FIRE;
	}

	@Override
	public void surfaceSizeChanged() {
		super.surfaceSizeChanged();

		float btnOffsetX = owner.iconSize * 2.0f;
		float btnOffsetY = owner.iconSize * 1.25f;
		float btnClickArea = owner.iconSize * (MyApplication.self.isLargeDevice ? (0.75f * 0.5f) : 0.5f);

		if ((position & Controls.POSITION_TOP) != 0) {
			fireBtnY = btnOffsetY;
		} else {
			fireBtnY = (float)engine.height - 1.0f - btnOffsetY;
		}

		if ((position & Controls.POSITION_RIGHT) != 0) {
			fromX = (float)engine.width * 0.4f;
			toX = (float)engine.width;
			fireBtnX = (float)engine.width - 1.0f - btnOffsetX;
		} else {
			fromX = 0.0f;
			toX = (float)engine.width * 0.6f;
			fireBtnX = btnOffsetX;
		}

		fireFromX = fireBtnX - btnClickArea;
		fireFromY = fireBtnY - btnClickArea;
		fireToX = fireBtnX + btnClickArea;
		fireToY = fireBtnY + btnClickArea;
	}

	@Override
	public boolean pointerDown(float x, float y) {
		if (x < fromX || x > toX) {
			return false;
		}

		startX = x;
		startY = y;

		heroA = state.heroA;
		heroVertA = state.heroVertA;

		if (x >= fireFromX && x <= fireToX && y >= fireFromY && y <= fireToY) {
			game.actionFire |= Controls.ACTION_FIRE_ONSCREEN;
			fireActive = true;
			engine.interracted = true;
		}

		return true;
	}

	@Override
	public void pointerUp() {
		super.pointerUp();

		game.actionFire &= ~Controls.ACTION_FIRE_ONSCREEN;
		fireActive = false;
	}

	@Override
	public void render(GL10 gl, long elapsedTime) {
		owner.drawIcon(fireBtnX, fireBtnY, TextureLoader.ICON_SHOOT, fireActive, false, elapsedTime);
	}

	@Override
	public void updateHero() {
		if (pointerId < 0) {
			if (Math.abs(state.heroVertA) > 0.5f) {
				state.heroVertA *= 0.75f;

				if (Math.abs(state.heroVertA) <= 0.5f) {
					state.heroVertA = 0.0f;
				}
			}

			return;
		}

		float sign = (offsetX < 0.0f ? -1.0f : 1.0f);
		float value = Math.abs(offsetX) / (float)engine.width;

		/*
		if (value < 0.2f) {
			value *= 5.0f;
			value *= value;
			value *= 0.2f;
		}
		*/

		if (value < 0.25f) {
			value *= 5.0f;
			value *= value;
			value *= 0.16f;
		}

		float rotateHor = value * sign * 360.0f;

		sign = (offsetY < 0.0f ? -1.0f : 1.0f);
		value = Math.abs(offsetY) / (float)engine.height;

		/*
		if (value < 0.2f) {
			value *= 5.0f;
			value *= value;
			value *= 0.2f;
		}
		*/

		if (value < 0.25f) {
			value *= 5.0f;
			value *= value;
			value *= 0.16f;
		}

		float rotateVert = value * sign * 45.0f;

		if (rotateHor > 0.5f) {
			engine.interracted = true;
		}

		state.setHeroA(heroA - rotateHor * config.horizontalLookMult * config.rotateSpeed);
		state.setHeroVertA(heroVertA - rotateVert * config.verticalLookMult);
	}

	@Override
	public float getDisplayX() {
		return fireBtnX;
	}

	@Override
	public float getDisplayY() {
		return fireBtnY;
	}
}
