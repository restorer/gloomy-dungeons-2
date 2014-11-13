package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.managers.SoundManager;

public class EndLevel implements EngineObject {
	protected static final float LINE_HEIGHT = 0.25f;
	protected static final float LINE_OFFSET = LINE_HEIGHT / 2.0f;

	protected Engine engine;
	protected Renderer renderer;
	protected State state;
	protected Labels labels;
	protected SoundManager soundManager;
	protected Game game;

	protected int totalKills;
	protected int totalItems;
	protected int totalSecrets;
	protected int totalSeconds;
	protected float currentKills;
	protected float currentItems;
	protected float currentSecrets;
	protected float currentSeconds;
	protected float currentAdd;
	protected int timeout;
	protected float ystart;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.renderer = engine.renderer;
		this.state = engine.state;
		this.labels = engine.labels;
		this.soundManager = engine.soundManager;
		this.game = engine.game;
	}

	public void init(int totalKills, int totalItems, int totalSecrets, int totalSeconds) {
		this.totalKills = totalKills;
		this.totalItems = totalItems;
		this.totalSecrets = totalSecrets;
		this.totalSeconds = totalSeconds;

		currentKills = 0.0f;
		currentItems = 0.0f;
		currentSecrets = 0.0f;
		currentSeconds = 0.0f;

		currentAdd = 1.0f;
		timeout = 0;
		ystart = 0.0f;

		if (totalKills >= 0) {
			ystart -= LINE_OFFSET;
		}

		if (totalItems >= 0) {
			ystart -= LINE_OFFSET;
		}

		if (totalSecrets >= 0) {
			ystart -= LINE_OFFSET;
		}
	}

	public void update() {
		if (game.actionFire != 0) {
			soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
			game.loadLevel(Game.LOAD_LEVEL_RELOAD);
			return;
		}

		if (timeout > 0) {
			timeout--;
			return;
		}

		timeout = 3;
		boolean playSound = false;

		if (currentKills < totalKills) {
			currentKills = Math.min(totalKills, currentKills + currentAdd);
			playSound = true;
		}

		if (currentItems < totalItems) {
			currentItems = Math.min(totalItems, currentItems + currentAdd);
			playSound = true;
		}

		if (currentSecrets < totalSecrets) {
			currentSecrets = Math.min(totalSecrets, currentSecrets + currentAdd);
			playSound = true;
		}

		if (currentSeconds < totalSeconds) {
			currentSeconds = Math.min(totalSeconds, currentSeconds + currentAdd);
			playSound = true;
		}

		currentAdd += 0.2f;

		if (playSound) {
			soundManager.playSound(SoundManager.SOUND_SHOOT_PIST);
		}
	}

	public void render(GL10 gl) {
		labels.beginDrawing(gl);
		renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

		float sx = -engine.ratio + 0.1f;
		float ex = engine.ratio - 0.1f;
		float my = ystart;

		labels.draw(
			gl, sx, my - LINE_OFFSET, ex, my + LINE_OFFSET,
			String.format(labels.map[Labels.LABEL_ENDL_TIME], Common.getTimeString((int)currentSeconds)),
			0.225f, Labels.ALIGN_CC
		);

		my += LINE_HEIGHT;

		if (totalSecrets >= 0) {
			labels.draw(
				gl, sx, my - LINE_OFFSET, ex, my + LINE_OFFSET,
				String.format(labels.map[Labels.LABEL_ENDL_SECRETS], (int)currentSecrets),
				0.225f, Labels.ALIGN_CC
			);

			my += LINE_HEIGHT;
		}

		if (totalItems >= 0) {
			labels.draw(
				gl, sx, my - LINE_OFFSET, ex, my + LINE_OFFSET,
				String.format(labels.map[Labels.LABEL_ENDL_ITEMS], (int)currentItems),
				0.225f, Labels.ALIGN_CC
			);

			my += LINE_HEIGHT;
		}

		if (totalKills >= 0) {
			labels.draw(
				gl, sx, my - LINE_OFFSET, ex, my + LINE_OFFSET,
				String.format(labels.map[Labels.LABEL_ENDL_KILLS], (int)currentKills),
				0.225f, Labels.ALIGN_CC
			);
		}

		labels.endDrawing(gl);
	}
}
