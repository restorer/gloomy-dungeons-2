package zame.game.engine;

import android.util.FloatMath;
import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;
import zame.game.managers.SoundManager;

public class Explosion extends DataListItem implements EngineObject, DataItem {
	protected static final int FIELD_X = 1;
	protected static final int FIELD_Y = 2;
	protected static final int FIELD_MON_UID = 4;
	protected static final int FIELD_AMMO_IDX = 5;
	protected static final int FIELD_HITS = 6;
	protected static final int FIELD_HIT_TIMEOUT = 7;
	protected static final int FIELD_TICKS_PASSED = 8;
	protected static final int FIELD_PREV_TEX_IDX = 9;

	protected static final int BOOM_ANIMATION_SPEED = 10;
	protected static final int BARREL_HITS = GameParams.HEALTH_HIT_BARREL;
	protected static final int BARREL_HIT_TIMEOUT = 25;
	protected static final int[] barrelBoomTextures = new int[] { 1, -2, 3 };

	protected static final int[][] BOOM_TEXTURES = {
		null, // AMMO_PISTOL
		null, // AMMO_SHOGUN
		new int[] { 1, -2, 3 }, // AMMO_ROCKET
	};

	protected Engine engine;
	protected State state;
	protected Game game;
	protected Level level;
	protected LevelRenderer levelRenderer;
	protected int[] boomTextures;
	protected float prevLight;

	public float x;
	public float y;
	public int monUid;
	public int ammoIdx; // -1 for hero hit
	public int hits;
	public int hitTimeout;
	public int ticksPassed;
	public int prevTexIdx;

	public Monster mon;

	public static void boom(State state, float x, float y, Monster mon, int ammoIdx, int hits, int hitTimeout) {
		if (ammoIdx >= 0 && BOOM_TEXTURES[ammoIdx] == null) {
			return;
		}

		Explosion explosion = state.explosions.take();

		if (explosion == null) {
			// remove very old explosion and re-take new
			// тут есть опастность, что останется остаточная освещённость
			// но вероятность этого крайне мала, так что пока это никак не обрабатывается
			state.explosions.release(state.explosions.first());
			explosion = state.explosions.take();
		}

		explosion.init(x, y, mon, ammoIdx, hits, hitTimeout);

		if (!explosion.update()) {
			state.explosions.release(explosion);
		}
	}

	public static void tryBoom(State state, int cx, int cy, int hits) {
		if (state.explosivesMap[cy][cx] > hits) {
			state.explosivesMap[cy][cx] -= hits;
			return;
		}

		boom(state, (float)cx + 0.5f, (float)cy + 0.5f, null, -1, BARREL_HITS, BARREL_HIT_TIMEOUT);
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.game = engine.game;
		this.level = engine.level;
		this.levelRenderer = engine.levelRenderer;
	}

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_X, x);
		writer.write(FIELD_Y, y);
		writer.write(FIELD_MON_UID, monUid);
		writer.write(FIELD_AMMO_IDX, ammoIdx);
		writer.write(FIELD_HITS, hits);
		writer.write(FIELD_HIT_TIMEOUT, hitTimeout);
		writer.write(FIELD_TICKS_PASSED, ticksPassed);
		writer.write(FIELD_PREV_TEX_IDX, prevTexIdx);
	}

	public void readFrom(DataReader reader) {
		x = reader.readFloat(FIELD_X);
		y = reader.readFloat(FIELD_Y);
		monUid = reader.readInt(FIELD_MON_UID);
		ammoIdx = reader.readInt(FIELD_AMMO_IDX);
		hits = reader.readInt(FIELD_HITS);
		hitTimeout = reader.readInt(FIELD_HIT_TIMEOUT);
		ticksPassed = reader.readInt(FIELD_TICKS_PASSED);
		prevTexIdx = reader.readInt(FIELD_PREV_TEX_IDX);

		boomTextures = (ammoIdx >= 0 ? BOOM_TEXTURES[ammoIdx] : barrelBoomTextures);
		prevLight = 0.0f;
	}

	public void init(float x, float y, Monster mon, int ammoIdx, int hits, int hitTimeout) {
		this.x = x;
		this.y = y;
		this.mon = mon;
		this.monUid = (mon == null ? -1 : mon.uid);
		this.ammoIdx = ammoIdx;
		this.hits = hits;
		this.hitTimeout = hitTimeout;

		ticksPassed = 0;
		prevTexIdx = -1;
		boomTextures = (ammoIdx >= 0 ? BOOM_TEXTURES[ammoIdx] : barrelBoomTextures);
		hitNear();

		levelRenderer.modLightMap((int)x, (int)y, 1.0f);
		prevLight = 1.0f;
	}

	public boolean update() {
		ticksPassed++;
		int texIdx = ticksPassed / BOOM_ANIMATION_SPEED;

		if (texIdx >= boomTextures.length) {
			levelRenderer.modLightMap((int)x, (int)y, -prevLight);
			return false;
		}

		float light = 1.0f - ((float)ticksPassed / (float)(boomTextures.length * BOOM_ANIMATION_SPEED));

		if (light < prevLight) {
			levelRenderer.modLightMap((int)x, (int)y, light - prevLight);
			prevLight = light;
		}

		if (texIdx > prevTexIdx) {
			prevTexIdx = texIdx;

			if (boomTextures[texIdx] < 0) {
				boomNear();
			}
		}

		return true;
	}

	public int getTexture() {
		int texIdx = ticksPassed / BOOM_ANIMATION_SPEED;

		if (texIdx >= boomTextures.length) {
			return -1;
		} else {
			return Math.abs(boomTextures[texIdx]) - 1;
		}
	}

	protected int getHits(float dx, float dy) {
		float dist = FloatMath.sqrt(dx * dx + dy * dy) * 2.0f;

		if (dist > 1.0f) {
			return engine.getRealHits(Math.max(1, (int)((float)hits / dist)), dist);
		} else {
			return engine.getRealHits(hits, dist);
		}
	}

	protected void hitNear() {
		int cx = (int)x;
		int cy = (int)y;

		int[][] localTexMap = state.texMap;
		int[][] localPassableMap = state.passableMap;
		int[][] localExplosivesMap = state.explosivesMap;
		Monster[][] localMonstersMap = level.monstersMap;
		Monster[][] localMonstersPrevMap = level.monstersPrevMap;

		if ((localPassableMap[cy][cx] & Level.PASSABLE_IS_EXPLOSIVE) != 0) {
			localTexMap[cy][cx] = 0;
			localPassableMap[cy][cx] = localPassableMap[cy][cx] & (Level.PASSABLE_IS_TRANSP | Level.PASSABLE_IS_NOTRANS);
			localExplosivesMap[cy][cx] = 0;
		}

		boolean alreadyHitHero = false;

		for (int yoff = -1; yoff <= 1; yoff++) {
			int ty = cy + yoff;

			for (int xoff = -1; xoff <= 1; xoff++) {
				int tx = cx + xoff;
				int pass = localPassableMap[ty][tx];

				if ((pass & Level.PASSABLE_IS_MONSTER) != 0) {
					Monster target = localMonstersMap[ty][tx];

					if (target == null) {
						target = localMonstersPrevMap[ty][tx];
					}

					if (target != null) {
						float dx = target.x - x;
						float dy = target.y - y;
						target.hit(getHits(dx, dy), hitTimeout);
					}
				} else if (((pass & Level.PASSABLE_IS_HERO) != 0) && !alreadyHitHero) {
					float dx = state.heroX - x;
					float dy = state.heroY - y;
					game.hitHero(getHits(dx, dy), mon);
					alreadyHitHero = true;
				}
			}
		}

		engine.soundManager.playSound(SoundManager.SOUND_BOOM);
	}

	protected void boomNear() {
		int cx = (int)x;
		int cy = (int)y;

		int[][] localPassableMap = state.passableMap;

		for (int yoff = -1; yoff <= 1; yoff++) {
			int ty = cy + yoff;

			for (int xoff = -1; xoff <= 1; xoff++) {
				int tx = cx + xoff;

				if ((localPassableMap[ty][tx] & Level.PASSABLE_IS_EXPLOSIVE) != 0) {
					Explosion.boom(state, (float)tx + 0.5f, (float)ty + 0.5f, mon, -1, BARREL_HITS, BARREL_HIT_TIMEOUT);
				}
			}
		}
	}
}
