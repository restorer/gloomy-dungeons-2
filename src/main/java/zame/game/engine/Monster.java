package zame.game.engine;

import android.util.FloatMath;
import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;
import zame.game.managers.SoundManager;
import zame.game.store.Achievements;
import zame.game.store.Profile;

public class Monster extends DataListItem implements EngineObject, DataItem {
	protected static final int FIELD_CELL_X = 1;
	protected static final int FIELD_CELL_Y = 2;
	protected static final int FIELD_X = 3;
	protected static final int FIELD_Y = 4;
	protected static final int FIELD_TEXTURE = 5;
	protected static final int FIELD_DIR = 6;
	protected static final int FIELD_MAX_STEP = 7;
	protected static final int FIELD_HEALTH = 8;
	protected static final int FIELD_HITS = 9;
	// FIELD_VISIBLE_DIST = 10
	protected static final int FIELD_ATTACK_DIST = 11;
	// FIELD_SHOOT_SOUND_IDX = 12
	protected static final int FIELD_AMMO_IDX = 13;
	protected static final int FIELD_STEP = 14;
	protected static final int FIELD_PREV_X = 15;
	protected static final int FIELD_PREV_Y = 16;
	protected static final int FIELD_HIT_TIMEOUT = 17;
	protected static final int FIELD_ATTACK_TIMEOUT = 18;
	protected static final int FIELD_AROUND_REQ_DIR = 19;
	protected static final int FIELD_INVERSE_ROTATION = 20;
	protected static final int FIELD_PREV_AROUND_X = 21;
	protected static final int FIELD_PREV_AROUND_Y = 22;
	protected static final int FIELD_SHOOT_ANGLE = 23;
	// FIELD_HIT_HERO_TIMEOUT = 24
	protected static final int FIELD_CHASE_MODE = 26;
	protected static final int FIELD_WAIT_FOR_DOOR = 27;
	protected static final int FIELD_IN_SHOOT_MODE = 28;
	protected static final int FIELD_UID = 29;
	protected static final int FIELD_TEXMAP = 30;
	protected static final int FIELD_SHOULD_SHOOT_IN_HERO = 31;

	protected static final float VISIBLE_DIST = 32.0f;
	protected static final float HEAR_DIST = 5.0f;

	protected Engine engine;
	protected State state;
	protected Level level;
	protected Game game;
	protected LevelRenderer levelRenderer;
	protected SoundManager soundManager;
	protected Profile profile;

	public int uid; // required for save/load for bullets
	public int cellX;
	public int cellY;
	public float x;
	public float y;
	public int texture;
	public int texmap;
	public int dir; // 0 - right, 1 - up, 2 - left, 3 - down
	public int maxStep;
	public int health;
	public int hits;
	public float attackDist;
	public int attackSoundIdx;
	public int deathSoundIdx;
	public int readySoundIdx;
	public int ammoIdx;

	public int step;
	public int prevX = -1;
	public int prevY = -1;
	public int hitTimeout; // hero hits monster
	public int attackTimeout; // monster hits hero
	public long dieTime;
	public int aroundReqDir;
	public boolean inverseRotation;
	public int prevAroundX;
	public int prevAroundY;
	public int shootAngle;
	public boolean chaseMode;
	public boolean waitForDoor;
	public boolean inShootMode;
	public boolean shouldShootInHero;
	public boolean isInAttackState;
	public boolean isAimedOnHero;
	public boolean mustEnableChaseMode;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.level = engine.level;
		this.game = engine.game;
		this.levelRenderer = engine.levelRenderer;
		this.soundManager = engine.soundManager;
		this.profile = engine.profile;
	}

	public void init() {
		step = 0;
		maxStep = 50;
		hitTimeout = 0;
		attackTimeout = 0;
		dieTime = 0;
		aroundReqDir = -1;
		inverseRotation = false;
		prevAroundX = -1;
		prevAroundY = -1;
		shouldShootInHero = false;
		chaseMode = false;
		waitForDoor = false;
		inShootMode = false;
		mustEnableChaseMode = false;
	}

	public void configure(int uid, int cellX, int cellY, int monIndex, LevelConfig.MonsterConfig monConf) {
		init();

		this.uid = uid;
		this.cellX = cellX;
		this.cellY = cellY;

		dir = 0;
		prevX = -1;
		prevY = -1;
		x = (float)cellX + 0.5f;
		y = (float)cellY + 0.5f;
		texture = TextureLoader.COUNT_MONSTER * (monIndex % 4);
		texmap = (monIndex >= 4 ? 1 : 0);

		health = monConf.health;
		hits = monConf.hits;
		setAttackDist(monConf.hitType != LevelConfig.HIT_TYPE_EAT);

		if (monConf.hitType == LevelConfig.HIT_TYPE_PIST) {
			ammoIdx = Weapons.AMMO_PISTOL;
		} else if (monConf.hitType == LevelConfig.HIT_TYPE_SHTG) {
			ammoIdx = Weapons.AMMO_SHOTGUN;
		} else if (monConf.hitType == LevelConfig.HIT_TYPE_ROCKET) {
			ammoIdx = Weapons.AMMO_ROCKET;
		} else { // HIT_TYPE_EAT
			ammoIdx = -1;
		}
	}

	public void postConfigure() {
		int monIndex = (texture / TextureLoader.COUNT_MONSTER) + (texmap * 4);

		if (monIndex < 0 || monIndex > 7) {
			monIndex = 0;
		}

		attackSoundIdx = SoundManager.SOUNDLIST_ATTACK_MON[monIndex];
		deathSoundIdx = SoundManager.SOUNDLIST_DEATH_MON[monIndex];
		readySoundIdx = SoundManager.SOUNDLIST_READY_MON[monIndex];
	}

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_CELL_X, cellX);
		writer.write(FIELD_CELL_Y, cellY);
		writer.write(FIELD_X, x);
		writer.write(FIELD_Y, y);
		writer.write(FIELD_TEXTURE, texture); // do not pack texture id, because monsters use different texmap
		writer.write(FIELD_DIR, dir);
		writer.write(FIELD_MAX_STEP, maxStep);
		writer.write(FIELD_HEALTH, health);
		writer.write(FIELD_HITS, hits);
		writer.write(FIELD_ATTACK_DIST, attackDist);
		writer.write(FIELD_AMMO_IDX, ammoIdx);
		writer.write(FIELD_STEP, step);
		writer.write(FIELD_PREV_X, prevX);
		writer.write(FIELD_PREV_Y, prevY);
		writer.write(FIELD_HIT_TIMEOUT, hitTimeout);
		writer.write(FIELD_ATTACK_TIMEOUT, attackTimeout);
		writer.write(FIELD_AROUND_REQ_DIR, aroundReqDir);
		writer.write(FIELD_INVERSE_ROTATION, inverseRotation);
		writer.write(FIELD_PREV_AROUND_X, prevAroundX);
		writer.write(FIELD_PREV_AROUND_Y, prevAroundY);
		writer.write(FIELD_SHOOT_ANGLE, shootAngle);
		writer.write(FIELD_CHASE_MODE, chaseMode);
		writer.write(FIELD_WAIT_FOR_DOOR, waitForDoor);
		writer.write(FIELD_IN_SHOOT_MODE, inShootMode);
		writer.write(FIELD_UID, uid);
		writer.write(FIELD_TEXMAP, TextureLoader.packTexmap(texmap));
		writer.write(FIELD_SHOULD_SHOOT_IN_HERO, shouldShootInHero);
	}

	public void readFrom(DataReader reader) {
		cellX = reader.readInt(FIELD_CELL_X);
		cellY = reader.readInt(FIELD_CELL_Y);
		x = reader.readFloat(FIELD_X);
		y = reader.readFloat(FIELD_Y);
		texture = reader.readInt(FIELD_TEXTURE); // do not unpack texture id, because monsters use different texmap
		dir = reader.readInt(FIELD_DIR);
		maxStep = reader.readInt(FIELD_MAX_STEP);
		health = reader.readInt(FIELD_HEALTH);
		hits = reader.readInt(FIELD_HITS);
		attackDist = reader.readFloat(FIELD_ATTACK_DIST);
		ammoIdx = reader.readInt(FIELD_AMMO_IDX);
		step = reader.readInt(FIELD_STEP);
		prevX = reader.readInt(FIELD_PREV_X);
		prevY = reader.readInt(FIELD_PREV_Y);
		hitTimeout = reader.readInt(FIELD_HIT_TIMEOUT);
		attackTimeout = reader.readInt(FIELD_ATTACK_TIMEOUT);
		aroundReqDir = reader.readInt(FIELD_AROUND_REQ_DIR);
		inverseRotation = reader.readBoolean(FIELD_INVERSE_ROTATION);
		prevAroundX = reader.readInt(FIELD_PREV_AROUND_X);
		prevAroundY = reader.readInt(FIELD_PREV_AROUND_Y);
		shootAngle = reader.readInt(FIELD_SHOOT_ANGLE);
		chaseMode = reader.readBoolean(FIELD_CHASE_MODE);
		waitForDoor = reader.readBoolean(FIELD_WAIT_FOR_DOOR);
		inShootMode = reader.readBoolean(FIELD_IN_SHOOT_MODE);
		uid = reader.readInt(FIELD_UID);
		texmap = TextureLoader.unpackTexmap(reader.readInt(FIELD_TEXMAP));
		shouldShootInHero = reader.readBoolean(FIELD_SHOULD_SHOOT_IN_HERO);

		isInAttackState = false;
		isAimedOnHero = false;
		dieTime = (health <= 0 ? -1 : 0);
		mustEnableChaseMode = false;
	}

	public void setAttackDist(boolean longAttackDist) {
		attackDist = (longAttackDist ? 10.0f : 1.8f);
	}

	protected void enableChaseMode() {
		if (!chaseMode) {
			chaseMode = true;

			if (!engine.level.isInitialUpdate) {
				soundManager.playSound(readySoundIdx);
			}
		}

		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				if (dx != 0 || dy != 0) {
					Monster mon = level.monstersMap[cellY + dy][cellX + dx];

					if (mon != null) {
						mon.mustEnableChaseMode = true;
					}
				}
			}
		}
	}

	public void hit(int amt, int hitTm) {
		hitTimeout = hitTm;
		aroundReqDir = -1;
		enableChaseMode();

		if (amt <= 0) {
			return;
		}

		health -= Math.max(1, (int)((float)amt * engine.healthHitMonsterMult));

		if (health <= 0) {
			engine.soundManager.playSound(deathSoundIdx);
			state.levelExp += GameParams.EXP_KILL_MONSTER;

			state.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_MONSTER;
			state.passableMap[cellY][cellX] |= Level.PASSABLE_IS_DEAD_CORPSE;
			level.monstersMap[cellY][cellX] = null;

			if (prevX >= 0 && prevY >= 0 && prevX < state.levelWidth && prevY < state.levelHeight) {
				level.monstersPrevMap[prevY][prevX] = null;
			}

			if (ammoIdx >= 0) {
				int ammoType = Weapons.AMMO_OBJ_TEX_MAP[ammoIdx];

				if ((state.passableMap[cellY][cellX] & Level.PASSABLE_MASK_OBJECT_DROP) == 0) {
					state.objectsMap[cellY][cellX] = ammoType;
					state.passableMap[cellY][cellX] |= Level.PASSABLE_IS_OBJECT;
					levelRenderer.modLightMap(cellX, cellY, LevelRenderer.LIGHT_OBJECT);
					state.totalItems++;
				} else {
					outer: for (int dy = -1; dy <= 1; dy++) {
						for (int dx = -1; dx <= 1; dx++) {
							if (((dy != 0) || (dx != 0)) && ((state.passableMap[cellY + dy][cellX + dx] & Level.PASSABLE_MASK_OBJECT_DROP) == 0)) {
								state.objectsMap[cellY + dy][cellX + dx] = ammoType;
								state.passableMap[cellY + dy][cellX + dx] |= Level.PASSABLE_IS_OBJECT;
								levelRenderer.modLightMap(cellX + dx, cellY + dy, LevelRenderer.LIGHT_OBJECT);
								state.totalItems++;
								break outer;
							}
						}
					}
				}
			}

			state.killedMonsters++;
			Achievements.updateStat(Achievements.STAT_MONSTERS_KILLED, profile, engine, state);
		}
	}

	public void update() {
		if (health <= 0) {
			return;
		}

		if (mustEnableChaseMode && !chaseMode) {
			enableChaseMode();
		}

		float dx = 1.0f;
		float dy = 1.0f;
		float dist = 1.0f;

		if (shouldShootInHero || step == 0) {
			x = (float)cellX + 0.5f;
			y = (float)cellY + 0.5f;
			dx = state.heroX - x;
			dy = state.heroY - y;
			dist = FloatMath.sqrt(dx*dx + dy*dy);
		}

		if (shouldShootInHero) {
			shouldShootInHero = false;

			if (game.killedTime == 0) {
				soundManager.playSound(attackSoundIdx);

				Bullet.shootOrPunch(
					state,
					x, y, GameMath.getAngle(dx, dy, dist),
					this, ammoIdx,
					Math.max(1, (int)((float)hits * engine.healthHitHeroMult)),
					0
				);
			}
		}

		if (step == 0) {
			boolean tryAround = false;
			boolean vis = false;

			isInAttackState = false;
			isAimedOnHero = false;

			if (prevX >= 0 && prevY >= 0 && prevX < state.levelWidth && prevY < state.levelHeight) {
				level.monstersPrevMap[prevY][prevX] = null;
			}

			prevX = cellX;
			prevY = cellY;
			level.monstersPrevMap[prevY][prevX] = this;

			if (dist <= VISIBLE_DIST) {
				if (!chaseMode && (
					(dist <= HEAR_DIST) ||
					engine.traceLine(x, y, state.heroX, state.heroY, Level.PASSABLE_MASK_CHASE_WM)
				)) {
					enableChaseMode();
				}

				// if (chaseMode && ...) - don't additionally traceLine if hero is invisible
				if (chaseMode && engine.traceLine(x, y, state.heroX, state.heroY, Level.PASSABLE_MASK_SHOOT_WM)) {
					vis = true;
				}
			}

			state.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_MONSTER;
			level.monstersMap[cellY][cellX] = null;

			if (chaseMode) {
				if (aroundReqDir >= 0) {
					if (!waitForDoor) {
						dir = (dir + (inverseRotation ? 3 : 1)) % 4;
					}
				} else if (dist <= VISIBLE_DIST) {
					if (Math.abs(dy) <= 1.0f) {
						dir = (dx < 0 ? 2 : 0);
					} else {
						dir = (dy < 0 ? 1 : 3);
					}

					tryAround = true;
				}

				if (vis && (dist <= attackDist)) {
					int angleToHero = (int)(GameMath.getAngle(dx, dy, dist) * GameMath.RAD2G_F);
					int angleDiff = angleToHero - shootAngle;

					if (angleDiff > 180) {
						angleDiff -= 360;
					} else if (angleDiff < -180) {
						angleDiff += 360;
					}

					angleDiff = (angleDiff < 0 ? -angleDiff : angleDiff);
					shootAngle = angleToHero;

					if (!inShootMode || angleDiff > Math.max(1, 15 - (int)(dist * 3.0f))) {
						inShootMode = true;
						step = engine.random.nextInt(20) + 1;
					} else if (inShootMode) {
						isAimedOnHero = true;
						shouldShootInHero = true;

						if (ammoIdx == Weapons.AMMO_PISTOL) {
							attackTimeout = 15;
							step = 35;
						} else {
							attackTimeout = 15;
							step = 50;
						}
					}

					isInAttackState = true;
					dir = ((shootAngle + 45) % 360) / 90;
					aroundReqDir = -1;
				} else {
					inShootMode = false;
					waitForDoor = false;

					for (int i = 0; i < 4; i++) {
						switch (dir) {
							case 0:
								cellX++;
								break;

							case 1:
								cellY--;
								break;

							case 2:
								cellX--;
								break;

							case 3:
								cellY++;
								break;
						}

						if ((state.passableMap[cellY][cellX] & Level.PASSABLE_MASK_MONSTER) == 0) {
							if (dir == aroundReqDir) {
								aroundReqDir = -1;
							}

							step = maxStep;
							break;
						}

						if (chaseMode &&
							((state.passableMap[cellY][cellX] & Level.PASSABLE_IS_DOOR) != 0) &&
							((state.passableMap[cellY][cellX] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO) != 0)
						) {
							Door door = level.doorsMap[cellY][cellX];

							if (!door.sticked) {
								door.open();

								waitForDoor = true;
								cellX = prevX;
								cellY = prevY;
								step = 10;
								break;
							}
						}

						cellX = prevX;
						cellY = prevY;

						if (tryAround) {
							if ((prevAroundX == cellX) && (prevAroundY == cellY)) {
								inverseRotation = !inverseRotation;
							}

							aroundReqDir = dir;
							prevAroundX = cellX;
							prevAroundY = cellY;
							tryAround = false;
						}

						dir = (dir + (inverseRotation ? 1 : 3)) % 4;
					}

					if (step == 0) {
						step = maxStep / 2;
					}

					shootAngle = dir * 90;
				}
			}

			state.passableMap[cellY][cellX] |= Level.PASSABLE_IS_MONSTER;
			level.monstersMap[cellY][cellX] = this;
		}

		x = (float)cellX + ((float)(prevX - cellX) * (float)step / (float)maxStep) + 0.5f;
		y = (float)cellY + ((float)(prevY - cellY) * (float)step / (float)maxStep) + 0.5f;

		if (attackTimeout > 0) {
			attackTimeout--;
		}

		if (hitTimeout > 0) {
			hitTimeout--;
		} else if (step > 0) {
			step--;
		}
	}
}
