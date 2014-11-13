package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;

public class Stats implements EngineObject {
	protected static final float DIST_X_STATS = 1.0625f;
	protected static final float DIST_X_KEYS = 0.5f;
	protected static final float OFFSET_Y_STATS = 0.3f;
	protected static final float OFFSET_Y_KEYS = OFFSET_Y_STATS + 0.5f;

	protected Engine engine;
	protected Renderer renderer;
	protected Config config;
	protected Labels labels;
	protected Weapons weapons;
	protected TextureLoader textureLoader;
	protected State state;
	protected float iconSize;
	protected float startX;
	protected float startKeysX;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.renderer = engine.renderer;
		this.config = engine.config;
		this.labels = engine.labels;
		this.weapons = engine.weapons;
		this.textureLoader = engine.textureLoader;
		this.state = engine.state;
	}

	public void surfaceSizeChanged() {
		iconSize = (float)Math.min(engine.height, engine.width) / 5.0f * config.controlsScale;
		startX = -1.0f;
		startKeysX = -1.0f;
	}

	protected void drawStatIcon(GL10 gl, int pos, int texNum) {
		float pointerX = startX + (float)pos * iconSize * DIST_X_STATS;
		float pointerY = iconSize * OFFSET_Y_STATS;

		float sx = (pointerX / (float)engine.width * engine.ratio) - 0.125f * config.controlsScale;
		float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * config.controlsScale;

		float ex = sx + 0.25f * config.controlsScale;
		float ey = sy + 0.25f * config.controlsScale;

		renderer.x1 = sx; renderer.y1 = sy;
		renderer.x2 = sx; renderer.y2 = ey;
		renderer.x3 = ex; renderer.y3 = ey;
		renderer.x4 = ex; renderer.y4 = sy;

		renderer.drawQuad(texNum);
	}

	protected void drawStatLabel(GL10 gl, int pos, int value) {
		float pointerX = startX + (float)pos * iconSize * DIST_X_STATS;
		float pointerY = iconSize * OFFSET_Y_STATS;

		float sx = (pointerX / (float)engine.width * engine.ratio) + 0.0625f * config.controlsScale;
		float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * config.controlsScale;

		float ex = sx + 0.125f * config.controlsScale; // 0.0625f
		float ey = sy + 0.25f * config.controlsScale;

		labels.draw(gl, sx, sy, ex, ey, value, 0.05f * config.controlsScale, Labels.ALIGN_CL); // 0.0625f
	}

	protected void drawKeyIcon(GL10 gl, int pos, int texNum) {
		float pointerX = startKeysX + iconSize * (float)pos * DIST_X_KEYS;
		float pointerY = iconSize * OFFSET_Y_KEYS;

		float sx = (pointerX / (float)engine.width * engine.ratio) - 0.125f * config.controlsScale;
		float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * config.controlsScale;

		float ex = sx + 0.25f * config.controlsScale;
		float ey = sy + 0.25f * config.controlsScale;

		renderer.x1 = sx; renderer.y1 = sy;
		renderer.x2 = sx; renderer.y2 = ey;
		renderer.x3 = ex; renderer.y3 = ey;
		renderer.x4 = ex; renderer.y4 = sy;

		renderer.drawQuad(texNum);
	}

	public void render(GL10 gl) {
		if (startX < 0.0f) {
			if (config.leftHandAim) {
				startX = (float)engine.width - iconSize - 3.0f * iconSize * DIST_X_STATS;
			} else {
				startX = iconSize * 1.4f;
			}

			startKeysX = startX;
		}

		renderer.initOrtho(gl, true, false, 0f, engine.ratio, 0f, 1.0f, 0f, 1.0f);

		renderer.z1 = 0.0f;
		renderer.z2 = 0.0f;
		renderer.z3 = 0.0f;
		renderer.z4 = 0.0f;

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		renderer.r1 = 1.0f; renderer.g1 = 1.0f; renderer.b1 = 1.0f; renderer.a1 = 0.8f;
		renderer.r2 = 1.0f; renderer.g2 = 1.0f; renderer.b2 = 1.0f; renderer.a2 = 0.8f;
		renderer.r3 = 1.0f; renderer.g3 = 1.0f; renderer.b3 = 1.0f; renderer.a3 = 0.8f;
		renderer.r4 = 1.0f; renderer.g4 = 1.0f; renderer.b4 = 1.0f; renderer.a4 = 0.8f;

		renderer.init();

		drawStatIcon(gl, 0, TextureLoader.ICON_HEALTH);
		drawStatIcon(gl, 1, TextureLoader.ICON_ARMOR);

		if ((weapons.currentParams.ammoIdx >= 0) && (state.heroAmmo[weapons.currentParams.ammoIdx] >= 0)) {
			drawStatIcon(gl, 2, TextureLoader.ICON_AMMO);
		}

		int keyPos = 0;

		renderer.a1 = 1.0f;
		renderer.a2 = 1.0f;
		renderer.a3 = 1.0f;
		renderer.a4 = 1.0f;

		if ((state.heroKeysMask & 1) != 0) {
			drawKeyIcon(gl, keyPos, TextureLoader.ICON_BLUE_KEY);
			keyPos++;
		}

		if ((state.heroKeysMask & 2) != 0) {
			drawKeyIcon(gl, keyPos, TextureLoader.ICON_RED_KEY);
			keyPos++;
		}

		if ((state.heroKeysMask & 4) != 0) {
			drawKeyIcon(gl, keyPos, TextureLoader.ICON_GREEN_KEY);
		}

		renderer.bindTextureCtl(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
		renderer.flush(gl);

		renderer.init();

		drawStatLabel(gl, 0, state.heroHealth);
		drawStatLabel(gl, 1, state.heroArmor);

		if ((weapons.currentParams.ammoIdx >= 0) && (state.heroAmmo[weapons.currentParams.ammoIdx] >= 0)) {
			drawStatLabel(gl, 2, state.heroAmmo[weapons.currentParams.ammoIdx]);
		}

		renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_LABELS]);
		renderer.flush(gl);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}
}
