package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.managers.SoundManager;

public class GameOver implements EngineObject {
	protected Engine engine;
	protected Renderer renderer;
	protected State state;
	protected Labels labels;
	protected SoundManager soundManager;
	protected Game game;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.renderer = engine.renderer;
		this.state = engine.state;
		this.labels = engine.labels;
		this.soundManager = engine.soundManager;
		this.game = engine.game;
	}

	public void update() {
		if (game.actionUpgradeButton) {
			soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
			soundManager.setPlaylist(SoundManager.LIST_MAIN);
			engine.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Upgrade.GameOver", state.levelName, 0);
			engine.changeView(Engine.VIEW_TYPE_UPGRADE);
		}

		if (game.actionFire != 0) {
			soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
			soundManager.setPlaylist(SoundManager.LIST_MAIN);
			game.loadLevel(Game.LOAD_LEVEL_NORMAL);
		}
	}

	public void render(GL10 gl) {
		labels.beginDrawing(gl);
		renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

		float sx = -engine.ratio + 0.1f;
		float ex = engine.ratio - 0.1f;

		labels.draw(
			gl, sx, 0.1f, ex, 0.5f,
			labels.map[Labels.LABEL_GAMEOVER],
			0.25f, Labels.ALIGN_CC
		);

		labels.draw(
			gl, sx, -0.25f, ex, 0.1f,
			labels.map[Labels.LABEL_GAMEOVER_LOAD_AUTOSAVE],
			0.25f, Labels.ALIGN_CC
		);

		labels.endDrawing(gl);
	}
}
