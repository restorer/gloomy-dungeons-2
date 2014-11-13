package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.Game;
import zame.game.engine.State;

public abstract class OnScreenController {
	protected Controls owner;
	protected Engine engine;
	protected Config config;
	protected Game game;
	protected State state;
	protected float prevOffsetX = 0.0f;
	protected float prevOffsetY = 0.0f;
	protected float offsetXMinBound;
	protected float offsetXMaxBound;
	protected float offsetYMinBound;
	protected float offsetYMaxBound;

	public int pointerId = -1;
	public float startX = 0.0f;
	public float startY = 0.0f;
	public float offsetX = 0.0f;
	public float offsetY = 0.0f;
	public int renderModeMask = Game.RENDER_MODE_GAME;
	public int position = 0;
	public int helpLabelId = -1;

	public void setOwner(Controls owner, Engine engine) {
		this.owner = owner;
		this.engine = engine;
		this.config = engine.config;
		this.game = engine.game;
		this.state = engine.state;
	}

	public void surfaceSizeChanged() {
		offsetXMinBound = -(float)engine.width * 0.5f;
		offsetXMaxBound = (float)engine.width * 0.5f;
		offsetYMinBound = -(float)engine.height * 0.5f;
		offsetYMaxBound = (float)engine.height * 0.5f;
	}

	public abstract boolean pointerDown(float x, float y);

	public boolean pointerMove(float x, float y) {
		offsetX = x - startX;
		offsetY = y - startY;

		float diffX = offsetX - prevOffsetX;
		float diffY = offsetY - prevOffsetY;

		if (diffX < offsetXMinBound || diffX > offsetXMaxBound || diffY < offsetYMinBound || diffY > offsetYMaxBound) {
			return false;
		}

		prevOffsetX = offsetX;
		prevOffsetY = offsetY;
		return true;
	}

	public void pointerUp() {
		pointerId = -1;
		offsetX = 0.0f;
		offsetY = 0.0f;
		prevOffsetX = 0.0f;
		prevOffsetY = 0.0f;
	}

	public void render(GL10 gl, long elapsedTime) {
	}

	public void updateHero() {
	}

	public float getDisplayX() {
		return startX;
	}

	public float getDisplayY() {
		return startY;
	}
}
