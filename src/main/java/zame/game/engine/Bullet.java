package zame.game.engine;

import android.util.FloatMath;
import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Bullet extends DataListItem implements EngineObject, DataItem {
	protected static final float SIDE_OFFSET = 0.15f;
	protected static final float LOOK_AHEAD_OFFSET = 0.2f; // think of it as of bullet radius
	protected static final float SHOOT_RADIUS_SQ = 0.2f * 0.2f;
	protected static final float HIT_MIN_FLY_DIST_SQ = 0.25f * 0.25f; // must be > than SHOOT_RADIUS_SQ
	protected static final float INITIAL_ACCEL = 0.4f; // must be > than SHOOT_RADIUS

	public static class BulletParams {
		public float speed;
		public float maxDist;
		public int baseTexture;
		public boolean explosion;

		public BulletParams(float speed, float maxDist, int baseTexture, boolean explosion) {
			this.speed = speed;
			this.maxDist = maxDist;
			this.baseTexture = baseTexture;
			this.explosion = explosion;
		}
	}

	public static class HitParams {
		public static final int HIT_OUT = 1; // must be > than 0
		public static final int HIT_WALL = 2; // must be > than HIT_OUT
		public static final int HIT_EXPLOSIVE = 3; // must be > than HIT_WALL
		public static final int HIT_MONSTER = 4; // must be > than HIT_EXPLOSIVE
		public static final int HIT_HERO = 5; // must be > than HIT_MONSTER

		public int hit;
		public float x;
		public float y;
		public float tx;
		public float ty;
		public Monster target;

		public HitParams(int hit) {
			this.hit = hit;
		}
	}

	protected static final int FIELD_X = 1;
	protected static final int FIELD_Y = 2;
	protected static final int FIELD_AR = 3;
	protected static final int FIELD_MON_UID = 4;
	protected static final int FIELD_AMMO_IDX = 5;
	protected static final int FIELD_HITS = 6;
	protected static final int FIELD_HIT_TIMEOUT = 7;
	protected static final int FIELD_BULLET_STATE = 8;
	protected static final int FIELD_DIST = 9;
	protected static final int FIELD_SX = 10;
	protected static final int FIELD_SY = 11;

	protected static final BulletParams[] BULLET_PARAMS = {
		new BulletParams(50.0f, 100.0f, -1, false), // AMMO_PISTOL
		new BulletParams(50.0f, 100.0f, -1, false), // AMMO_SHOTGUN
		new BulletParams(0.5f, 100.0f, 0, true), // AMMO_ROCKET
	};

	protected static final BulletParams[] NEAR_PARAMS = {
		new BulletParams(1.4f, 1.4f, -1, false), // -1 (hero punch)
		new BulletParams(1.8f, 1.8f, -1, false), // -2 (monster punch)
	};

	public static final int STATE_INITIAL = 0;
	public static final int STATE_FLY = 1;
	public static final int STATE_RELEASE = 2;

	protected Engine engine;
	protected State state;
	protected Game game;
	protected Level level;
	protected LevelRenderer levelRenderer;
	protected float dist;
	protected float xoff;
	protected float yoff;
	protected BulletParams params;
	protected HitParams tmpHitOut = new HitParams(HitParams.HIT_OUT);
	protected HitParams tmpHitA = new HitParams(0);
	protected HitParams tmpHitB = new HitParams(0);

	public float sx;
	public float sy;
	public float x;
	public float y;
	public float ar;
	public int monUid;
	public int ammoIdx; // -1 for hero hit
	public int hits;
	public int hitTimeout;

	public float dx;
	public float dy;
	public Monster mon;
	public int bulletState;
	public int angle;

	public static boolean shootOrPunch(State state, float x, float y, float ar, Monster mon, int ammoIdx, int hits, int hitTimeout) {
		Bullet bullet = state.bullets.take();

		if (bullet == null) {
			// remove very old bullet and re-take new
			state.bullets.release(state.bullets.first());
			bullet = state.bullets.take();
		}

		bullet.init(x, y, ar, mon, ammoIdx, hits, hitTimeout);
		boolean hit = bullet.update();

		if (bullet.bulletState == STATE_RELEASE) {
			state.bullets.release(bullet);
		}

		return hit;
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
		writer.write(FIELD_AR, ar);
		writer.write(FIELD_MON_UID, monUid);
		writer.write(FIELD_AMMO_IDX, ammoIdx);
		writer.write(FIELD_HITS, hits);
		writer.write(FIELD_HIT_TIMEOUT, hitTimeout);
		writer.write(FIELD_BULLET_STATE, bulletState);
		writer.write(FIELD_DIST, dist);
		writer.write(FIELD_SX, sx);
		writer.write(FIELD_SY, sy);
	}

	public void readFrom(DataReader reader) {
		x = reader.readFloat(FIELD_X);
		y = reader.readFloat(FIELD_Y);
		ar = reader.readFloat(FIELD_AR);
		monUid = reader.readInt(FIELD_MON_UID);
		ammoIdx = reader.readInt(FIELD_AMMO_IDX);
		hits = reader.readInt(FIELD_HITS);
		hitTimeout = reader.readInt(FIELD_HIT_TIMEOUT);
		bulletState = reader.readInt(FIELD_BULLET_STATE);
		dist = reader.readFloat(FIELD_DIST);
		sx = reader.readFloat(FIELD_SX);
		sy = reader.readFloat(FIELD_SY);

		updateVars();
	}

	public void init(float x, float y, float ar, Monster mon, int ammoIdx, int hits, int hitTimeout) {
		this.sx = x;
		this.sy = y;
		this.x = x;
		this.y = y;
		this.ar = ar;
		this.mon = mon;
		this.monUid = (mon == null ? -1 : mon.uid);
		this.ammoIdx = ammoIdx;
		this.hits = hits;
		this.hitTimeout = hitTimeout;

		dist = 0.0f;
		bulletState = STATE_INITIAL;

		updateVars();
	}

	protected void updateVars() {
		angle = ((int)(ar * GameMath.RAD2G_F) + 360) % 360;
		dx = FloatMath.cos(ar);
		dy = -FloatMath.sin(ar);
		params = (ammoIdx < 0 ? NEAR_PARAMS[monUid < 0 ? 0 : 1] : BULLET_PARAMS[ammoIdx]);
		xoff = -dy * SIDE_OFFSET;
		yoff = dx * SIDE_OFFSET;
	}

	public boolean update() {
		if (bulletState > STATE_FLY) {
			return false;
		}

		level.shootSeq++;

		float accel = (bulletState == STATE_INITIAL ? Math.max(INITIAL_ACCEL, params.speed) : params.speed);
		float nx = x + dx * accel;
		float ny = y + dy * accel;
		float cx = nx + dx * LOOK_AHEAD_OFFSET;
		float cy = ny + dy * LOOK_AHEAD_OFFSET;

		HitParams hitParamsLPtr;
		HitParams hitParamsRPtr;

		if (!params.explosion) {
			hitParamsLPtr = traceBullet(x + xoff, y + yoff, cx + xoff, cy + yoff);

			if (hitParamsLPtr != null && hit(hitParamsLPtr)) {
				daBoom(true);
				return true;
			}

			hitParamsRPtr = traceBullet(x - xoff, y - yoff, cx - xoff, cy - yoff);

			if (hitParamsRPtr != null && hit(hitParamsRPtr)) {
				daBoom(true);
				return true;
			}
		} else {
			hitParamsLPtr = tmpHitOut;
			hitParamsRPtr = tmpHitOut;
		}

		HitParams hitParamsPtr = traceBullet(x, y, cx, cy);

		if (hitParamsPtr != null && hit(hitParamsPtr)) {
			daBoom(true);
			return true;
		}

		if (hitParamsPtr == null || hitParamsLPtr == null || hitParamsRPtr == null) {
			dist += accel;

			if (dist < params.maxDist - 0.01f) {
				x = nx;
				y = ny;
				bulletState = STATE_FLY;
			} else {
				daBoom(false);
			}
		} else if (hitParamsPtr.hit != HitParams.HIT_OUT) {
			x = FloatMath.floor(hitParamsPtr.x - dx * 0.5f) + 0.5f;
			y = FloatMath.floor(hitParamsPtr.y - dy * 0.5f) + 0.5f;
			daBoom(true);
		} else {
			daBoom(false);
		}

		return false;
	}

	public int getTexture() {
		if (bulletState == STATE_RELEASE || params.baseTexture < 0) {
			return -1;
		}

		return params.baseTexture + ((((int)state.heroA + 360 + 45 - angle) % 360) / 90);
	}

	protected void daBoom(boolean boom) {
		if (boom && params.explosion && ammoIdx >= 0) { // "ammoIdx >= 0" just for case
			Explosion.boom(state, x, y, mon, ammoIdx, hits, hitTimeout);
		}

		bulletState = STATE_RELEASE;
	}

	protected boolean hit(HitParams hitParams) {
		if (hitParams.hit < HitParams.HIT_EXPLOSIVE) {
			return false;
		}

		if (!params.explosion) {
			float sdx = hitParams.tx - x;
			float sdy = hitParams.ty - y;
			float dist = FloatMath.sqrt(sdx * sdx + sdy * sdy);

			if (dist > params.maxDist) {
				return false;
			}

			int resHits = engine.getRealHits(hits, dist);

			if (hitParams.hit == HitParams.HIT_MONSTER && hitParams.target != null) {
				hitParams.target.hit(resHits, hitTimeout);
			} else if (hitParams.hit == HitParams.HIT_HERO) {
				game.hitHero(resHits, mon);
			}
		}

		x = hitParams.tx - dx * 0.1f;
		y = hitParams.ty - dy * 0.1f;
		return true;
	}

	protected boolean checkHitRadius(float hx, float hy, float tx, float ty) {
		float cdx = tx - sx;
		float cdy = ty - sy;

		if ((cdx * cdx + cdy * cdy) < HIT_MIN_FLY_DIST_SQ) {
			return false;
		}

		float hdx = hx - tx;
		float hdy = hy - ty;
		float a = dx * dx + dy * dy;
		float b = 2.0f * (hdx * dx + hdy * dy);
		float c = hdx * hdx + hdy * hdy - SHOOT_RADIUS_SQ;

		// start of bullet vector is out of radius
		if (b >= 0.0f && c >= 0.0f) {
			return false;
		}

		return ((b * b - 4.0f * a * c) >= 0.0f);
	}

	// modified Level_CheckLine from wolf3d for iphone by Carmack
	protected HitParams traceBullet(float x1, float y1, float x2, float y2) {
		int cx1 = (int)x1;
		int cy1 = (int)y1;
		int maxX = state.levelWidth - 1;
		int maxY = state.levelHeight - 1;

		if (cx1 < 0 || cx1 > maxX || cy1 < 0 || cy1 > maxY) {
			return tmpHitOut;
		}

		int cx2 = (int)x2;
		int cy2 = (int)y2;
		float maxXf = (float)maxX;
		float maxYf = (float)maxY;
		int[][] localPassableMap = state.passableMap;
		Monster[][] localMonstersMap = level.monstersMap;
		Monster[][] localMonstersPrevMap = level.monstersPrevMap;
		int[][] localShootSeqMap = level.shootSeqMap;
		int localShootSeq = level.shootSeq;

		tmpHitA.hit = 0;
		tmpHitB.hit = 0;

		if (cx1 != cx2) {
			int stepX;
			float partial;

			if (cx2 > cx1) {
				partial = 1.0f - (x1 - (float)((int)x1));
				stepX = 1;
			} else {
				partial = x1 - (float)((int)x1);
				stepX = -1;
			}

			float dx = ((x2 >= x1) ? (x2 - x1) : (x1 - x2));
			float stepY = (y2 - y1) / dx;
			float y = y1 + (stepY * partial);

			cx1 += stepX;
			cx2 += stepX;

			do {
				if (cx1 < 0 || cx1 > maxX || y < 0.0f || y > maxYf) {
					tmpHitA.hit = HitParams.HIT_OUT;
					break;
				}

				int iy = (int)y;
				int pass = localPassableMap[iy][cx1];

				if ((pass & Level.PASSABLE_MASK_BULLET) != 0) {
					tmpHitA.x = (stepX > 0 ? cx1 : (float)cx1 + GameMath.ONE_MINUS_LITTLE);
					tmpHitA.y = y;

					if ((pass & Level.PASSABLE_IS_MONSTER) != 0) {
						Monster target = localMonstersMap[iy][cx1];

						if (target == null) {
							target = localMonstersPrevMap[iy][cx1];
						}

						if (target != null && checkHitRadius(tmpHitA.x, tmpHitA.y, target.x, target.y)) {
							tmpHitA.target = target;
							tmpHitA.hit = HitParams.HIT_MONSTER;
							tmpHitA.tx = target.x;
							tmpHitA.ty = target.y;
							break;
						}
					} else if ((pass & Level.PASSABLE_IS_HERO) != 0) {
						if (checkHitRadius(tmpHitA.x, tmpHitA.y, state.heroX, state.heroY)) {
							tmpHitA.hit = HitParams.HIT_HERO;
							tmpHitA.tx = state.heroX;
							tmpHitA.ty = state.heroY;
							break;
						}
					} else if ((pass & Level.PASSABLE_IS_EXPLOSIVE) != 0) {
						float expX = (float)cx1 + 0.5f;
						float expY = (float)iy + 0.5f;

						if (checkHitRadius(tmpHitA.x, tmpHitA.y, expX, expY)) {
							if (params.explosion) {
								tmpHitA.hit = HitParams.HIT_EXPLOSIVE;
								tmpHitA.tx = expX;
								tmpHitA.ty = expY;
								break;
							} else if (localShootSeqMap[iy][cx1] != localShootSeq) {
								localShootSeqMap[iy][cx1] = localShootSeq;
								Explosion.tryBoom(state, cx1, iy, hits);
								// no break
							}
						}
					} else {
						tmpHitA.hit = HitParams.HIT_WALL;
						break;
					}
				}

				y += stepY;
				cx1 += stepX;
			} while (cx1 != cx2);
		}

		if (cy1 != cy2) {
			int stepY;
			float partial;

			if (cy2 > cy1) {
				partial = 1.0f - (y1 - (float)((int)y1));
				stepY = 1;
			} else {
				partial = y1 - (float)((int)y1);
				stepY = -1;
			}

			float dy = ((y2 >= y1) ? (y2 - y1) : (y1 - y2));
			float stepX = (x2 - x1) / dy;
			float x = x1 + (stepX * partial);

			cy1 += stepY;
			cy2 += stepY;

			do {
				if (cy1 < 0 || cy1 > maxY || x < 0.0f || x > maxXf) {
					tmpHitB.hit = HitParams.HIT_OUT;
					break;
				}

				int ix = (int)x;
				int pass = localPassableMap[cy1][ix];

				if ((pass & Level.PASSABLE_MASK_BULLET) != 0) {
					tmpHitB.x = x;
					tmpHitB.y = (stepY > 0 ? cy1 : (float)cy1 + GameMath.ONE_MINUS_LITTLE);

					if ((pass & Level.PASSABLE_IS_MONSTER) != 0) {
						Monster target = localMonstersMap[cy1][ix];

						if (target == null) {
							target = localMonstersPrevMap[cy1][ix];
						}

						if (target != null && checkHitRadius(tmpHitB.x, tmpHitB.y, target.x, target.y)) {
							tmpHitB.target = target;
							tmpHitB.hit = HitParams.HIT_MONSTER;
							tmpHitB.tx = target.x;
							tmpHitB.ty = target.y;
							break;
						}
					} else if ((pass & Level.PASSABLE_IS_HERO) != 0) {
						if (checkHitRadius(tmpHitB.x, tmpHitB.y, state.heroX, state.heroY)) {
							tmpHitB.hit = HitParams.HIT_HERO;
							tmpHitB.tx = state.heroX;
							tmpHitB.ty = state.heroY;
							break;
						}
					} else if ((pass & Level.PASSABLE_IS_EXPLOSIVE) != 0) {
						float expX = (float)ix + 0.5f;
						float expY = (float)cy1 + 0.5f;

						if (checkHitRadius(tmpHitB.x, tmpHitB.y, expX, expY)) {
							if (params.explosion) {
								tmpHitB.hit = HitParams.HIT_EXPLOSIVE;
								tmpHitB.tx = expX;
								tmpHitB.ty = expY;
								break;
							} else if (localShootSeqMap[cy1][ix] != localShootSeq) {
								localShootSeqMap[cy1][ix] = localShootSeq;
								Explosion.tryBoom(state, ix, cy1, hits);
								// no break
							}
						}
					} else {
						tmpHitB.hit = HitParams.HIT_WALL;
						break;
					}
				}

				x += stepX;
				cy1 += stepY;
			} while (cy1 != cy2);
		}

		if (tmpHitA.hit == HitParams.HIT_OUT && tmpHitB.hit == HitParams.HIT_OUT) {
			return tmpHitOut;
		} else if (tmpHitA.hit < HitParams.HIT_WALL && tmpHitB.hit < HitParams.HIT_WALL) {
			if (levelRenderer.showMonstersOnMap) {
				TraceInfo.addInfo(levelRenderer, x1, y1, x2, y2, 0);
			}

			return null;
		}

		if (tmpHitA.hit > HitParams.HIT_OUT && tmpHitB.hit < HitParams.HIT_WALL) {
			if (levelRenderer.showMonstersOnMap) {
				TraceInfo.addInfo(levelRenderer, x1, y1, tmpHitA.x, tmpHitA.y, tmpHitA.hit);
			}

			return tmpHitA;
		} else if (tmpHitB.hit > HitParams.HIT_OUT && tmpHitA.hit < HitParams.HIT_WALL) {
			if (levelRenderer.showMonstersOnMap) {
				TraceInfo.addInfo(levelRenderer, x1, y1, tmpHitB.x, tmpHitB.y, tmpHitB.hit);
			}

			return tmpHitB;
		} else {
			float h1dx = tmpHitA.x - x1;
			float h1dy = tmpHitA.y - y1;
			float h2dx = tmpHitB.x - x1;
			float h2dy = tmpHitB.y - y1;

			if ((h1dx * h1dx + h1dy * h1dy) < (h2dx * h2dx + h2dy * h2dy)) {
				if (levelRenderer.showMonstersOnMap) {
					TraceInfo.addInfo(levelRenderer, x1, y1, tmpHitA.x, tmpHitA.y, tmpHitA.hit);
				}

				return tmpHitA;
			} else {
				if (levelRenderer.showMonstersOnMap) {
					TraceInfo.addInfo(levelRenderer, x1, y1, tmpHitB.x, tmpHitB.y, tmpHitB.hit);
				}

				return tmpHitB;
			}
		}
	}
}
