package zame.game.engine;

import android.util.FloatMath;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.managers.SoundManager;
import zame.game.providers.UpdateLeaderboardProvider;
import zame.game.store.Achievements;
import zame.game.store.Profile;
import zame.game.store.Store;

public class Game implements EngineObject {
	public static final int RENDER_MODE_GAME = 1;
	public static final int RENDER_MODE_END_LEVEL = 2;
	public static final int RENDER_MODE_GAME_OVER = 4;
	public static final int RENDER_MODE_ALL = RENDER_MODE_GAME | RENDER_MODE_END_LEVEL | RENDER_MODE_GAME_OVER;

	public static final float WALK_WALL_DIST = 0.2f;
	public static final float USE_IGNORE_THRESHOLD = 0.5f;
	public static final int LOAD_LEVEL_NORMAL = 1;
	public static final int LOAD_LEVEL_NEXT = 2;
	public static final int LOAD_LEVEL_RELOAD = 3;
	public static final int LOAD_LEVEL_JUST_NEXT_NAME = 4;
	public static final float CROSSHAIR_SIZE = 0.005f;

	protected Engine engine;
	protected Config config;
	protected State state;
	protected Weapons weapons;
	protected Labels labels;
	protected Overlay overlay;
	protected Level level;
	protected LevelRenderer levelRenderer;
	protected SoundManager soundManager;
	protected Renderer renderer;
	protected HeroController heroController;
	protected Profile profile;
	protected EndLevel endLevel;
	protected GameOver gameOver;

	protected long prevMovedTime = 0;
	protected long nextLevelTime;
	protected float killedAngle;
	protected float killedHeroAngle;
	protected boolean isGameOverFlag;
	protected boolean playStartLevelSound;
	protected boolean skipEndLevelScreenOnce;
	protected int heroCellX = 0;
	protected int heroCellY = 0;
	protected float prevUseX = -1.0f;
	protected float prevUseY = -1.0f;
	protected int maxHealth;
	protected int maxArmor;
	protected int maxAmmoPistol;
	protected int maxAmmoShotgun;
	protected int maxAmmoRocket;
	protected long firstTouchTime;

	public long killedTime;
	public int actionFire = 0;
	public boolean actionNextWeapon = false;
	public boolean actionToggleMap = false;
	public boolean actionGameMenu = false;
	public boolean actionUpgradeButton = false;
	public boolean hasMoved = false;
	public String savedGameParam = "";
	public String unprocessedGameCode = "";
	public int renderMode;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.config = engine.config;
		this.state = engine.state;
		this.weapons = engine.weapons;
		this.labels = engine.labels;
		this.overlay = engine.overlay;
		this.level = engine.level;
		this.levelRenderer = engine.levelRenderer;
		this.soundManager = engine.soundManager;
		this.renderer = engine.renderer;
		this.heroController = engine.heroController;
		this.profile = engine.profile;
		this.endLevel = engine.endLevel;
		this.gameOver = engine.gameOver;
	}

	protected void setStartValues() {
		nextLevelTime = 0;
		killedTime = 0;
		isGameOverFlag = false;
		playStartLevelSound = false;
		skipEndLevelScreenOnce = false;
		firstTouchTime = -1;
	}

	public void init() {
		setStartValues();
		renderMode = RENDER_MODE_GAME;
		soundManager.setPlaylist(SoundManager.LIST_MAIN);

		labels.init();
		overlay.init();
		state.init();
		level.init();
		weapons.init();

		if (savedGameParam.length() == 0) {
			loadLevel(LOAD_LEVEL_NORMAL);
			playStartLevelSound = true;

			if (State.LEVEL_INITIAL.equals(state.levelName)) {
				engine.tracker.sendEventAndFlush(Common.GA_CATEGORY, "JustStarted", "", 0);
			}
		} else {
			int result = state.load(savedGameParam);

			if (result == State.LOAD_RESULT_SUCCESS && !state.mustReload && !state.mustLoadAutosave) {
				engine.updateAfterLevelLoadedOrCreated();

				if (!savedGameParam.equals(engine.instantName)) {
					engine.createAutosave();
				}
			} else {
				loadLevel(LOAD_LEVEL_NORMAL);
				playStartLevelSound = true;
			}
		}

		savedGameParam = engine.instantName;
		updatePurchases();
	}

	protected void updatePurchases() {
		maxHealth = GameParams.HEALTH_MAX + (profile.isPurchased(Store.ADDITIONAL_HEALTH) ? GameParams.HEALTH_ADDITIONAL : 0);
		maxArmor = GameParams.ARMOR_MAX + (profile.isPurchased(Store.ADDITIONAL_ARMOR) ? GameParams.ARMOR_ADDITIONAL : 0);
		maxAmmoPistol = GameParams.AMMO_MAX_PISTOL + (profile.isPurchased(Store.ADDITIONAL_AMMO) ? GameParams.AMMO_ADDITIONAL_PISTOL : 0);
		maxAmmoShotgun = GameParams.AMMO_MAX_SHOTGUN + (profile.isPurchased(Store.ADDITIONAL_AMMO) ? GameParams.AMMO_ADDITIONAL_SHOTGUN : 0);
		maxAmmoRocket = GameParams.AMMO_MAX_ROCKET + (profile.isPurchased(Store.ADDITIONAL_AMMO) ? GameParams.AMMO_ADDITIONAL_ROCKET : 0);

		if (profile.isPurchased(Store.DBL_CHAINGUN)) {
			state.heroHasWeapon[Weapons.WEAPON_DBLCHAINGUN] = true;

			if (state.heroAmmo[Weapons.AMMO_PISTOL] < GameParams.AMMO_ENSURED_PISTOL) {
				state.heroAmmo[Weapons.AMMO_PISTOL] = GameParams.AMMO_ENSURED_PISTOL;
			}
		}

		if (profile.isPurchased(Store.DBL_PISTOL)) {
			state.heroHasWeapon[Weapons.WEAPON_DBLPISTOL] = true;

			if (state.heroAmmo[Weapons.AMMO_PISTOL] < GameParams.AMMO_ENSURED_PISTOL) {
				state.heroAmmo[Weapons.AMMO_PISTOL] = GameParams.AMMO_ENSURED_PISTOL;
			}
		}

		if (profile.isPurchased(Store.PDBL_SHOTGUN)) {
			state.heroHasWeapon[Weapons.WEAPON_PDBLSHOTGUN] = true;

			if (state.heroAmmo[Weapons.AMMO_SHOTGUN] < GameParams.AMMO_ENSURED_SHOTGUN) {
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = GameParams.AMMO_ENSURED_SHOTGUN;
			}
		}

		if (profile.isPurchased(Store.ALWAYS_OPEN_MAP) && profile.products[Store.ALWAYS_OPEN_MAP].value != 0) {
			levelRenderer.openAllMap();
		}

		if (profile.isPurchased(Store.SECRETS)) {
			levelRenderer.openSecrets();
		}

		if (profile.isPurchased(Store.INITIAL_ARMOR)) {
			state.heroArmor = Math.max(GameParams.ARMOR_ADD_RED, state.heroArmor);
		}
	}

	public void processGameCode(String codes) {
		// this function is called from another thread,
		// when reloading level rendering thread can fail
		// so here is self-made lock
		// why not to use normal lock? because in rare cases it causes ANR
		// (due to multithreading)
		engine.renderBlackScreen = true;

		String[] codeList = codes.toLowerCase().split(" ");

		if (codeList.length > 0) {
			Achievements.deepResetStat(Achievements.STAT_QUICKY, profile, engine, state);
		}

		for (String code : codeList) {
			if (code.length() < 2) {
				continue;
			}

			if (code.equals("iwda")) {
				state.heroHasWeapon[Weapons.WEAPON_PISTOL] = true;
				state.heroHasWeapon[Weapons.WEAPON_SHOTGUN] = true;
				state.heroHasWeapon[Weapons.WEAPON_CHAINGUN] = true;
				state.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] = true;
				state.heroHasWeapon[Weapons.WEAPON_DBLCHAINGUN] = true;
				state.heroHasWeapon[Weapons.WEAPON_CHAINSAW] = true;
				state.heroHasWeapon[Weapons.WEAPON_RLAUNCHER] = true;

				state.heroAmmo[Weapons.AMMO_PISTOL] = maxAmmoPistol;
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = maxAmmoShotgun;
				state.heroAmmo[Weapons.AMMO_ROCKET] = maxAmmoRocket;
			} else if (code.equals("iwdh")) {
				state.heroHealth = maxHealth;
				state.heroArmor = maxArmor;
			} else if (code.equals("iwdk")) {
				state.heroKeysMask = 7;
			} else if (code.equals("scli")) {
				loadLevel(LOAD_LEVEL_NEXT);
			} else if (code.equals("sfps")) {
				engine.showFps = !engine.showFps;
			} else if (code.equals("smon")) {
				levelRenderer.showMonstersOnMap = !levelRenderer.showMonstersOnMap;
			} else if (code.equals("iwgm")) {
				state.godMode = !state.godMode;
			} else if (code.equals("iddqd")) {
				state.godMode = false;
				state.heroHealth = 1;
				state.heroArmor = 0;
			} else if (code.equals("iwdm")) {
				levelRenderer.openAllMap();
			} else if (code.equals("slfb")) {
				loadLevel(LOAD_LEVEL_RELOAD);
			} else if (code.startsWith("sl")) {
				String newLevelName = code.substring(2);

				if (level.exists(newLevelName)) {
					state.levelName = newLevelName;
					loadLevel(LOAD_LEVEL_RELOAD);
				}
			}
		}

		if (engine.gameViewActive) {
			engine.renderBlackScreen = false;
		}
	}

	public void loadLevel(int loadLevelType) {
		if (loadLevelType == LOAD_LEVEL_NEXT || loadLevelType == LOAD_LEVEL_JUST_NEXT_NAME) {
			String nextLevelName = profile.getLevel(state.levelName).getNextLevelName();

			if (!level.exists(nextLevelName)) {
				return;
			}

			state.levelName = nextLevelName;
			state.showEpisodeSelector = true;
		}

		if (loadLevelType != LOAD_LEVEL_JUST_NEXT_NAME) {
			setStartValues();
			state.mustReload = false;

			if (state.mustLoadAutosave) {
				if (state.load(engine.autosaveName) == State.LOAD_RESULT_SUCCESS) {
					state.showEpisodeSelector = false;
					engine.updateAfterLevelLoadedOrCreated();
				} else {
					level.load(state.levelName);
					state.heroWeapon = weapons.getBestWeapon();
				}

				updatePurchases();
				renderMode = RENDER_MODE_GAME;
			} else {
				level.load(state.levelName);
				state.heroWeapon = weapons.getBestWeapon();
				engine.createAutosave();
			}
		} else {
			state.mustReload = true;
		}

		state.mustLoadAutosave = false;

		if (loadLevelType == LOAD_LEVEL_NEXT || loadLevelType == LOAD_LEVEL_RELOAD) {
			engine.changeView(Engine.VIEW_TYPE_SELECT_EPISODE);
		}
	}

	protected void showGameOverScreen() {
		if (engine.inWallpaperMode) {
			loadLevel(LOAD_LEVEL_NEXT);
			return;
		}

		state.mustLoadAutosave = true;
		renderMode = RENDER_MODE_GAME_OVER;
		engine.tracker.sendEventAndFlush(Common.GA_CATEGORY, "GameOver", state.levelName, 0);
	}

	protected void showEndLevelScreen() {
		if (skipEndLevelScreenOnce || engine.inWallpaperMode) {
			skipEndLevelScreenOnce = false;
			loadLevel(LOAD_LEVEL_NEXT);
			return;
		}

		engine.tracker.sendEventAndFlush(Common.GA_CATEGORY, "LevelCompleted", state.levelName, 0);

		loadLevel(LOAD_LEVEL_JUST_NEXT_NAME);
		renderMode = RENDER_MODE_END_LEVEL;

		if (state.pickedItems > state.totalItems) {
			state.pickedItems = state.totalItems;
			Common.log("Game.showEndLevelScreen: state.pickedItems > state.totalItems");
		}

		state.overallItems += state.pickedItems;
		state.overallMonsters += state.killedMonsters;
		state.overallSecrets += state.foundSecrets;
		state.overallSeconds += state.timeInTicks / Engine.FRAMES_PER_SECOND;

		profile.autoSaveOnUpdate = false;
		profile.exp += state.levelExp;

		if (!profile.alreadyCompletedLevels.contains(state.levelName)) {
			profile.exp += GameParams.EXP_END_LEVEL;
			profile.alreadyCompletedLevels.add(state.levelName);
		}

		profile.update();
		state.levelExp = 0;

		if (state.totalMonsters != 0 && state.killedMonsters == state.totalMonsters) {
			Achievements.updateStat(Achievements.STAT_P100_KILLS_ROW, profile, engine, state);
		} else {
			Achievements.resetStat(Achievements.STAT_P100_KILLS_ROW, profile, engine, state);
		}

		if (state.totalItems != 0 && state.pickedItems == state.totalItems) {
			Achievements.updateStat(Achievements.STAT_P100_ITEMS_ROW, profile, engine, state);
		} else {
			Achievements.resetStat(Achievements.STAT_P100_ITEMS_ROW, profile, engine, state);
		}

		if (state.totalSecrets != 0 && state.foundSecrets == state.totalSecrets) {
			Achievements.updateStat(Achievements.STAT_P100_SECRETS_ROW, profile, engine, state);
		} else {
			Achievements.resetStat(Achievements.STAT_P100_SECRETS_ROW, profile, engine, state);
		}

		if (State.LEVEL_E02M01.equals(state.levelName) && (state.overallSeconds < (4 * 60 + 30))) {
			Achievements.updateStat(Achievements.STAT_QUICKY, profile, engine, state);
		}

		if (profile.isUnsavedUpdates) {
			profile.save();
		}

		endLevel.init(
			(state.totalMonsters == 0 ? -1 : (state.killedMonsters * 100 / state.totalMonsters)),
			(state.totalItems == 0 ? -1 : (state.pickedItems * 100 / state.totalItems)),
			(state.totalSecrets == 0 ? -1 : (state.foundSecrets * 100 / state.totalSecrets)),
			(state.timeInTicks / Engine.FRAMES_PER_SECOND)
		);

		UpdateLeaderboardProvider.updateLeaderboard();
	}

	public void nextLevel(boolean isTutorial) {
		skipEndLevelScreenOnce = isTutorial;
		nextLevelTime = engine.elapsedTime;

		soundManager.playSound(SoundManager.SOUND_LEVEL_END);

		if (!isTutorial) {
			soundManager.setPlaylist(SoundManager.LIST_ENDL);
		}
	}

	protected void toggleAutoMap() {
		state.showAutoMap = !state.showAutoMap;
	}

	public void hitHero(int hits, Monster mon) {
		if (killedTime > 0) {
			return;
		}

		if (mon != null) {
			overlay.showHitSide(mon.x, mon.y);
		} else {
			overlay.showOverlay(Overlay.BLOOD);
		}

		if (!state.godMode && nextLevelTime == 0) {
			if (state.heroArmor > 0) {
				state.heroArmor = Math.max(0, state.heroArmor - Math.max(1, hits * 3 / 4));
				state.heroHealth -= Math.max(1, hits / 4);
			} else {
				state.heroHealth -= hits;
			}
		}

		if (state.heroHealth <= 0) {
			state.heroHealth = 0;
			killedTime = engine.elapsedTime;

			if (mon != null) {
				killedAngle = GameMath.getAngle(mon.x - state.heroX, mon.y - state.heroY) * GameMath.RAD2G_F;

				killedHeroAngle = (
					(Math.abs(360.0f + state.heroA - killedAngle) < Math.abs(state.heroA - killedAngle)) ?
					(360.0f + state.heroA) :
					state.heroA
				);
			} else {
				killedAngle = state.heroA;
				killedHeroAngle = state.heroA;
			}

			soundManager.playSound(SoundManager.SOUND_DEATH_HERO);
			engine.soundManager.setPlaylist(SoundManager.LIST_GAMEOVER);
		}
	}

	protected boolean processUse(int x, int y) {
		if (level.doorsMap[y][x] != null) {
			Door door = level.doorsMap[y][x];

			if (door.openPos >= Door.OPEN_POS_PASSABLE) {
				return false;
			}

			if (door.sticked) {
				if (door.requiredKey == 0) {
					overlay.showLabel(Labels.LABEL_CANT_OPEN);
					soundManager.playSound(SoundManager.SOUND_NOWAY);

					if (door.mark != null) {
						processOneMark(100 + door.mark.id);
					}

					return true;
				}

				if ((state.heroKeysMask & door.requiredKey) == 0) {
					if (door.requiredKey == 4) {
						overlay.showLabel(Labels.LABEL_NEED_GREEN_KEY);
					} else if (door.requiredKey == 2) {
						overlay.showLabel(Labels.LABEL_NEED_RED_KEY);
					} else {
						overlay.showLabel(Labels.LABEL_NEED_BLUE_KEY);
					}

					if (door.mark != null) {
						processOneMark(100 + door.mark.id);
					}

					soundManager.playSound(SoundManager.SOUND_NOWAY);
					return true;
				}

				door.sticked = false;
			}

			if (door.open()) {
				if ((state.passableMap[door.y][door.x] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO) == 0) {
					state.levelExp += GameParams.EXP_OPEN_DOOR;
					Achievements.updateStat(Achievements.STAT_DOORS_OPENED, profile, engine, state);
				}

				state.passableMap[door.y][door.x] |= Level.PASSABLE_IS_DOOR_OPENED_BY_HERO;

				if (door.mark != null) {
					processOneMark(door.mark.id);
				}

				return true;
			}

			return false;
		}

		if (level.marksMap[y][x] != null) {
			return processOneMark(level.marksMap[y][x].id);
		}

		return false;
	}

	protected boolean processUse(float x, float y, float wallDist) {
		if (Math.abs(x - prevUseX) < USE_IGNORE_THRESHOLD && Math.abs(y - prevUseY) < USE_IGNORE_THRESHOLD) {
			return false;
		}

		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(state.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(state.levelHeight - 1, (int)(y + wallDist));

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				if (processUse(i, j)) {
					prevUseX = x;
					prevUseY = y;
					return true;
				}
			}
		}

		prevUseX = -1.0f;
		prevUseY = -1.0f;
		return false;
	}

	protected void processShoot() {
		// just for case
		if (weapons.hasNoAmmo(state.heroWeapon)) {
			weapons.selectBestWeapon();
		}

		Weapons.WeaponParams localParams = weapons.currentParams;

		boolean hitOrShoot = Bullet.shootOrPunch(
			state, state.heroX, state.heroY, engine.heroAr, null,
			localParams.ammoIdx, localParams.hits, localParams.hitTimeout
		);

		if (weapons.currentCycle[weapons.shootCycle] > -1000) {
			soundManager.playSound((localParams.noHitSoundIdx != 0 && !hitOrShoot) ?
				localParams.noHitSoundIdx :
				localParams.soundIdx
			);
		}

		if (localParams.ammoIdx >= 0) {
			state.heroAmmo[localParams.ammoIdx] -= localParams.needAmmo;

			if (state.heroAmmo[localParams.ammoIdx] < localParams.needAmmo) {
				if (state.heroAmmo[localParams.ammoIdx] < 0) {
					state.heroAmmo[localParams.ammoIdx] = 0;
				}

				weapons.selectBestWeapon();
			}
		}
	}

	public boolean updateHeroPosition(float dx, float dy, float accel) {
		if (accel < -0.9f) {
			accel = -0.9f;
		} else if (accel > 0.9f) {
			accel = 0.9f;
		}

		float addX = accel * dx;
		float newX = state.heroX;

		if (Math.abs(addX) > 0.01f) {
			level.fillInitialInWallMap(state.heroX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
			newX += addX;

			if (!level.isPassable(newX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO)) {
				if (processUse(newX, state.heroY, WALK_WALL_DIST)) {
					hasMoved = true;
					return false;
				}

				newX = (addX > 0 ?
					FloatMath.ceil(state.heroX) - WALK_WALL_DIST - 0.005f :
					FloatMath.floor(state.heroX) + WALK_WALL_DIST + 0.005f
				);
			}
		}

		float addY = accel * dy;
		float newY = state.heroY;

		if (Math.abs(addY) > 0.01f) {
			level.fillInitialInWallMap(newX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
			newY += addY;

			if (!level.isPassable(newX, newY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO)) {
				if (processUse(newX, newY, WALK_WALL_DIST)) {
					hasMoved = true;
					return false;
				}

				newY = (addY > 0 ?
					FloatMath.ceil(state.heroY) - WALK_WALL_DIST - 0.005f :
					FloatMath.floor(state.heroY) + WALK_WALL_DIST + 0.005f
				);
			}
		}

		boolean positionUpdated = (Math.abs(state.heroX - newX) > GameMath.INFINITY) || (Math.abs(state.heroY - newY) > GameMath.INFINITY);
		hasMoved |= positionUpdated;
		state.heroX = newX;
		state.heroY = newY;

		if (Math.abs(newX - prevUseX) >= USE_IGNORE_THRESHOLD || Math.abs(newY - prevUseY) >= USE_IGNORE_THRESHOLD) {
			prevUseX = -1.0f;
			prevUseY = -1.0f;
		}

		return positionUpdated;
	}

	public void resume() {
		if (engine.gameViewActive && unprocessedGameCode.length() != 0) {
			processGameCode(unprocessedGameCode);
			unprocessedGameCode = "";
		}

		if (engine.gameViewActive) {
			if (!engine.inWallpaperMode && !profile.getLevel(state.levelName).purchased) {
				engine.changeView(Engine.VIEW_TYPE_EOD_BLOCKER);
				return;
			}

			state.showEpisodeSelector = false;
		}
	}

	public void update() {
		if (renderMode == RENDER_MODE_END_LEVEL) {
			endLevel.update();
			return;
		} else if (renderMode == RENDER_MODE_GAME_OVER) {
			gameOver.update();
			return;
		}

		state.timeInTicks++;

		if (playStartLevelSound) {
			soundManager.playSound(SoundManager.SOUND_LEVEL_START);
			playStartLevelSound = false;
		}

		if (firstTouchTime < 0 && engine.interracted) {
			firstTouchTime = engine.elapsedTime;
		}

		hasMoved = false;

		for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
			door.tryClose();
		}

		for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
			mon.update();
		}

		for (Bullet bullet = state.bullets.first(); bullet != null;) {
			Bullet nextBullet = (Bullet)bullet.next;
			bullet.update();

			if (bullet.bulletState == Bullet.STATE_RELEASE) {
				state.bullets.release(bullet);
			}

			bullet = nextBullet;
		}

		for (Explosion explosion = state.explosions.first(); explosion != null;) {
			Explosion nextExplosion = (Explosion)explosion.next;

			if (!explosion.update()) {
				state.explosions.release(explosion);
			}

			explosion = nextExplosion;
		}

		for (Timeout timeout = state.timeouts.first(); timeout != null;) {
			Timeout nextTimeout = (Timeout)timeout.next;

			if (timeout.delay <= 0) {
				level.executeActions(timeout.markId);
				state.timeouts.release(timeout);
			} else {
				timeout.delay--;
			}

			timeout = nextTimeout;
		}

		for (Look look = state.looks.first(); look != null;) {
			Look nextLook = (Look)look.next;

			if (levelRenderer.awTouchedCellsMap[look.y][look.x]) {
				processOneMark(look.markId);
				state.looks.release(look);
			}

			look = nextLook;
		}

		if ((nextLevelTime > 0) || (killedTime > 0)) {
			if (weapons.shootCycle > 0) {
				weapons.shootCycle = (weapons.shootCycle + 1) % weapons.currentCycle.length;
			}

			return;
		}

		if (weapons.currentCycle[weapons.shootCycle] < 0) {
			processShoot();
		}

		if (weapons.shootCycle > 0) {
			weapons.shootCycle = (weapons.shootCycle + 1) % weapons.currentCycle.length;
		}

		if (actionNextWeapon && weapons.shootCycle==0 && weapons.changeWeaponDir==0) {
			weapons.nextWeapon();
			actionNextWeapon = false;
		}

		if (actionFire!=0 && weapons.shootCycle==0 && weapons.changeWeaponDir==0) {
			weapons.shootCycle++;
		}

		if (actionToggleMap) {
			toggleAutoMap();
			actionToggleMap = false;
		}

		if (actionGameMenu) {
			engine.changeView(Engine.VIEW_TYPE_GAME_MENU);
			actionGameMenu = false;

			if (firstTouchTime < 0) {
				firstTouchTime = engine.elapsedTime;
			}
		}

		level.clearPassable(state.heroX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_IS_HERO);
		heroController.updateHero();
		level.setPassable(state.heroX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_IS_HERO);

		if (((int)state.heroX != heroCellX) || ((int)state.heroY != heroCellY)) {
			heroCellX = (int)state.heroX;
			heroCellY = (int)state.heroY;

			processMarks();
			pickObjects();
		}
	}

	protected boolean processOneMark(int markId) {
		if (level.executeActions(markId)) {
			// if this is *not* end level switch, play sound
			if (nextLevelTime == 0) {
				soundManager.playSound(SoundManager.SOUND_SWITCH);
				overlay.showOverlay(Overlay.MARK);
			}

			return true;
		}

		return false;
	}

	protected void processMarks() {
		if (
			(level.marksMap[(int)state.heroY][(int)state.heroX] != null) &&
			(level.doorsMap[(int)state.heroY][(int)state.heroX] == null)
		) {
			processOneMark(level.marksMap[(int)state.heroY][(int)state.heroX].id);
		}
	}

	protected void pickObjects() {
		int cy = (int)state.heroY;
		int cx = (int)state.heroX;

		if ((state.passableMap[cy][cx] & Level.PASSABLE_IS_OBJECT) == 0) {
			return;
		}

		// decide shall we pick object or not

		switch (state.objectsMap[cy][cx]) {
			case TextureLoader.OBJ_ARMOR_GREEN:
			case TextureLoader.OBJ_ARMOR_RED:
				if (state.heroArmor >= maxArmor) {
					return;
				}
				break;

			case TextureLoader.OBJ_STIM:
			case TextureLoader.OBJ_MEDI:
				if (state.heroHealth >= maxHealth) {
					return;
				}
				break;

			case TextureLoader.OBJ_CLIP:
			case TextureLoader.OBJ_AMMO:
				if (state.heroAmmo[Weapons.AMMO_PISTOL] >= maxAmmoPistol) {
					return;
				}
				break;

			case TextureLoader.OBJ_SHELL:
			case TextureLoader.OBJ_SBOX:
				if (state.heroAmmo[Weapons.AMMO_SHOTGUN] >= maxAmmoShotgun) {
					return;
				}
				break;

			case TextureLoader.OBJ_ROCKET:
			case TextureLoader.OBJ_RBOX:
				if (state.heroAmmo[Weapons.AMMO_ROCKET] >= maxAmmoRocket) {
					return;
				}
				break;

			case TextureLoader.OBJ_BPACK:
				if (
					state.heroHealth >= maxHealth
					&& state.heroAmmo[Weapons.AMMO_PISTOL] >= maxAmmoPistol
					&& state.heroAmmo[Weapons.AMMO_SHOTGUN] >= maxAmmoShotgun
				) {
					return;
				}
				break;

			case TextureLoader.OBJ_SHOTGUN:
				if (state.heroHasWeapon[Weapons.WEAPON_SHOTGUN] && state.heroAmmo[Weapons.AMMO_SHOTGUN] >= maxAmmoShotgun) {
					return;
				}
				break;

			case TextureLoader.OBJ_CHAINGUN:
				if (state.heroHasWeapon[Weapons.WEAPON_CHAINGUN] && state.heroAmmo[Weapons.AMMO_PISTOL] >= maxAmmoPistol) {
					return;
				}
				break;

			case TextureLoader.OBJ_DBLSHOTGUN:
				if (state.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] && state.heroAmmo[Weapons.AMMO_SHOTGUN] >= maxAmmoShotgun) {
					return;
				}
				break;

			case TextureLoader.OBJ_DBLCHAINGUN:
				if (state.heroHasWeapon[Weapons.WEAPON_DBLCHAINGUN] && state.heroAmmo[Weapons.AMMO_PISTOL] >= maxAmmoPistol) {
					return;
				}
				break;

			case TextureLoader.OBJ_RLAUNCHER:
				if (state.heroHasWeapon[Weapons.WEAPON_RLAUNCHER] && state.heroAmmo[Weapons.AMMO_ROCKET] >= maxAmmoRocket) {
					return;
				}
				break;
		}

		// play sounds

		switch (state.objectsMap[cy][cx]) {
			case TextureLoader.OBJ_CLIP:
			case TextureLoader.OBJ_AMMO:
			case TextureLoader.OBJ_SHELL:
			case TextureLoader.OBJ_SBOX:
			case TextureLoader.OBJ_ROCKET:
			case TextureLoader.OBJ_RBOX:
				soundManager.playSound(SoundManager.SOUND_PICK_AMMO);
				break;

			case TextureLoader.OBJ_BPACK:
			case TextureLoader.OBJ_SHOTGUN:
			case TextureLoader.OBJ_CHAINGUN:
			case TextureLoader.OBJ_DBLSHOTGUN:
			case TextureLoader.OBJ_CHAINSAW:
			case TextureLoader.OBJ_DBLCHAINGUN:
			case TextureLoader.OBJ_RLAUNCHER:
				soundManager.playSound(SoundManager.SOUND_PICK_WEAPON);
				break;

			default:
				soundManager.playSound(SoundManager.SOUND_PICK_ITEM);
				break;
		}

		// add healh/armor/wepons/bullets

		int bestWeapon = (state.autoSelectWeapon ? -1 : weapons.getBestWeapon());

		switch (state.objectsMap[cy][cx]) {
			case TextureLoader.OBJ_ARMOR_GREEN:
				state.heroArmor = Math.min(state.heroArmor + GameParams.ARMOR_ADD_GREEN, maxArmor);
				break;

			case TextureLoader.OBJ_ARMOR_RED:
				state.heroArmor = Math.min(state.heroArmor + GameParams.ARMOR_ADD_RED, maxArmor);
				break;

			case TextureLoader.OBJ_KEY_BLUE:
				state.heroKeysMask |= 1;
				break;

			case TextureLoader.OBJ_KEY_RED:
				state.heroKeysMask |= 2;
				break;

			case TextureLoader.OBJ_KEY_GREEN:
				state.heroKeysMask |= 4;
				break;

			case TextureLoader.OBJ_OPENMAP:
				levelRenderer.openAllMap();
				break;

			case TextureLoader.OBJ_STIM:
				state.heroHealth = Math.min(state.heroHealth + GameParams.HEALTH_ADD_STIM, maxHealth);
				break;

			case TextureLoader.OBJ_MEDI:
				state.heroHealth = Math.min(state.heroHealth + GameParams.HEALTH_ADD_MEDI, maxHealth);
				break;

			case TextureLoader.OBJ_CLIP:
				state.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(state.heroAmmo[Weapons.AMMO_PISTOL] + GameParams.AMMO_ADD_CLIP, maxAmmoPistol);
				if (bestWeapon < Weapons.WEAPON_PISTOL) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_AMMO:
				state.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(state.heroAmmo[Weapons.AMMO_PISTOL] + GameParams.AMMO_ADD_AMMO, maxAmmoPistol);
				if (bestWeapon < Weapons.WEAPON_PISTOL) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_SHELL:
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(state.heroAmmo[Weapons.AMMO_SHOTGUN] + GameParams.AMMO_ADD_SHELL, maxAmmoShotgun);

				if (bestWeapon < Weapons.WEAPON_SHOTGUN && state.heroHasWeapon[Weapons.WEAPON_SHOTGUN]) {
					weapons.selectBestWeapon();
					state.autoSelectWeapon = true;
				}
				break;

			case TextureLoader.OBJ_SBOX:
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(state.heroAmmo[Weapons.AMMO_SHOTGUN] + GameParams.AMMO_ADD_SBOX, maxAmmoShotgun);

				if (bestWeapon < Weapons.WEAPON_SHOTGUN && state.heroHasWeapon[Weapons.WEAPON_SHOTGUN]) {
					weapons.selectBestWeapon();
					state.autoSelectWeapon = true;
				}
				break;

			case TextureLoader.OBJ_ROCKET:
				state.heroAmmo[Weapons.AMMO_ROCKET] = Math.min(state.heroAmmo[Weapons.AMMO_ROCKET] + GameParams.AMMO_ADD_ROCKET, maxAmmoRocket);

				if (bestWeapon < Weapons.WEAPON_RLAUNCHER && state.heroHasWeapon[Weapons.WEAPON_RLAUNCHER]) {
					weapons.selectBestWeapon();
					state.autoSelectWeapon = true;
				}
				break;

			case TextureLoader.OBJ_RBOX:
				state.heroAmmo[Weapons.AMMO_ROCKET] = Math.min(state.heroAmmo[Weapons.AMMO_ROCKET] + GameParams.AMMO_ADD_RBOX, maxAmmoRocket);

				if (bestWeapon < Weapons.WEAPON_RLAUNCHER && state.heroHasWeapon[Weapons.WEAPON_RLAUNCHER]) {
					weapons.selectBestWeapon();
					state.autoSelectWeapon = true;
				}
				break;

			case TextureLoader.OBJ_BPACK:
				state.heroHealth = Math.min(state.heroHealth + GameParams.HEALTH_ADD_STIM, maxHealth);
				state.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(state.heroAmmo[Weapons.AMMO_PISTOL] + GameParams.AMMO_ADD_CLIP, maxAmmoPistol);
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(state.heroAmmo[Weapons.AMMO_SHOTGUN] + GameParams.AMMO_ADD_SHELL, maxAmmoShotgun);
				// do not check shotgun existing (than, if it didn't exists, pistol will be selected)
				if (bestWeapon < Weapons.WEAPON_SHOTGUN) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_SHOTGUN:
				state.heroHasWeapon[Weapons.WEAPON_SHOTGUN] = true;
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(state.heroAmmo[Weapons.AMMO_SHOTGUN] + GameParams.AMMO_ADD_SHOTGUN, maxAmmoShotgun);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_CHAINGUN:
				state.heroHasWeapon[Weapons.WEAPON_CHAINGUN] = true;
				state.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(state.heroAmmo[Weapons.AMMO_PISTOL] + GameParams.AMMO_ADD_CHAINGUN, maxAmmoPistol);
				if (bestWeapon < Weapons.WEAPON_CHAINGUN) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_CHAINSAW:
				if (!state.heroHasWeapon[Weapons.WEAPON_CHAINSAW]) {
					state.heroHasWeapon[Weapons.WEAPON_CHAINSAW] = true;
					weapons.switchWeapon(Weapons.WEAPON_CHAINSAW);
				}
				break;

			case TextureLoader.OBJ_DBLSHOTGUN:
				state.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] = true;
				state.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(state.heroAmmo[Weapons.AMMO_SHOTGUN] + GameParams.AMMO_ADD_DBLSHOTGUN, maxAmmoShotgun);
				if (bestWeapon < Weapons.WEAPON_DBLSHOTGUN) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_DBLCHAINGUN:
				state.heroHasWeapon[Weapons.WEAPON_DBLCHAINGUN] = true;
				state.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(state.heroAmmo[Weapons.AMMO_PISTOL] + GameParams.AMMO_ADD_DBLCHAINGUN, maxAmmoPistol);
				if (bestWeapon < Weapons.WEAPON_DBLCHAINGUN) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;

			case TextureLoader.OBJ_RLAUNCHER:
				state.heroHasWeapon[Weapons.WEAPON_RLAUNCHER] = true;
				state.heroAmmo[Weapons.AMMO_ROCKET] = Math.min(state.heroAmmo[Weapons.AMMO_ROCKET] + GameParams.AMMO_ADD_RLAUNCHER, maxAmmoRocket);
				if (bestWeapon < Weapons.WEAPON_RLAUNCHER) { weapons.selectBestWeapon(); state.autoSelectWeapon = true; }
				break;
		}

		// count objects leaved by monsters, because when monster leave object, it increate total items count
		state.pickedItems++;
		state.levelExp += GameParams.EXP_PICK_OBJECT;

		// remove picked objects from map
		state.objectsMap[cy][cx] = 0;
		state.passableMap[cy][cx] &= ~Level.PASSABLE_MASK_OBJECT;
		levelRenderer.modLightMap(cx, cy, -LevelRenderer.LIGHT_OBJECT);

		overlay.showOverlay(Overlay.ITEM);
	}

	protected void drawCrosshair(GL10 gl) {
		renderer.initOrtho(gl, true, false, -engine.ratio, engine.ratio, -1.0f, 1.0f, 0f, 1.0f);
		renderer.init();

		renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 0.5f);

		renderer.setQuadOrthoCoords(-CROSSHAIR_SIZE, 0.03f, CROSSHAIR_SIZE, 0.08f); // up
		renderer.drawQuad();

		renderer.setQuadOrthoCoords(CROSSHAIR_SIZE, -0.03f, -CROSSHAIR_SIZE, -0.08f); // down
		renderer.drawQuad();

		renderer.setQuadOrthoCoords(0.03f, -CROSSHAIR_SIZE, 0.08f, CROSSHAIR_SIZE); // right
		renderer.drawQuad();

		renderer.setQuadOrthoCoords(-0.03f, CROSSHAIR_SIZE, -0.08f, -CROSSHAIR_SIZE); // left
		renderer.drawQuad();

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		renderer.flush(gl, false);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void drawSky(GL10 gl) {
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_ALPHA_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_FLAT);

		renderer.initOrtho(gl, true, false, 0.0f, 1.0f, 0.0f, 1.0f, 0f, 1.0f);

		renderer.init();
		renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
		renderer.setQuadTexCoords(0, 0, 4 << 16, 1 << 16);

		float ox = FloatMath.sin((state.heroA % 30.0f) * GameMath.G2RAD_F);
		float oy = FloatMath.sin(state.heroVertA * GameMath.G2RAD_F);

		renderer.setQuadOrthoCoords(ox - 1.0f, 1.1f - oy, ox + 1.0f, 0.15f - oy);
		renderer.drawQuad();

		renderer.bindTextureRep(gl, engine.textureLoader.textures[TextureLoader.TEXTURE_SKY]);
		renderer.flush(gl);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();

		gl.glEnable(GL10.GL_CULL_FACE);
	}

	protected void render(GL10 gl) {
		long walkTime = 0;

		if (hasMoved) {
			if (prevMovedTime != 0) {
				walkTime = engine.elapsedTime - prevMovedTime;
			} else {
				prevMovedTime = engine.elapsedTime;
			}
		} else {
			prevMovedTime = 0;
		}

		float yoff = LevelRenderer.HALF_WALL / 8.0f + FloatMath.sin((float)walkTime / 100.0f) * LevelRenderer.HALF_WALL / 16.0f;

		if (killedTime > 0) {
			yoff -= Math.min(1.0f, (float)(engine.elapsedTime - killedTime) / 500.0f) * LevelRenderer.HALF_WALL / 2.0f;
			state.setHeroA(killedHeroAngle + (killedAngle - killedHeroAngle) * Math.min(1.0f, (float)(engine.elapsedTime - killedTime) / 1000.0f));
		}

		drawSky(gl);
		levelRenderer.render(gl, engine.elapsedTime, -yoff);

		if (config.showCrosshair && !engine.inWallpaperMode) {
			drawCrosshair(gl);
		}

		weapons.render(gl, walkTime);

		if (state.showAutoMap) {
			levelRenderer.renderAutoMap(gl);
		}

		overlay.renderOverlay(gl);
		overlay.renderHitSide(gl);

		if (!engine.inWallpaperMode) {
			engine.stats.render(gl);

			if (renderMode == RENDER_MODE_GAME) {
				heroController.renderControls(gl, state.showHelp, state.showHelpFull, (firstTouchTime >= 0 ? firstTouchTime : engine.elapsedTime));
			}
		}

		if (nextLevelTime > 0) {
			overlay.renderEndLevelLayer(gl, (float)(engine.elapsedTime - nextLevelTime) / 500.0f);
		}

		if (renderMode == RENDER_MODE_END_LEVEL) {
			endLevel.render(gl);
			heroController.renderControls(gl, false, false, 0L);
		} else if (renderMode == RENDER_MODE_GAME_OVER) {
			gameOver.render(gl);
			heroController.renderControls(gl, false, false, 0L);
		}

		overlay.renderLabels(gl);

		if (config.gamma > 0.01f) {
			overlay.renderGammaLayer(gl);
		}

		if (engine.showFps) {
			engine.drawFps(gl);
		}

		if (renderMode == RENDER_MODE_GAME) {
			if (nextLevelTime > 0) {
				if (engine.elapsedTime - nextLevelTime > 1000) {
					if (isGameOverFlag) {
						showGameOverScreen();
					} else {
						showEndLevelScreen();
					}
				}
			} else if ((killedTime > 0) && (engine.elapsedTime - killedTime > 3500)) {
				isGameOverFlag = true;
				nextLevelTime = engine.elapsedTime;
			}
		}
	}
}
