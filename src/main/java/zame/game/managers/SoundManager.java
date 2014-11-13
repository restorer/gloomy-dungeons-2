package zame.game.managers;

import zame.game.MyApplication;

public abstract class SoundManager {
	public static class PlayList {
		public String[] list;
		public int idx;

		public PlayList(String[] list) {
			this.list = list;
		}
	}

	public static final int FOCUS_MASK_MAIN_ACTIVITY = 1;
	public static final int FOCUS_MASK_PROGRESS_DIALOG = 2;
	public static final int FOCUS_MASK_GAME_CODE_DIALOG = 4;
	public static final int FOCUS_MASK_GAME_MENU_DIALOG = 8;
	public static final int FOCUS_MASK_LOAD_DIALOG = 16;
	public static final int FOCUS_MASK_LOAD_WARN_DIALOG = 32;
	public static final int FOCUS_MASK_RESTART_WARN_DIALOG = 64;
	public static final int FOCUS_MASK_SAVE_DIALOG = 128;
	public static final int FOCUS_MASK_STORE_VIEW_DIALOG = 256;
	public static final int FOCUS_MASK_STORE_BUY_DIALOG = 512;
	public static final int FOCUS_MASK_STORE_NO_CREDITS_DIALOG = 1024;
	public static final int FOCUS_MASK_CHANGE_DIFFICULTY_DIALOG = 2048;
	public static final int FOCUS_MASK_SHARE_DIALOG = 4096;
	public static final int FOCUS_MASK_ONOFF_STATE_DIALOG = 8192;
	public static final int FOCUS_MASK_QUIT_WARN_DIALOG = 16384;
	public static final int FOCUS_MASK_RATE_GAME_DIALOG = 32768;
	public static final int FOCUS_MASK_DELETE_PROFILE_DIALOG = 65536;

	public static final PlayList LIST_MAIN = new PlayList(new String[] {
		"dlc_t1.mp3",
		"l1.mid",
		"dlc_t2.mp3",
		"l2.mid",
		"dlc_t3.mp3",
		"l3.mid",
		"dlc_t4.mp3",
		"l4.mid",
	});

	public static final PlayList LIST_ENDL = new PlayList(new String[] { "endl.mid" });
	public static final PlayList LIST_GAMEOVER = new PlayList(new String[] { "gameover.mid" });

	public static final int SOUND_BTN_PRESS = 0;
	public static final int SOUND_NOWAY = 1;
	public static final int SOUND_DOOR_OPEN = 2;
	public static final int SOUND_DOOR_CLOSE = 3;
	public static final int SOUND_SHOOT_PIST = 4;
	public static final int SOUND_SHOOT_SHTG = 5;
	public static final int SOUND_LEVEL_START = 6;
	public static final int SOUND_LEVEL_END = 7;
	public static final int SOUND_SWITCH = 8;
	public static final int SOUND_PICK_ITEM = 9;
	public static final int SOUND_PICK_AMMO = 10;
	public static final int SOUND_PICK_WEAPON = 11;
	public static final int SOUND_ATTACK_MON_3 = 12;
	public static final int SOUND_DEATH_HERO = 13;
	public static final int SOUND_SHOOT_HAND = 14;
	public static final int SOUND_SHOOT_DBLSHTG = 15;
	public static final int SOUND_SHOOT_SAW = 16;
	public static final int SOUND_SHOOT_ROCKET = 17;
	public static final int SOUND_BOOM = 18;
	public static final int SOUND_ATTACK_MON_4 = 19;
	public static final int SOUND_ATTACK_MON_7 = 20;
	public static final int SOUND_DEATH_MON_1 = 21;
	public static final int SOUND_DEATH_MON_2 = 22;
	public static final int SOUND_DEATH_MON_3 = 23;
	public static final int SOUND_DEATH_MON_4 = 24;
	public static final int SOUND_DEATH_MON_5 = 25;
	public static final int SOUND_DEATH_MON_6 = 26;
	public static final int SOUND_DEATH_MON_7 = 27;
	public static final int SOUND_DEATH_MON_8 = 28;
	public static final int SOUND_READY_MON_1 = 29;
	public static final int SOUND_READY_MON_2 = 30;
	public static final int SOUND_READY_MON_3 = 31;
	public static final int SOUND_READY_MON_4 = 32;
	public static final int SOUND_READY_MON_5 = 33;
	public static final int SOUND_READY_MON_6 = 34;
	public static final int SOUND_READY_MON_7 = 35;
	public static final int SOUND_READY_MON_8 = 36;
	public static final int SOUND_LAST = 37;

	public static final int SOUND_ATTACK_MON_1 = SOUND_SHOOT_PIST;
	public static final int SOUND_ATTACK_MON_2 = SOUND_SHOOT_SHTG;
	public static final int SOUND_ATTACK_MON_5 = SOUND_SHOOT_PIST;
	public static final int SOUND_ATTACK_MON_6 = SOUND_SHOOT_SHTG;
	public static final int SOUND_ATTACK_MON_8 = SOUND_SHOOT_ROCKET;
	public static final int SOUND_ACHIEVEMENT_UNLOCKED = SOUND_PICK_ITEM;

	public static final int[] SOUNDLIST_ATTACK_MON = {
		SOUND_ATTACK_MON_1,
		SOUND_ATTACK_MON_2,
		SOUND_ATTACK_MON_3,
		SOUND_ATTACK_MON_4,
		SOUND_ATTACK_MON_5,
		SOUND_ATTACK_MON_6,
		SOUND_ATTACK_MON_7,
		SOUND_ATTACK_MON_8,
	};

	public static final int[] SOUNDLIST_DEATH_MON = {
		SOUND_DEATH_MON_1,
		SOUND_DEATH_MON_2,
		SOUND_DEATH_MON_3,
		SOUND_DEATH_MON_4,
		SOUND_DEATH_MON_5,
		SOUND_DEATH_MON_6,
		SOUND_DEATH_MON_7,
		SOUND_DEATH_MON_8,
	};

	public static final int[] SOUNDLIST_READY_MON = {
		SOUND_READY_MON_1,
		SOUND_READY_MON_2,
		SOUND_READY_MON_3,
		SOUND_READY_MON_4,
		SOUND_READY_MON_5,
		SOUND_READY_MON_6,
		SOUND_READY_MON_7,
		SOUND_READY_MON_8,
	};

	public static SoundManager getInstance(boolean isWallpaper) {
		if (isWallpaper) {
			return new SoundManagerDummy();
		}

		if (MyApplication.self.soundManagerInst == null) {
			MyApplication.self.soundManagerInst = new SoundManagerInst();
		}

		return MyApplication.self.soundManagerInst;
	}

	public boolean instantPause = true;

	public void playSound(int idx) {
		playSound(idx, 1.0f);
	}

	public abstract void setSoundEnabledSetting(boolean enabled);
	public abstract void setMusicVolumeSetting(int volume);
	public abstract void setEffectsVolumeSetting(int volume);
	public abstract void onSettingsUpdated();

	public abstract void playSound(int idx, float volume);
	public abstract void setPlaylist(PlayList playlist);
	public abstract void onWindowFocusChanged(boolean hasFocus, int focusMask);
	public abstract void initialize();
	public abstract void shutdown();
}
