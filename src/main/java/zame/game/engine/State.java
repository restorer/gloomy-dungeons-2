package zame.game.engine;

import java.io.IOException;
import java.util.ArrayList;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.engine.data.DataList;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;
import zame.game.store.Achievements;

public class State extends BaseState implements EngineObject {
	public static final String LEVEL_INITIAL = "e00m00";
	public static final String LEVEL_E02M01 = "e02m01";

	protected static final int FIELD_BUILD = 1;
	protected static final int FIELD_LEVEL_NAME = 2;
	protected static final int FIELD_HERO_X = 3;
	protected static final int FIELD_HERO_Y = 4;
	protected static final int FIELD_HERO_A = 5;
	protected static final int FIELD_HERO_KEYS_MASK = 6;
	protected static final int FIELD_HERO_WEAPON = 7;
	protected static final int FIELD_HERO_HEALTH = 8;
	protected static final int FIELD_HERO_ARMOR = 9;
	protected static final int FIELD_HERO_HAS_WEAPON = 10;
	protected static final int FIELD_HERO_AMMO = 11;
	protected static final int FIELD_TOTAL_ITEMS = 12;
	protected static final int FIELD_TOTAL_MONSTERS = 13;
	protected static final int FIELD_TOTAL_SECRETS = 14;
	protected static final int FIELD_PICKED_ITEMS = 15;
	protected static final int FIELD_KILLED_MONSTERS = 16;
	protected static final int FIELD_FOUND_SECRETS = 17;
	protected static final int FIELD_FOUND_SECRETS_MASK = 18;
	protected static final int FIELD_LEVEL_WIDTH = 19;
	protected static final int FIELD_LEVEL_HEIGHT = 20;
	protected static final int FIELD_WALLS_MAP = 21;
	protected static final int FIELD_TEX_MAP = 22;
	protected static final int FIELD_OBJECTS_MAP = 23;
	protected static final int FIELD_PASSABLE_MAP = 24;
	protected static final int FIELD_DOORS = 25;
	protected static final int FIELD_MONSTERS = 26;
	protected static final int FIELD_MARKS = 27;
	protected static final int FIELD_ACTIONS = 28;
	protected static final int FIELD_DRAWED_AUTO_WALLS = 29;
	protected static final int FIELD_AUTO_WALLS = 30;
	protected static final int FIELD_SHOW_AUTO_MAP = 31;
	protected static final int FIELD_TEMP_ELAPSED_TIME = 32;
	protected static final int FIELD_TEMP_LAST_TIME = 33;
	protected static final int FIELD_GOD_MODE = 34;
	protected static final int FIELD_SHOW_EPISODE_SELECTOR = 35;
	protected static final int FIELD_HIGHLIGHTED_CONTROL_TYPE_MASK = 36;
	protected static final int FIELD_SHOWN_MESSAGE_ID = 37;
	protected static final int FIELD_HERO_VERT_A = 38;
	protected static final int FIELD_AUTO_SELECT_WEAPON = 39;
	protected static final int FIELD_AW_TOUCHED_CELLS = 40;
	protected static final int FIELD_TIME_IN_TICKS = 41;
	protected static final int FIELD_OVERALL_ITEMS = 42;
	protected static final int FIELD_OVERALL_MONSTERS = 43;
	protected static final int FIELD_OVERALL_SECRETS = 44;
	protected static final int FIELD_OVERALL_SECONDS = 45;
	protected static final int FIELD_WP_PATH_IDX = 46;
	protected static final int FIELD_BULLETS = 47;
	protected static final int FIELD_EXPLOSIONS = 48;
	protected static final int FIELD_FLOOR_MAP_1 = 49;
	protected static final int FIELD_CEIL_MAP_1 = 50;
	protected static final int FIELD_MUST_RELOAD = 51;
	protected static final int FIELD_MUST_LOAD_AUTOSAVE = 52;
	protected static final int FIELD_LEVEL_EXP = 53;
	protected static final int FIELD_FLOOR_MAP_2 = 54;
	protected static final int FIELD_FLOOR_MAP_3 = 55;
	protected static final int FIELD_FLOOR_MAP_4 = 56;
	protected static final int FIELD_CEIL_MAP_2 = 57;
	protected static final int FIELD_CEIL_MAP_3 = 58;
	protected static final int FIELD_CEIL_MAP_4 = 59;
	protected static final int FIELD_ARROWS_MAP = 60;
	protected static final int FIELD_SAVED_OR_NEW = 61;
	protected static final int FIELD_SHOW_HELP = 62;
	protected static final int FIELD_STATS = 63;
	protected static final int FIELD_EXPLOSIVES_MAP = 64;
	protected static final int FIELD_TIMEOUTS = 65;
	protected static final int FIELD_SHOW_HELP_FULL = 66;
	protected static final int FIELD_LOOKS = 67;

	protected Engine engine;

	public String levelName = "";
	public boolean showEpisodeSelector;
	public boolean mustReload;
	public boolean mustLoadAutosave;
	public boolean savedOrNew;
	public float heroX;
	public float heroY;
	public float heroA;
	public int heroKeysMask;
	public int heroWeapon;
	public int heroHealth;
	public int heroArmor;
	public boolean[] heroHasWeapon = new boolean[Weapons.WEAPON_LAST];
	public int[] heroAmmo = new int[Weapons.AMMO_LAST];

	public int totalItems;
	public int totalMonsters;
	public int totalSecrets;
	public int pickedItems;
	public int killedMonsters;
	public int foundSecrets;
	public int foundSecretsMask;
	public int timeInTicks; // divide by 40 to get seconds
	public int levelExp;

	public int overallItems;
	public int overallMonsters;
	public int overallSecrets;
	public int overallSeconds;

	public int levelWidth;
	public int levelHeight;

	public int[][] wallsMap;
	public int[][] texMap;
	public int[][] objectsMap;
	public int[][] passableMap;
	public int[][] floorMap1;
	public int[][] floorMap2;
	public int[][] floorMap3;
	public int[][] floorMap4;
	public int[][] ceilMap1;
	public int[][] ceilMap2;
	public int[][] ceilMap3;
	public int[][] ceilMap4;
	public int[][] arrowsMap;
	public int[][] explosivesMap;

	public DataList<Door> doors = new DataList<Door>(Door.class, Level.MAX_DOORS);
	public DataList<Monster> monsters = new DataList<Monster>(Monster.class, Level.MAX_MONSTERS);
	public DataList<Mark> marks = new DataList<Mark>(Mark.class, Level.MAX_MARKS);
	public DataList<Timeout> timeouts = new DataList<Timeout>(Timeout.class, Level.MAX_TIMEOUTS);
	public DataList<Look> looks = new DataList<Look>(Look.class, Level.MAX_LOOKS);

	public ArrayList<ArrayList<Action>> actions = new ArrayList<ArrayList<Action>>();

	public int[][] drawedAutoWalls;
	public DataList<AutoWall> autoWalls = new DataList<AutoWall>(AutoWall.class, LevelRenderer.MAX_AUTO_WALLS);
	public DataList<TouchedCell> awTouchedCells = new DataList<TouchedCell>(TouchedCell.class, LevelRenderer.MAX_AW_CELLS);

	public boolean showAutoMap;
	public long tempElapsedTime;
	public long tempLastTime;
	public boolean godMode;
	public int highlightedControlTypeMask;
	public int shownMessageId;
	public boolean showHelp;
	public boolean showHelpFull;

	public float heroVertA;
	public boolean autoSelectWeapon;
	public int wpPathIdx;

	public DataList<Bullet> bullets = new DataList<Bullet>(Bullet.class, Level.MAX_BULLETS);
	public DataList<Explosion> explosions = new DataList<Explosion>(Explosion.class, Level.MAX_EXPLOSIONS);

	public int[] stats = new int[Achievements.STAT_LAST];

	public void setEngine(Engine engine) {
		this.engine = engine;

		for (int i = 0; i < doors.buffer.length; i++) {
			((EngineObject)doors.buffer[i]).setEngine(engine);
		}

		for (int i = 0; i < monsters.buffer.length; i++) {
			((EngineObject)monsters.buffer[i]).setEngine(engine);
		}

		for (int i = 0; i < bullets.buffer.length; i++) {
			((EngineObject)bullets.buffer[i]).setEngine(engine);
		}

		for (int i = 0; i < explosions.buffer.length; i++) {
			((EngineObject)explosions.buffer[i]).setEngine(engine);
		}
	}

	public void setHeroA(float angle) {
		heroA = angle;
		engine.heroAngleUpdated();
	}

	public void setHeroVertA(float angle) {
		if (angle < -5.0f) {
			angle = -5.0f;
		} else if (angle > 5.0f) {
			angle = 5.0f;
		}

		heroVertA = angle;
	}

	public void init() {
		levelName = LEVEL_INITIAL;
		showEpisodeSelector = true;
		mustReload = false;
		mustLoadAutosave = false;
		savedOrNew = true;
		levelWidth = 1;
		levelHeight = 1;
		heroWeapon = 1;
		heroHealth = 100;
		heroArmor = 0;
		highlightedControlTypeMask = 0;
		shownMessageId = -1;
		showHelp = false;
		showAutoMap = false;
		godMode = false;
		overallItems = 0;
		overallMonsters = 0;
		overallSecrets = 0;
		overallSeconds = 0;

		for (int i = 0; i < Weapons.WEAPON_LAST; i++) {
			heroHasWeapon[i] = false;
		}

		for (int i = 0; i < Weapons.AMMO_LAST; i++) {
			heroAmmo[i] = 0;
		}

		for (int i = 0; i < Achievements.STAT_LAST; i++) {
			stats[i] = 0;
		}

		heroHasWeapon[Weapons.WEAPON_HAND] = true;
		heroHasWeapon[Weapons.WEAPON_PISTOL] = true;
		heroAmmo[Weapons.AMMO_PISTOL] = GameParams.AMMO_ENSURED_PISTOL;

		setStartValues();
	}

	public void setStartValues() {
		heroKeysMask = 0;
		totalItems = 0;
		totalMonsters = 0;
		totalSecrets = 0;
		pickedItems = 0;
		killedMonsters = 0;
		foundSecrets = 0;
		foundSecretsMask = 0;
		levelExp = 0;
		highlightedControlTypeMask = 0;
		shownMessageId = -1;
		showHelp = false;
		showHelpFull = false;
		heroVertA = 0.0f;
		timeInTicks = 0;
		wpPathIdx = 0;
		autoSelectWeapon = true;

		doors.clear();
		monsters.clear();
		marks.clear();
		timeouts.clear();
		looks.clear();
		actions.clear();
		autoWalls.clear();
		awTouchedCells.clear();
		bullets.clear();
		explosions.clear();

		for (int i = 0; i < Level.MAX_ACTIONS; i++) {
			actions.add(new ArrayList<Action>());
		}

		drawedAutoWalls = new int[levelHeight][levelWidth];

		for (int i = 0; i < levelHeight; i++) {
			for (int j = 0; j < levelWidth; j++) {
				drawedAutoWalls[i][j] = 0;
			}
		}
	}

	@Override
	public void writeTo(DataWriter writer) throws IOException {
		for (int i = 0; i < levelHeight; i++) {
			for (int j = 0; j < levelWidth; j++) {
				wallsMap[i][j] = TextureLoader.packTexId(wallsMap[i][j]);
				texMap[i][j] = TextureLoader.packTexId(texMap[i][j]);
				objectsMap[i][j] = TextureLoader.packTexId(objectsMap[i][j]);
				arrowsMap[i][j] = TextureLoader.packTexId(arrowsMap[i][j]);

				floorMap1[i][j] = TextureLoader.packTexId(floorMap1[i][j]);
				floorMap2[i][j] = TextureLoader.packTexId(floorMap2[i][j]);
				floorMap3[i][j] = TextureLoader.packTexId(floorMap3[i][j]);
				floorMap4[i][j] = TextureLoader.packTexId(floorMap4[i][j]);

				ceilMap1[i][j] = TextureLoader.packTexId(ceilMap1[i][j]);
				ceilMap2[i][j] = TextureLoader.packTexId(ceilMap2[i][j]);
				ceilMap3[i][j] = TextureLoader.packTexId(ceilMap3[i][j]);
				ceilMap4[i][j] = TextureLoader.packTexId(ceilMap4[i][j]);
			}
		}

		writer.write(FIELD_BUILD, MyApplication.self.getVersionName());
		writer.write(FIELD_LEVEL_NAME, levelName);
		writer.write(FIELD_HERO_X, heroX);
		writer.write(FIELD_HERO_Y, heroY);
		writer.write(FIELD_HERO_A, heroA);
		writer.write(FIELD_HERO_KEYS_MASK, heroKeysMask);
		writer.write(FIELD_HERO_WEAPON, heroWeapon);
		writer.write(FIELD_HERO_HEALTH, heroHealth);
		writer.write(FIELD_HERO_ARMOR, heroArmor);
		writer.write(FIELD_HERO_HAS_WEAPON, heroHasWeapon);
		writer.write(FIELD_HERO_AMMO, heroAmmo);
		writer.write(FIELD_TOTAL_ITEMS, totalItems);
		writer.write(FIELD_TOTAL_MONSTERS, totalMonsters);
		writer.write(FIELD_TOTAL_SECRETS, totalSecrets);
		writer.write(FIELD_PICKED_ITEMS, pickedItems);
		writer.write(FIELD_KILLED_MONSTERS, killedMonsters);
		writer.write(FIELD_FOUND_SECRETS, foundSecrets);
		writer.write(FIELD_FOUND_SECRETS_MASK, foundSecretsMask);
		writer.write(FIELD_LEVEL_WIDTH, levelWidth);
		writer.write(FIELD_LEVEL_HEIGHT, levelHeight);
		writer.write(FIELD_WALLS_MAP, wallsMap);
		writer.write(FIELD_TEX_MAP, texMap);
		writer.write(FIELD_OBJECTS_MAP, objectsMap);
		writer.write(FIELD_PASSABLE_MAP, passableMap);
		writer.write(FIELD_DOORS, doors);
		writer.write(FIELD_MONSTERS, monsters);
		writer.write(FIELD_MARKS, marks);
		writer.writeList2d(FIELD_ACTIONS, actions);
		writer.write(FIELD_DRAWED_AUTO_WALLS, drawedAutoWalls);
		writer.write(FIELD_AUTO_WALLS, autoWalls);
		writer.write(FIELD_SHOW_AUTO_MAP, showAutoMap);
		writer.write(FIELD_TEMP_ELAPSED_TIME, tempElapsedTime);
		writer.write(FIELD_TEMP_LAST_TIME, tempLastTime);
		writer.write(FIELD_GOD_MODE, godMode);
		writer.write(FIELD_SHOW_EPISODE_SELECTOR, showEpisodeSelector);
		writer.write(FIELD_HIGHLIGHTED_CONTROL_TYPE_MASK, highlightedControlTypeMask);
		writer.write(FIELD_SHOWN_MESSAGE_ID, shownMessageId);
		writer.write(FIELD_HERO_VERT_A, heroVertA);
		writer.write(FIELD_AUTO_SELECT_WEAPON, autoSelectWeapon);
		writer.write(FIELD_AW_TOUCHED_CELLS, awTouchedCells);
		writer.write(FIELD_TIME_IN_TICKS, timeInTicks);
		writer.write(FIELD_OVERALL_ITEMS, overallItems);
		writer.write(FIELD_OVERALL_MONSTERS, overallMonsters);
		writer.write(FIELD_OVERALL_SECRETS, overallSecrets);
		writer.write(FIELD_OVERALL_SECONDS, overallSeconds);
		writer.write(FIELD_WP_PATH_IDX, wpPathIdx);
		writer.write(FIELD_BULLETS, bullets);
		writer.write(FIELD_EXPLOSIONS, explosions);
		writer.write(FIELD_FLOOR_MAP_1, floorMap1);
		writer.write(FIELD_FLOOR_MAP_2, floorMap2);
		writer.write(FIELD_FLOOR_MAP_3, floorMap3);
		writer.write(FIELD_FLOOR_MAP_4, floorMap4);
		writer.write(FIELD_CEIL_MAP_1, ceilMap1);
		writer.write(FIELD_CEIL_MAP_2, ceilMap2);
		writer.write(FIELD_CEIL_MAP_3, ceilMap3);
		writer.write(FIELD_CEIL_MAP_4, ceilMap4);
		writer.write(FIELD_ARROWS_MAP, arrowsMap);
		writer.write(FIELD_MUST_RELOAD, mustReload);
		writer.write(FIELD_MUST_LOAD_AUTOSAVE, mustLoadAutosave);
		writer.write(FIELD_LEVEL_EXP, levelExp);
		writer.write(FIELD_SAVED_OR_NEW, savedOrNew);
		writer.write(FIELD_SHOW_HELP, showHelp);
		writer.write(FIELD_STATS, stats);
		writer.write(FIELD_EXPLOSIVES_MAP, explosivesMap);
		writer.write(FIELD_TIMEOUTS, timeouts);
		writer.write(FIELD_SHOW_HELP_FULL, showHelpFull);
		writer.write(FIELD_LOOKS, looks);

		for (int i = 0; i < levelHeight; i++) {
			for (int j = 0; j < levelWidth; j++) {
				wallsMap[i][j] = TextureLoader.unpackTexId(wallsMap[i][j]);
				texMap[i][j] = TextureLoader.unpackTexId(texMap[i][j]);
				objectsMap[i][j] = TextureLoader.unpackTexId(objectsMap[i][j]);
				arrowsMap[i][j] = TextureLoader.unpackTexId(arrowsMap[i][j]);

				floorMap1[i][j] = TextureLoader.unpackTexId(floorMap1[i][j]);
				floorMap2[i][j] = TextureLoader.unpackTexId(floorMap2[i][j]);
				floorMap3[i][j] = TextureLoader.unpackTexId(floorMap3[i][j]);
				floorMap4[i][j] = TextureLoader.unpackTexId(floorMap4[i][j]);

				ceilMap1[i][j] = TextureLoader.unpackTexId(ceilMap1[i][j]);
				ceilMap2[i][j] = TextureLoader.unpackTexId(ceilMap2[i][j]);
				ceilMap3[i][j] = TextureLoader.unpackTexId(ceilMap3[i][j]);
				ceilMap4[i][j] = TextureLoader.unpackTexId(ceilMap4[i][j]);
			}
		}
	}

	@Override
	public void readFrom(DataReader reader) {
		levelName = reader.readString(FIELD_LEVEL_NAME, LEVEL_INITIAL);
		heroX = reader.readFloat(FIELD_HERO_X);
		heroY = reader.readFloat(FIELD_HERO_Y);
		setHeroA(reader.readFloat(FIELD_HERO_A));
		heroKeysMask = reader.readInt(FIELD_HERO_KEYS_MASK);
		heroWeapon = reader.readInt(FIELD_HERO_WEAPON);
		heroHealth = reader.readInt(FIELD_HERO_HEALTH);
		heroArmor = reader.readInt(FIELD_HERO_ARMOR);
		heroHasWeapon = reader.readBooleanArray(FIELD_HERO_HAS_WEAPON, Weapons.WEAPON_LAST);
		heroAmmo = reader.readIntArray(FIELD_HERO_AMMO, Weapons.AMMO_LAST);
		totalItems = reader.readInt(FIELD_TOTAL_ITEMS);
		totalMonsters = reader.readInt(FIELD_TOTAL_MONSTERS);
		totalSecrets = reader.readInt(FIELD_TOTAL_SECRETS);
		pickedItems = reader.readInt(FIELD_PICKED_ITEMS);
		killedMonsters = reader.readInt(FIELD_KILLED_MONSTERS);
		foundSecrets = reader.readInt(FIELD_FOUND_SECRETS);
		foundSecretsMask = reader.readInt(FIELD_FOUND_SECRETS_MASK);
		levelWidth = reader.readInt(FIELD_LEVEL_WIDTH);
		levelHeight = reader.readInt(FIELD_LEVEL_HEIGHT);
		wallsMap = reader.readInt2dArray(FIELD_WALLS_MAP, levelHeight, levelWidth);
		texMap = reader.readInt2dArray(FIELD_TEX_MAP, levelHeight, levelWidth);
		objectsMap = reader.readInt2dArray(FIELD_OBJECTS_MAP, levelHeight, levelWidth);
		passableMap = reader.readInt2dArray(FIELD_PASSABLE_MAP, levelHeight, levelWidth);
		reader.readDataList(FIELD_DOORS, doors);
		reader.readDataList(FIELD_MONSTERS, monsters);
		reader.readDataList(FIELD_MARKS, marks);
		reader.readList2d(FIELD_ACTIONS, actions, Action.class);
		drawedAutoWalls = reader.readInt2dArray(FIELD_DRAWED_AUTO_WALLS, levelHeight, levelWidth);
		reader.readDataList(FIELD_AUTO_WALLS, autoWalls);
		showAutoMap = reader.readBoolean(FIELD_SHOW_AUTO_MAP);
		tempElapsedTime = reader.readLong(FIELD_TEMP_ELAPSED_TIME);
		tempLastTime = reader.readLong(FIELD_TEMP_LAST_TIME);
		godMode = reader.readBoolean(FIELD_GOD_MODE);
		showEpisodeSelector = reader.readBoolean(FIELD_SHOW_EPISODE_SELECTOR, true);
		highlightedControlTypeMask = reader.readInt(FIELD_HIGHLIGHTED_CONTROL_TYPE_MASK);
		shownMessageId = reader.readInt(FIELD_SHOWN_MESSAGE_ID);
		heroVertA = reader.readFloat(FIELD_HERO_VERT_A);
		autoSelectWeapon = reader.readBoolean(FIELD_AUTO_SELECT_WEAPON);
		reader.readDataList(FIELD_AW_TOUCHED_CELLS, awTouchedCells);
		timeInTicks = reader.readInt(FIELD_TIME_IN_TICKS);
		overallItems = reader.readInt(FIELD_OVERALL_ITEMS);
		overallMonsters = reader.readInt(FIELD_OVERALL_MONSTERS);
		overallSecrets = reader.readInt(FIELD_OVERALL_SECRETS);
		overallSeconds = reader.readInt(FIELD_OVERALL_SECONDS);
		wpPathIdx = reader.readInt(FIELD_WP_PATH_IDX);
		reader.readDataList(FIELD_BULLETS, bullets);
		reader.readDataList(FIELD_EXPLOSIONS, explosions);
		floorMap1 = reader.readInt2dArray(FIELD_FLOOR_MAP_1, levelHeight, levelWidth);
		floorMap2 = reader.readInt2dArray(FIELD_FLOOR_MAP_2, levelHeight, levelWidth);
		floorMap3 = reader.readInt2dArray(FIELD_FLOOR_MAP_3, levelHeight, levelWidth);
		floorMap4 = reader.readInt2dArray(FIELD_FLOOR_MAP_4, levelHeight, levelWidth);
		ceilMap1 = reader.readInt2dArray(FIELD_CEIL_MAP_1, levelHeight, levelWidth);
		ceilMap2 = reader.readInt2dArray(FIELD_CEIL_MAP_2, levelHeight, levelWidth);
		ceilMap3 = reader.readInt2dArray(FIELD_CEIL_MAP_3, levelHeight, levelWidth);
		ceilMap4 = reader.readInt2dArray(FIELD_CEIL_MAP_4, levelHeight, levelWidth);
		arrowsMap = reader.readInt2dArray(FIELD_ARROWS_MAP, levelHeight, levelWidth);
		mustReload = reader.readBoolean(FIELD_MUST_RELOAD);
		mustLoadAutosave = reader.readBoolean(FIELD_MUST_LOAD_AUTOSAVE);
		levelExp = reader.readInt(FIELD_LEVEL_EXP);
		savedOrNew = reader.readBoolean(FIELD_SAVED_OR_NEW);
		showHelp = reader.readBoolean(FIELD_SHOW_HELP);
		stats = reader.readIntArray(FIELD_STATS, Achievements.STAT_LAST);
		explosivesMap = reader.readInt2dArray(FIELD_EXPLOSIVES_MAP, levelHeight, levelWidth);
		reader.readDataList(FIELD_TIMEOUTS, timeouts);
		showHelpFull = reader.readBoolean(FIELD_SHOW_HELP_FULL);
		reader.readDataList(FIELD_LOOKS, looks);

		for (int i = 0; i < levelHeight; i++) {
			for (int j = 0; j < levelWidth; j++) {
				wallsMap[i][j] = TextureLoader.unpackTexId(wallsMap[i][j]);
				texMap[i][j] = TextureLoader.unpackTexId(texMap[i][j]);
				objectsMap[i][j] = TextureLoader.unpackTexId(objectsMap[i][j]);
				arrowsMap[i][j] = TextureLoader.unpackTexId(arrowsMap[i][j]);

				floorMap1[i][j] = TextureLoader.unpackTexId(floorMap1[i][j]);
				floorMap2[i][j] = TextureLoader.unpackTexId(floorMap2[i][j]);
				floorMap3[i][j] = TextureLoader.unpackTexId(floorMap3[i][j]);
				floorMap4[i][j] = TextureLoader.unpackTexId(floorMap4[i][j]);

				ceilMap1[i][j] = TextureLoader.unpackTexId(ceilMap1[i][j]);
				ceilMap2[i][j] = TextureLoader.unpackTexId(ceilMap2[i][j]);
				ceilMap3[i][j] = TextureLoader.unpackTexId(ceilMap3[i][j]);
				ceilMap4[i][j] = TextureLoader.unpackTexId(ceilMap4[i][j]);

				if (((passableMap[i][j] & Level.PASSABLE_IS_EXPLOSIVE) != 0) && (explosivesMap[i][j] == 0)) {
					explosivesMap[i][j] = GameParams.HEALTH_BARREL;
				}
			}
		}

		engine.level.conf = LevelConfig.read(MyApplication.self.getAssets(), levelName);

		// Fix monster texture from older savefile versions

		for (Monster mon = monsters.first(); mon != null; mon = (Monster)mon.next) {
			if ((mon.texture & 0xF0000) != 0) {
				if (mon.ammoIdx == Weapons.AMMO_PISTOL) { // Soldier or Robotic Spider
					mon.texture = TextureLoader.COUNT_MONSTER * 0;
				} else if (mon.ammoIdx == Weapons.AMMO_SHOTGUN) { // Officer or Shotyfly
					mon.texture = TextureLoader.COUNT_MONSTER * 1;
				} else if (mon.ammoIdx == Weapons.AMMO_ROCKET) { // Rocketto
					mon.texture = TextureLoader.COUNT_MONSTER * 3;
				} else if (mon.hits == engine.level.conf.monsters[3].hits) { // Incubus
					mon.texture = TextureLoader.COUNT_MONSTER * 3;
				} else { // Underground Snake or Flying jaws
					mon.texture = TextureLoader.COUNT_MONSTER * 2;
				}
			}
		}
	}

	@Override
	protected int getVersion() {
		return 7;
	}

	@Override
	protected void versionUpgrade(int version) {
		if (version <= 4) {
			if (heroWeapon == 5) {
				heroWeapon = 6;
			} else if (heroWeapon == 6) {
				heroWeapon = 7;
			} else if (heroWeapon == 7) {
				heroWeapon = 5;
			}

			boolean tmp5 = heroHasWeapon[5];
			boolean tmp6 = heroHasWeapon[6];
			boolean tmp7 = heroHasWeapon[7];

			heroHasWeapon[5] = tmp7;
			heroHasWeapon[6] = tmp5;
			heroHasWeapon[7] = tmp6;
		}

		if (version <= 5) {
			for (int i = 0; i < levelHeight; i++) {
				for (int j = 0; j < levelWidth; j++) {
					if (floorMap1[i][j] > 0) { floorMap1[i][j] += 5; }
					if (floorMap2[i][j] > 0) { floorMap2[i][j] += 5; }
					if (floorMap3[i][j] > 0) { floorMap3[i][j] += 5; }
					if (floorMap4[i][j] > 0) { floorMap4[i][j] += 5; }

					if (ceilMap1[i][j] > 0) { ceilMap1[i][j] += 9; }
					if (ceilMap2[i][j] > 0) { ceilMap2[i][j] += 9; }
					if (ceilMap3[i][j] > 0) { ceilMap3[i][j] += 9; }
					if (ceilMap4[i][j] > 0) { ceilMap4[i][j] += 9; }

					if (arrowsMap[i][j] > 0) { arrowsMap[i][j] += 9; }
				}
			}
		}

		if (version <= 6) {
			for (int i = 0; i < levelHeight; i++) {
				for (int j = 0; j < levelWidth; j++) {
					if (wallsMap[i][j] > 0) {
						wallsMap[i][j] += 65;
					}
				}
			}

			for (ArrayList<Action> actionsList : actions) {
				for (Action act : actionsList) {
					if (act.type == Level.ACTION_SWITCH && act.param > 0) {
						act.param += 65;
					}
				}
			}
		}
	}

	@Override
	public boolean save(String name) {
		if (levelExp != 0) {
			engine.profile.exp += levelExp;
			engine.profile.save();
			levelExp = 0;
		}

		if (!super.save(engine.getSavePathBySaveName(name))) {
			Common.showToast(R.string.msg_cant_save_state);
			return false;
		}

		return true;
	}

	@Override
	public int load(String name) {
		int result = super.load(engine.getSavePathBySaveName(name));

		if (result == LOAD_RESULT_ERROR) {
			Common.showToast(R.string.msg_cant_load_state);
		}

		return result;
	}
}
