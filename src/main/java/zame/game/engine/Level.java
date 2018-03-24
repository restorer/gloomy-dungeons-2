package zame.game.engine;

import android.content.res.AssetManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import zame.game.BuildConfig;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.store.Achievements;
import zame.game.store.Profile;

public class Level implements EngineObject {
	public static final int T_HERO = 1;
	public static final int T_WALL = 2;
	public static final int T_TWALL = 3;
	public static final int T_TPASS = 4;
	public static final int T_TWIND = 5;
	public static final int T_DOOR = 6;
	public static final int T_DITEM = 7;
	public static final int T_DLAMP = 8;
	public static final int T_OBJ = 9;
	public static final int T_MON = 10;

	public static final int MAX_WIDTH = 64;
	public static final int MAX_HEIGHT = 64;
	public static final int MAX_DOORS = 128;
	public static final int MAX_MONSTERS = 256;
	public static final int MAX_MARKS = 253;
	public static final int MAX_ACTIONS = MAX_MARKS + 1;
	public static final int MAX_BULLETS = 512;
	public static final int MAX_EXPLOSIONS = 512;
	public static final int MAX_TIMEOUTS = 16;
	public static final int MAX_LOOKS = 8;

	public static final int ACTION_CLOSE = 1;
	public static final int ACTION_OPEN = 2;
	public static final int ACTION_REQ_KEY = 3;
	public static final int ACTION_SWITCH = 4;
	public static final int ACTION_NEXT_LEVEL = 5;
	public static final int ACTION_RESTORE_HEALTH = 6;
	public static final int ACTION_SECRET = 7;
	public static final int ACTION_UNMARK = 8;
	public static final int ACTION_ENSURE_WEAPON = 9;
	public static final int ACTION_MSG_ON = 10;
	public static final int ACTION_MSG_OFF = 11;
	public static final int ACTION_FLOOR = 12;
	public static final int ACTION_HELP_ON = 13;
	public static final int ACTION_HELP_OFF = 14;
	public static final int ACTION_CEIL = 15;
	public static final int ACTION_TIMEOUT = 16;
	public static final int ACTION_MSG_FLASH = 17;
	public static final int ACTION_LOOK = 18;
	public static final int ACTION_HELP_ON_ROTATE = 19;
	public static final int ACTION_WALL = 20;

	public static final int PASSABLE_IS_WALL = 1;
	public static final int PASSABLE_IS_TRANSP_WALL = 2;
	public static final int PASSABLE_IS_OBJECT = 4;
	public static final int PASSABLE_IS_DECOR_ITEM = 8;
	public static final int PASSABLE_IS_DOOR = 16;
	public static final int PASSABLE_IS_HERO = 32;
	public static final int PASSABLE_IS_MONSTER = 64;
	public static final int PASSABLE_IS_DEAD_CORPSE = 128;
	public static final int PASSABLE_IS_SECRET = 256; // additional state
	public static final int PASSABLE_IS_TRANSP = 512; // additional state, used in LevelRenderer
	public static final int PASSABLE_IS_OBJECT_KEY = 1024;
	public static final int PASSABLE_IS_DOOR_OPENED_BY_HERO = 2048; // was door opened by hero at least once?
	public static final int PASSABLE_IS_TRANSP_WINDOW = 4096; // additional state, used in LevelRenderer
	public static final int PASSABLE_IS_TRANSP_WINDOW_VERT = 8192; // additional state, used in LevelRenderer
	public static final int PASSABLE_IS_DECOR_LAMP = 16384; // additional state, used in LevelRenderer
	public static final int PASSABLE_IS_EXPLOSIVE = 32768;
	public static final int PASSABLE_IS_NOTRANS = 65536;

	public static final int PASSABLE_MASK_HERO = PASSABLE_IS_WALL
		| PASSABLE_IS_TRANSP_WALL
		| PASSABLE_IS_DECOR_ITEM
		| PASSABLE_IS_DOOR
		| PASSABLE_IS_MONSTER;

	public static final int PASSABLE_MASK_MONSTER = PASSABLE_IS_WALL
		| PASSABLE_IS_TRANSP_WALL
		| PASSABLE_IS_DECOR_ITEM
		| PASSABLE_IS_DOOR
		| PASSABLE_IS_MONSTER
		| PASSABLE_IS_HERO;

	public static final int PASSABLE_MASK_BULLET = PASSABLE_IS_WALL
		| PASSABLE_IS_DOOR
		| PASSABLE_IS_MONSTER
		| PASSABLE_IS_HERO
		| PASSABLE_IS_EXPLOSIVE;

	public static final int PASSABLE_MASK_BULLET_HERO = PASSABLE_IS_WALL | PASSABLE_IS_DOOR;
	public static final int PASSABLE_MASK_CHASE_WM = PASSABLE_IS_WALL | PASSABLE_IS_DOOR;
	public static final int PASSABLE_MASK_SHOOT_WM = PASSABLE_IS_WALL | PASSABLE_IS_DOOR | PASSABLE_IS_DECOR_ITEM | PASSABLE_IS_MONSTER;
	public static final int PASSABLE_MASK_OBJECT = PASSABLE_IS_OBJECT | PASSABLE_IS_OBJECT_KEY;
	public static final int PASSABLE_MASK_DOOR = ~PASSABLE_IS_DOOR_OPENED_BY_HERO;

	public static final int PASSABLE_MASK_OBJECT_DROP = PASSABLE_IS_WALL
		| PASSABLE_IS_TRANSP_WALL
		| PASSABLE_IS_DECOR_ITEM
		| PASSABLE_IS_DOOR
		| PASSABLE_IS_OBJECT;

	public static final int PASSABLE_MASK_WALL_N_TRANSP = PASSABLE_IS_WALL | PASSABLE_IS_TRANSP; // additional state, used in LevelRenderer
	public static final int PASSABLE_MASK_DECORATION = PASSABLE_IS_DECOR_ITEM | PASSABLE_IS_DECOR_LAMP; // additional state, used in LevelRenderer
	public static final int PASSABLE_MASK_OBJ_OR_DECOR = PASSABLE_IS_OBJECT | PASSABLE_MASK_DECORATION; // additional state, used in LevelRenderer

	protected static final int PASSABLE_MASK_ACTION_WALL = PASSABLE_IS_WALL
		| PASSABLE_IS_TRANSP_WALL
		| PASSABLE_IS_OBJECT
		| PASSABLE_IS_DECOR_ITEM
		| PASSABLE_IS_DOOR
		| PASSABLE_IS_TRANSP
		| PASSABLE_IS_OBJECT_KEY
		| PASSABLE_IS_TRANSP_WINDOW
		| PASSABLE_IS_DECOR_LAMP;

	protected Engine engine;
	protected State state;
	protected Level level;
	protected Weapons weapons;
	protected LevelRenderer levelRenderer;
	protected Profile profile;
	protected AssetManager assetManager;
	protected boolean[] wasAlreadyInWall = new boolean[9];

	public Door[][] doorsMap;
	public Mark[][] marksMap;
	public Monster[][] monstersMap;
	public Monster[][] monstersPrevMap;
	public int[][] shootSeqMap;
	public ArrayList<ArrayList<Mark>> marksHash;
	public LevelConfig conf;
	public int shootSeq;
	public boolean isInitialUpdate = false;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.level = engine.level;
		this.weapons = engine.weapons;
		this.levelRenderer = engine.levelRenderer;
		this.profile = engine.profile;
		this.assetManager = MyApplication.self.getAssets();
	}

	public void init() {
		marksHash = new ArrayList<ArrayList<Mark>>();
	}

	public boolean exists(String levelName) {
		if (levelName == null || "".equals(levelName)) {
			return false;
		}

		try {
			assetManager.open(String.format(Locale.US, "levels/%s.map", levelName)).close();
			return true;
		} catch (IOException ex) {
		}

		return false;
	}

	public void load(String levelName) {
		try {
			conf = LevelConfig.read(assetManager, levelName);
			level.create(Common.readBytes(assetManager.open(String.format(Locale.US, "levels/%s.map", levelName))));
			engine.updateAfterLevelLoadedOrCreated();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void create(byte[] data) {
		init(); // re-init level. just for case
		int pos = 1; // skip graphics set

		// skip monsters config
		while (((int)data[pos] & 0xFF) != 0) {
			pos += 4;
		}

		pos++;
		state.levelWidth = data[pos++];
		state.levelHeight = data[pos++];

		if (state.levelWidth > MAX_WIDTH || state.levelHeight > MAX_HEIGHT) {
			throw new RuntimeException("Too big level");
		}

		state.setStartValues();

		state.wallsMap = new int[state.levelHeight][state.levelWidth];
		state.texMap = new int[state.levelHeight][state.levelWidth];
		state.objectsMap = new int[state.levelHeight][state.levelWidth];
		state.passableMap = new int[state.levelHeight][state.levelWidth];
		state.floorMap1 = new int[state.levelHeight][state.levelWidth];
		state.floorMap2 = new int[state.levelHeight][state.levelWidth];
		state.floorMap3 = new int[state.levelHeight][state.levelWidth];
		state.floorMap4 = new int[state.levelHeight][state.levelWidth];
		state.ceilMap1 = new int[state.levelHeight][state.levelWidth];
		state.ceilMap2 = new int[state.levelHeight][state.levelWidth];
		state.ceilMap3 = new int[state.levelHeight][state.levelWidth];
		state.ceilMap4 = new int[state.levelHeight][state.levelWidth];
		state.arrowsMap = new int[state.levelHeight][state.levelWidth];
		state.explosivesMap = new int[state.levelHeight][state.levelWidth];

		for (int i = 0; i < state.levelHeight; i++) {
			for (int j = 0; j < state.levelWidth; j++) {
				state.wallsMap[i][j] = 0;
				state.texMap[i][j] = 0;
				state.objectsMap[i][j] = 0;
				state.passableMap[i][j] = 0;
				state.floorMap1[i][j] = 0;
				state.floorMap2[i][j] = 0;
				state.floorMap3[i][j] = 0;
				state.floorMap4[i][j] = 0;
				state.ceilMap1[i][j] = 0;
				state.ceilMap2[i][j] = 0;
				state.ceilMap3[i][j] = 0;
				state.ceilMap4[i][j] = 0;
				state.arrowsMap[i][j] = 0;
				state.explosivesMap[i][j] = 0;
			}
		}

		int doorUid = 0;
		int monsterUid = 0;

		for (int i = 0; i < state.levelHeight; i++) {
			for (int j = 0; j < state.levelWidth; j++) {
				int type = (int)data[pos++] & 0xFF;
				int value = (int)data[pos++] & 0xFF;
				int floor1 = (int)data[pos++] & 0xFF;
				int floor2 = (int)data[pos++] & 0xFF;
				int floor3 = (int)data[pos++] & 0xFF;
				int floor4 = (int)data[pos++] & 0xFF;
				int ceil1 = (int)data[pos++] & 0xFF;
				int ceil2 = (int)data[pos++] & 0xFF;
				int ceil3 = (int)data[pos++] & 0xFF;
				int ceil4 = (int)data[pos++] & 0xFF;
				int arrow = (int)data[pos++] & 0xFF;

				boolean noTrans = ((type & 128) != 0);
				boolean isVert = ((type & 64) != 0);
				type = type & 63;

				// guarantee 1-cell wall border around level
				if (
					(i == 0 || j == 0 || i == (state.levelHeight - 1) || j == (state.levelWidth - 1)) &&
					(type != T_WALL)
				) {
					type = T_WALL;
					value = 1;
				}

				if (noTrans) {
					// special invisible, non-rendererable transparent wall (to make special effects with transparents)
					state.passableMap[i][j] |= PASSABLE_IS_TRANSP | PASSABLE_IS_NOTRANS;
				}

				if (floor1 != 0) {
					state.floorMap1[i][j] = floor1 - 1 + TextureLoader.BASE_FLOOR;
				}

				if (floor2 != 0) {
					state.floorMap2[i][j] = floor2 - 1 + TextureLoader.BASE_FLOOR;
				}

				if (floor3 != 0) {
					state.floorMap3[i][j] = floor3 - 1 + TextureLoader.BASE_FLOOR;
				}

				if (floor4 != 0) {
					state.floorMap4[i][j] = floor4 - 1 + TextureLoader.BASE_FLOOR;
				}

				if (ceil1 != 0) {
					state.ceilMap1[i][j] = ceil1 - 1 + TextureLoader.BASE_CEIL;
				}

				if (ceil2 != 0) {
					state.ceilMap2[i][j] = ceil2 - 1 + TextureLoader.BASE_CEIL;
				}

				if (ceil3 != 0) {
					state.ceilMap3[i][j] = ceil3 - 1 + TextureLoader.BASE_CEIL;
				}

				if (ceil4 != 0) {
					state.ceilMap4[i][j] = ceil4 - 1 + TextureLoader.BASE_CEIL;
				}

				if (arrow != 0) {
					state.arrowsMap[i][j] = arrow - 1 + TextureLoader.BASE_ARROWS;
				}

				if (type == T_HERO) {
					state.heroX = (float)j + 0.5f;
					state.heroY = (float)i + 0.5f;
					state.setHeroA((float)(180 - value * 90));
					state.passableMap[i][j] |= PASSABLE_IS_HERO;
				} else if (type == T_WALL) {
					state.wallsMap[i][j] = value - 1 + TextureLoader.BASE_WALLS;
					state.passableMap[i][j] |= PASSABLE_IS_WALL;
				} else if (type == T_TWALL) {
					state.texMap[i][j] = value - 1 + TextureLoader.BASE_TRANSP_WALLS;
					state.passableMap[i][j] |= PASSABLE_IS_TRANSP | PASSABLE_IS_TRANSP_WALL;
				} else if (type == T_TPASS) {
					state.texMap[i][j] = value - 1 + TextureLoader.BASE_TRANSP_PASSABLE;
					state.passableMap[i][j] |= PASSABLE_IS_TRANSP;
				} else if (type == T_TWIND) {
					state.texMap[i][j] = value - 1 + TextureLoader.BASE_TRANSP_WINDOWS;
					state.passableMap[i][j] |= PASSABLE_IS_TRANSP | PASSABLE_IS_TRANSP_WALL | PASSABLE_IS_TRANSP_WINDOW;

					if (isVert) {
						state.passableMap[i][j] |= PASSABLE_IS_TRANSP_WINDOW_VERT;
					}
				} else if (type == T_DOOR) {
					Door door = state.doors.take();

					if (door == null) {
						throw new RuntimeException("Too many doors");
					}

					door.init();
					door.uid = doorUid++;
					door.x = j;
					door.y = i;
					door.texture = value - 1;
					door.vert = isVert;

					state.wallsMap[i][j] = (isVert ? -1 : -2); // mark door for PortalTracer
					state.passableMap[i][j] |= PASSABLE_IS_DOOR;
				} else if (type == T_OBJ) {
					state.objectsMap[i][j] = value - 1 + TextureLoader.BASE_OBJECTS;
					state.passableMap[i][j] |= PASSABLE_IS_OBJECT;

					if (state.objectsMap[i][j] == TextureLoader.OBJ_KEY_BLUE ||
						state.objectsMap[i][j] == TextureLoader.OBJ_KEY_RED ||
						state.objectsMap[i][j] == TextureLoader.OBJ_KEY_GREEN
					) {
						state.passableMap[i][j] |= PASSABLE_IS_OBJECT_KEY;
					}

					state.totalItems++;
				} else if (type == T_DITEM) {
					state.texMap[i][j] = value - 1 + TextureLoader.BASE_DECOR_ITEM;
					state.passableMap[i][j] |= PASSABLE_IS_DECOR_ITEM;

					if (value == 4) {
						state.passableMap[i][j] |= PASSABLE_IS_EXPLOSIVE;
						state.explosivesMap[i][j] = GameParams.HEALTH_BARREL;
					}
				} else if (type == T_DLAMP) {
					state.texMap[i][j] = value - 1 + TextureLoader.BASE_DECOR_LAMP;
					state.passableMap[i][j] |= PASSABLE_IS_DECOR_LAMP;
				} else if (type == T_MON) {
					Monster mon = state.monsters.take();

					if (mon == null) {
						throw new RuntimeException("Too many monsters");
					}

					mon.configure(monsterUid++, j, i, value - 1, conf.monsters[value - 1]);
					state.passableMap[i][j] |= PASSABLE_IS_MONSTER;
					state.totalMonsters++;
				}
			}
		}

		while (((int)data[pos] & 0xFF) != 255) {
			Mark mark = state.marks.take();

			if (mark == null) {
				throw new RuntimeException("Too many marks");
			}

			mark.id = (int)data[pos++] & 0xFF;
			mark.x = (int)data[pos++] & 0xFF;
			mark.y = (int)data[pos++] & 0xFF;
		}

		pos++;
		updateMaps();
		int secretsMask = 0;

		while (((int)data[pos] & 0xFF) != 255) {
			int mark = (int)data[pos++] & 0xFF;
			ArrayList<Action> actions = state.actions.get(mark);

			while (((int)data[pos] & 0xFF) != 0) {
				Action act = new Action();

				act.type = (int)data[pos++] & 0xFF;
				act.markId = (int)data[pos++] & 0xFF;
				act.param = (int)data[pos++] & 0xFF;

				if (act.type == ACTION_SECRET) {
					ArrayList<Mark> marks = marksHash.get(act.markId);

					for (Mark markObj : marks) {
						if (
							((state.passableMap[markObj.y][markObj.x] & PASSABLE_IS_OBJECT) != 0)
							&& ((state.passableMap[markObj.y][markObj.x] & PASSABLE_IS_SECRET) == 0)
						) {
							state.totalItems--;
						}

						state.passableMap[markObj.y][markObj.x] |= PASSABLE_IS_SECRET;
					}

					if ((secretsMask & act.param) == 0) {
						secretsMask |= act.param;
						state.totalSecrets++;
					}
				}

				actions.add(act);
			}

			pos++;
		}

		isInitialUpdate = true;

		for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
			mon.update();
		}

		executeActions(0);
		isInitialUpdate = false;
	}

	public void updateMaps() {
		for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
			door.mark = null;
		}

		doorsMap = new Door[state.levelHeight][state.levelWidth];
		marksMap = new Mark[state.levelHeight][state.levelWidth];
		monstersMap = new Monster[state.levelHeight][state.levelWidth];
		monstersPrevMap = new Monster[state.levelHeight][state.levelWidth];
		shootSeqMap = new int[state.levelHeight][state.levelWidth];
		shootSeq = 0;

		marksHash.clear();

		for (int i = 0; i <= MAX_MARKS; i++) {
			marksHash.add(new ArrayList<Mark>());
		}

		for (int i = 0; i < state.levelHeight; i++) {
			for (int j = 0; j < state.levelWidth; j++) {
				doorsMap[i][j] = null;
				marksMap[i][j] = null;
				monstersMap[i][j] = null;
				monstersPrevMap[i][j] = null;
			}
		}

		for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
			doorsMap[door.y][door.x] = door;
		}

		for (Mark mark = state.marks.first(); mark != null; mark = (Mark)mark.next) {
			marksHash.get(mark.id).add(mark);

			if (doorsMap[mark.y][mark.x] == null) {
				marksMap[mark.y][mark.x] = mark;
			} else {
				doorsMap[mark.y][mark.x].mark = mark;
			}
		}

		for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
			mon.postConfigure();
			monstersMap[mon.cellY][mon.cellX] = mon;

			if (mon.prevX >= 0 && mon.prevY >= 0 && mon.prevX < state.levelWidth && mon.prevY < state.levelHeight) {
				monstersPrevMap[mon.prevY][mon.prevX] = mon;
			}
		}
	}

	public boolean executeActions(int id) {
		ArrayList<Action> actions = state.actions.get(id);

		if (actions.isEmpty()) {
			return false;
		}

		if (State.LEVEL_INITIAL.equals(state.levelName)) {
			engine.tracker.sendEvent(Common.GA_CATEGORY, "Tutorial", "Action." + (id < 10 ? "0" : "") + String.valueOf(id), 0);

			if (BuildConfig.DEBUG) {
				Common.log("Action." + (id < 10 ? "0" : "") + String.valueOf(id));
			}
 		}

		for (Action act : actions) {
			ArrayList<Mark> marks = marksHash.get(act.markId);

			switch (act.type) {
				case ACTION_CLOSE:
				case ACTION_OPEN:
				case ACTION_REQ_KEY: {
					for (Mark mark : marks) {
						Door door = doorsMap[mark.y][mark.x];

						if (door != null) {
							door.stick(act.type == ACTION_OPEN);

							if (act.type == ACTION_OPEN) {
								Achievements.updateStat(Achievements.STAT_DOORS_OPENED, profile, engine, state);
							}

							if (act.type == ACTION_REQ_KEY) {
								door.requiredKey = act.param;
							}
						}
					}
				}
				break;

				case ACTION_UNMARK: {
					for (Mark mark : marks) {
						marksMap[mark.y][mark.x] = null;

						if (((state.passableMap[mark.y][mark.x] & PASSABLE_IS_OBJECT) != 0)
							&& ((state.passableMap[mark.y][mark.x] & PASSABLE_IS_SECRET) != 0)
						) {
							state.totalItems++;
						}

						state.passableMap[mark.y][mark.x] &= ~PASSABLE_IS_SECRET;
					}

					for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
						if (door.mark != null && door.mark.id == act.markId) {
							door.mark = null;
						}
					}

					marksHash.get(act.markId).clear();

					for (Mark mark = state.marks.first(); mark != null;) {
						Mark nextMark = (Mark)mark.next;

						if (mark.id == act.markId) {
							state.marks.release(mark);
						}

						mark = nextMark;
					}
				}
				break;

				case ACTION_WALL: {
					int resWall = 0;
					int resTex = 0;
					int resPassable = 0;

					if (act.param > 0) {
						ArrayList<Mark> argumentMarks = marksHash.get(act.param);

						if (argumentMarks != null && !argumentMarks.isEmpty()) {
							Mark argumentMark = argumentMarks.get(0);

							resWall = state.wallsMap[argumentMark.y][argumentMark.x];
							resTex = state.texMap[argumentMark.y][argumentMark.x];
							resPassable = state.passableMap[argumentMark.y][argumentMark.x] & PASSABLE_MASK_ACTION_WALL;
						}
					}

					for (Mark mark : marks) {
						if ((state.passableMap[mark.y][mark.x] & PASSABLE_IS_MONSTER) != 0) {
							for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
								if (mon.cellX == mark.x && mon.cellY == mark.y) {
									state.monsters.release(mon);
									break;
								}
							}
						}

						levelRenderer.modLightMap(mark.x, mark.y, -levelRenderer.getLightMapValue(mark.x, mark.y));
						state.wallsMap[mark.y][mark.x] = resWall;
						state.texMap[mark.y][mark.x] = resTex;
						state.passableMap[mark.y][mark.x] = state.passableMap[mark.y][mark.x] & (~PASSABLE_MASK_ACTION_WALL) | resPassable;
						levelRenderer.modLightMap(mark.x, mark.y, levelRenderer.getLightMapValue(mark.x, mark.y));

						if (level.doorsMap[mark.y][mark.x] != null) {
							state.doors.release(level.doorsMap[mark.y][mark.x]);
							level.doorsMap[mark.y][mark.x] = null;
						}
					}
				}
				break;

				case ACTION_NEXT_LEVEL:
					engine.game.nextLevel(false);
					break;

				case ACTION_RESTORE_HEALTH:
					state.heroHealth = GameParams.HEALTH_MAX;
					break;

				case ACTION_SECRET:
					if ((state.foundSecretsMask & act.param) == 0) {
						state.foundSecretsMask |= act.param;
						state.foundSecrets++;
						engine.overlay.showLabel(Labels.LABEL_SECRET_FOUND);
						state.levelExp += GameParams.EXP_SECRET_FOUND;
						Achievements.updateStat(Achievements.STAT_SECRETS_FOUND, profile, engine, state);
					}
					break;

				case ACTION_ENSURE_WEAPON:
					if (act.param > 0 && act.param < Weapons.WEAPON_LAST) {
						state.heroHasWeapon[act.param] = true;

						if (act.param == Weapons.WEAPON_PISTOL || act.param == Weapons.WEAPON_CHAINGUN || act.param == Weapons.WEAPON_DBLCHAINGUN) {
							if (state.heroAmmo[Weapons.AMMO_PISTOL] < GameParams.AMMO_ENSURED_PISTOL) {
								state.heroAmmo[Weapons.AMMO_PISTOL] = GameParams.AMMO_ENSURED_PISTOL;
							}
						} else if (act.param == Weapons.WEAPON_SHOTGUN || act.param == Weapons.WEAPON_DBLSHOTGUN) {
							if (state.heroAmmo[Weapons.AMMO_SHOTGUN] < GameParams.AMMO_ENSURED_SHOTGUN) {
								state.heroAmmo[Weapons.AMMO_SHOTGUN] = GameParams.AMMO_ENSURED_SHOTGUN;
							}
						} else if (act.param == Weapons.WEAPON_RLAUNCHER) {
							if (state.heroAmmo[Weapons.AMMO_ROCKET] < GameParams.AMMO_ENSURED_ROCKET) {
								state.heroAmmo[Weapons.AMMO_ROCKET] = GameParams.AMMO_ENSURED_ROCKET;
							}
						}

						weapons.updateWeapon();
					}
					break;

				case ACTION_MSG_ON:
					state.shownMessageId = act.param;
					break;

				case ACTION_MSG_OFF:
					state.shownMessageId = -1;
					break;

				case ACTION_FLOOR: {
					int floorTex1 = 0;
					int floorTex2 = 0;
					int floorTex3 = 0;
					int floorTex4 = 0;
					int arrowTex = 0;

					if (act.param > 0) {
						ArrayList<Mark> argumentMarks = marksHash.get(act.param);

						if (argumentMarks != null && !argumentMarks.isEmpty()) {
							Mark argumentMark = argumentMarks.get(0);

							floorTex1 = state.floorMap1[argumentMark.y][argumentMark.x];
							floorTex2 = state.floorMap2[argumentMark.y][argumentMark.x];
							floorTex3 = state.floorMap3[argumentMark.y][argumentMark.x];
							floorTex4 = state.floorMap4[argumentMark.y][argumentMark.x];
							arrowTex = state.arrowsMap[argumentMark.y][argumentMark.x];
						}
					}

					for (Mark mark : marks) {
						levelRenderer.modLightMap(mark.x, mark.y, -levelRenderer.getLightMapValue(mark.x, mark.y));

						state.floorMap1[mark.y][mark.x] = floorTex1;
						state.floorMap2[mark.y][mark.x] = floorTex2;
						state.floorMap3[mark.y][mark.x] = floorTex3;
						state.floorMap4[mark.y][mark.x] = floorTex4;
						state.arrowsMap[mark.y][mark.x] = arrowTex;

						levelRenderer.modLightMap(mark.x, mark.y, levelRenderer.getLightMapValue(mark.x, mark.y));
					}
					break;
				}

				case ACTION_HELP_ON:
					state.showHelp = true;
					state.showHelpFull = true;
					break;

				case ACTION_HELP_OFF:
					state.showHelp = false;
					break;

				case ACTION_CEIL: {
					int ceilTex1 = 0;
					int ceilTex2 = 0;
					int ceilTex3 = 0;
					int ceilTex4 = 0;

					if (act.param > 0) {
						ArrayList<Mark> argumentMarks = marksHash.get(act.param);

						if (argumentMarks != null && !argumentMarks.isEmpty()) {
							Mark argumentMark = argumentMarks.get(0);

							ceilTex1 = state.ceilMap1[argumentMark.y][argumentMark.x];
							ceilTex2 = state.ceilMap2[argumentMark.y][argumentMark.x];
							ceilTex3 = state.ceilMap3[argumentMark.y][argumentMark.x];
							ceilTex4 = state.ceilMap4[argumentMark.y][argumentMark.x];
						}
					}

					for (Mark mark : marks) {
						levelRenderer.modLightMap(mark.x, mark.y, -levelRenderer.getLightMapValue(mark.x, mark.y));

						state.ceilMap1[mark.y][mark.x] = ceilTex1;
						state.ceilMap2[mark.y][mark.x] = ceilTex2;
						state.ceilMap3[mark.y][mark.x] = ceilTex3;
						state.ceilMap4[mark.y][mark.x] = ceilTex4;

						levelRenderer.modLightMap(mark.x, mark.y, levelRenderer.getLightMapValue(mark.x, mark.y));
					}
					break;
				}

				case ACTION_TIMEOUT:
					Timeout timeout = state.timeouts.take();

					if (timeout != null) {
						timeout.markId = act.markId;
						timeout.delay = Engine.FRAMES_PER_SECOND_D10 * act.param;
					}
					break;

				case ACTION_MSG_FLASH:
					engine.overlay.showLabel(act.param);
					break;

				case ACTION_LOOK:
					for (Mark mark : marks) {
						Look look = state.looks.take();

						if (look != null) {
							look.markId = act.markId;
							look.x = mark.x;
							look.y = mark.y;
						}
					}
					break;

				case ACTION_HELP_ON_ROTATE:
					state.showHelp = true;
					state.showHelpFull = false;
					break;

				case ACTION_SWITCH: {
					int resWall = 0;
					int resPassable = 0;

					if (act.param > 0) {
						resWall = TextureLoader.BASE_WALLS + act.param - 1;
						resPassable = PASSABLE_IS_WALL;
					}

					for (Mark mark : marks) {
						if ((state.passableMap[mark.y][mark.x] & PASSABLE_IS_MONSTER) != 0) {
							for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
								if (mon.cellX == mark.x && mon.cellY == mark.y) {
									state.monsters.release(mon);
									break;
								}
							}
						}

						levelRenderer.modLightMap(mark.x, mark.y, -levelRenderer.getLightMapValue(mark.x, mark.y));
						state.texMap[mark.y][mark.x] = 0;
						state.wallsMap[mark.y][mark.x] = resWall;
						state.passableMap[mark.y][mark.x] = resPassable;
						levelRenderer.modLightMap(mark.x, mark.y, levelRenderer.getLightMapValue(mark.x, mark.y));

						if (level.doorsMap[mark.y][mark.x] != null) {
							state.doors.release(level.doorsMap[mark.y][mark.x]);
							level.doorsMap[mark.y][mark.x] = null;
						}
					}
				}
				break;
			}
		}

		return true;
	}

	public void setPassable(float x, float y, float wallDist, int mask) {
		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(state.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(state.levelHeight - 1, (int)(y + wallDist));

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				state.passableMap[j][i] |= mask;
			}
		}
	}

	public void clearPassable(float x, float y, float wallDist, int mask) {
		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(state.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(state.levelHeight - 1, (int)(y + wallDist));

		mask = ~mask;

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				state.passableMap[j][i] &= mask;
			}
		}
	}

	public void fillInitialInWallMap(float x, float y, float wallDist, int mask) {
		int fx = Math.max(0, (int)(x - wallDist));
		int cx = (int)x;
		int tx = Math.min(state.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int cy = (int)y;
		int ty = Math.min(state.levelHeight - 1, (int)(y + wallDist));

		wasAlreadyInWall[0] = ((state.passableMap[fy][fx] & mask) != 0);
		wasAlreadyInWall[1] = ((state.passableMap[cy][fx] & mask) != 0);
		wasAlreadyInWall[2] = ((state.passableMap[ty][fx] & mask) != 0);
		wasAlreadyInWall[3] = ((state.passableMap[fy][cx] & mask) != 0);
		wasAlreadyInWall[4] = ((state.passableMap[cy][cx] & mask) != 0);
		wasAlreadyInWall[5] = ((state.passableMap[ty][cx] & mask) != 0);
		wasAlreadyInWall[6] = ((state.passableMap[fy][tx] & mask) != 0);
		wasAlreadyInWall[7] = ((state.passableMap[cy][tx] & mask) != 0);
		wasAlreadyInWall[8] = ((state.passableMap[ty][tx] & mask) != 0);
	}

	// call fillInitialInWallMap before using isPassable
	public boolean isPassable(float x, float y, float wallDist, int mask) {
		// level always have 1-cell wall border, so we can skip border checks (like x>=0 && x<width)
		// but just for case limit coordinates

		int fx = Math.max(0, (int)(x - wallDist));
		int cx = (int)x;
		int tx = Math.min(state.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int cy = (int)y;
		int ty = Math.min(state.levelHeight - 1, (int)(y + wallDist));

		boolean passable = wasAlreadyInWall[0] || ((state.passableMap[fy][fx] & mask) == 0);
		passable &= wasAlreadyInWall[1] || ((state.passableMap[cy][fx] & mask) == 0);
		passable &= wasAlreadyInWall[2] || ((state.passableMap[ty][fx] & mask) == 0);
		passable &= wasAlreadyInWall[3] || ((state.passableMap[fy][cx] & mask) == 0);
		passable &= wasAlreadyInWall[4] || ((state.passableMap[cy][cx] & mask) == 0);
		passable &= wasAlreadyInWall[5] || ((state.passableMap[ty][cx] & mask) == 0);
		passable &= wasAlreadyInWall[6] || ((state.passableMap[fy][tx] & mask) == 0);
		passable &= wasAlreadyInWall[7] || ((state.passableMap[cy][tx] & mask) == 0);
		passable &= wasAlreadyInWall[8] || ((state.passableMap[ty][tx] & mask) == 0);

		return passable;
	}
}
