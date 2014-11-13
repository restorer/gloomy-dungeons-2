package zame.game.managers;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.Common;
import zame.game.MyApplication;

// http://stackoverflow.com/questions/7437505/how-to-properly-use-soundpool-on-a-game
// http://stackoverflow.com/questions/10181822/android-soundpool-play-sometimes-lags

public class SoundManagerInst extends SoundManager {
	public static class SoundThread extends Thread {
		public static class Item {
			int idx;
			float volume;
			float effectsVolume;

			public Item(int idx, float volume, float effectsVolume) {
				this.idx = idx;
				this.volume = volume;
				this.effectsVolume = effectsVolume;
			}
		}

		protected static final int MAX_PLAYING_SOUNDS = 16;

		protected AssetManager assetManager;
		protected SoundPool soundPool = new SoundPool(MAX_PLAYING_SOUNDS, AudioManager.STREAM_MUSIC, 0);
		protected int[] soundIds = new int[SOUND_LAST];
		protected float[] soundVolumes = new float[SOUND_LAST];

		public BlockingQueue<Item> queue = new LinkedBlockingQueue<Item>(MAX_PLAYING_SOUNDS);
		public volatile boolean isActive = true;

		public SoundThread(AssetManager assetManager) {
			this.assetManager = assetManager;

			loadSound("btn_press", SOUND_BTN_PRESS, 1.0f);
			loadSound("noway", SOUND_NOWAY, 1.0f);
			loadSound("door_open", SOUND_DOOR_OPEN, 1.0f); // 0.5f
			loadSound("door_close", SOUND_DOOR_CLOSE, 1.0f); // 0.5f
			loadSound("shoot_pist", SOUND_SHOOT_PIST, 1.0f); // 0.5f
			loadSound("shoot_shtg", SOUND_SHOOT_SHTG, 1.0f); // 0.75f
			loadSound("level_start", SOUND_LEVEL_START, 1.0f); // 0.75f
			loadSound("level_end", SOUND_LEVEL_END, 1.0f);
			loadSound("switch", SOUND_SWITCH, 1.0f);
			loadSound("pick_item", SOUND_PICK_ITEM, 1.0f); // 0.75f
			loadSound("pick_ammo", SOUND_PICK_AMMO, 1.0f); // 0.75f
			loadSound("pick_weapon", SOUND_PICK_WEAPON, 1.0f);
			loadSound("attack_mon_3", SOUND_ATTACK_MON_3, 1.0f);
			loadSound("death_hero", SOUND_DEATH_HERO, 1.0f);
			loadSound("shoot_hand", SOUND_SHOOT_HAND, 1.0f); // 0.5f
			loadSound("shoot_dblshtg", SOUND_SHOOT_DBLSHTG, 1.0f); // 0.5f
			loadSound("shoot_saw", SOUND_SHOOT_SAW, 1.0f); // 0.5f
			loadSound("shoot_rocket", SOUND_SHOOT_ROCKET, 1.0f); // 0.5f
			loadSound("boom", SOUND_BOOM, 1.0f); // 0.5f
			loadSound("attack_mon_4", SOUND_ATTACK_MON_4, 1.0f);
			loadSound("attack_mon_7", SOUND_ATTACK_MON_7, 1.0f);
			loadSound("death_mon_1", SOUND_DEATH_MON_1, 1.0f);
			loadSound("death_mon_2", SOUND_DEATH_MON_2, 1.0f);
			loadSound("death_mon_3", SOUND_DEATH_MON_3, 1.0f);
			loadSound("death_mon_4", SOUND_DEATH_MON_4, 1.0f);
			loadSound("death_mon_5", SOUND_DEATH_MON_5, 1.0f);
			loadSound("death_mon_6", SOUND_DEATH_MON_6, 1.0f);
			loadSound("death_mon_7", SOUND_DEATH_MON_7, 1.0f);
			loadSound("death_mon_8", SOUND_DEATH_MON_8, 1.0f);
			loadSound("ready_mon_1", SOUND_READY_MON_1, 1.0f);
			loadSound("ready_mon_2", SOUND_READY_MON_2, 1.0f);
			loadSound("ready_mon_3", SOUND_READY_MON_3, 1.0f);
			loadSound("ready_mon_4", SOUND_READY_MON_4, 1.0f);
			loadSound("ready_mon_5", SOUND_READY_MON_5, 1.0f);
			loadSound("ready_mon_6", SOUND_READY_MON_6, 1.0f);
			loadSound("ready_mon_7", SOUND_READY_MON_7, 1.0f);
			loadSound("ready_mon_8", SOUND_READY_MON_8, 1.0f);
		}

		protected void loadSound(String name, int idx, float volume) {
			try {
				AssetFileDescriptor afd = assetManager.openFd("sounds/" + name + ".mp3");
				int soundId = soundPool.load(afd, 1);
				afd.close();

				soundIds[idx] = soundId;
				soundVolumes[idx] = volume;
			} catch (Exception ex) {
				Common.log(ex);
				soundIds[idx] = -1;
			}
		}

		@Override
		public void run() {
			try {
				Item item;

				while (isActive) {
					item = queue.take();

					if (item != null && item.idx >= 0 && item.idx < SOUND_LAST && soundIds[item.idx] >= 0) {
						float actVolume = soundVolumes[item.idx] * item.effectsVolume * item.volume;
						soundPool.play(soundIds[item.idx], actVolume, actVolume, 0, 0, 1.0f);
					}
				}
			} catch (InterruptedException ex) {
			}
		}
	}

	public class PauseTask extends TimerTask {
		public void run() {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}

			pauseTask = null;
		}
	}

	protected AssetManager assetManager;
	protected MediaPlayer mediaPlayer = new MediaPlayer();
	protected volatile SoundThread soundThread;

	protected PlayList current = null;
	protected boolean musicLoaded = false;
	protected Timer pauseTimer = new Timer();
	protected TimerTask pauseTask = null;

	protected boolean soundEnabled = false;
	protected float musicVolume = 1.0f;
	protected float effectsVolume = 1.0f;
	protected int inFocusMask = 0;

	public SoundManagerInst() {
		assetManager = MyApplication.self.getAssets();

		mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Common.log("MediaPlayer error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
				return false;
			}
		});

		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				if (current != null) {
					current.idx = (current.idx + 1) % current.list.length;
				}

				play(true);
			}
		});
	}

	protected synchronized SoundThread getSoundThread() {
		if (soundThread == null) {
			soundThread = new SoundThread(assetManager);
			soundThread.start();
		}

		return soundThread;
	}

	protected void updateVolume() {
		SharedPreferences sp = MyApplication.self.getSharedPreferences();
		setSoundEnabledSetting(sp.getBoolean("EnableSound", false));
		setMusicVolumeSetting(sp.getInt("MusicVolume", 10));
		setEffectsVolumeSetting(sp.getInt("EffectsVolume", 5));
	}

	protected void play(boolean wasPlaying) {
		mediaPlayer.reset();
		musicLoaded = false;

		if (current != null) {
			int oldIdx = current.idx;

			for (;;) {
				String name = current.list[current.idx];

				try {
					if (name.length() > 4 && "dlc_".equals(name.substring(0, 4))) {
						File file = new File(MyApplication.self.INTERNAL_ROOT + name);

						if (file.exists()) {
							FileInputStream is = new FileInputStream(file);

							try {
								mediaPlayer.setDataSource(is.getFD());
								mediaPlayer.prepare();
								musicLoaded = true;
							} catch (Exception ex) {
								Common.log(ex);
							}

							is.close();
						}
					} else {
						AssetFileDescriptor afd = assetManager.openFd("music/" + name);

						try {
							mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
							mediaPlayer.prepare();
							musicLoaded = true;
						} catch (Exception ex) {
							Common.log(ex);
						}

						afd.close();
					}
				} catch (Exception ex) {
					Common.log(ex);
				}

				if (musicLoaded) {
					break;
				}

				current.idx = (current.idx + 1) % current.list.length;

				if (current.idx == oldIdx) {
					break;
				}
			}
		}

		if (pauseTask != null) {
			pauseTask.cancel();
			pauseTimer.purge();
			pauseTask = null;
		} else if (musicLoaded && wasPlaying) {
			mediaPlayer.setVolume(musicVolume, musicVolume);
			mediaPlayer.start();
		}
	}

	@Override
	public void setSoundEnabledSetting(boolean enabled) {
		soundEnabled = enabled;
	}

	@Override
	public void setMusicVolumeSetting(int volume) {
		musicVolume = (float)volume / 10.0f; // 0 .. 1.0
	}

	@Override
	public void setEffectsVolumeSetting(int volume) {
		effectsVolume = (float)volume / 10.0f; // 0 .. 1.0
	}

	@Override
	public void onSettingsUpdated() {
		if (soundEnabled) {
			if (musicLoaded && (musicVolume > 0.01f)) {
				mediaPlayer.setVolume(musicVolume, musicVolume);
				mediaPlayer.start();
			}
		} else {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		}
	}

	@Override
	public void playSound(int idx, float volume) {
		if (soundEnabled && (effectsVolume > 0.01f) && (volume > 0.01f)) {
			getSoundThread().queue.offer(new SoundThread.Item(idx, volume, effectsVolume));
		}
	}

	@Override
	public void setPlaylist(PlayList playlist) {
		if (current != playlist) {
			PlayList prev = current;
			boolean isPlaying = mediaPlayer.isPlaying();

			current = playlist;
			play(isPlaying || (soundEnabled && prev == null));

			if (prev != null && isPlaying) {
				prev.idx = (prev.idx + 1) % prev.list.length;
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus, int focusMask) {
		// Common.log("hasFocus=" + String.valueOf(hasFocus) + ", focusMask=" + String.valueOf(focusMask) + ", inFocusMask=" + String.valueOf(inFocusMask) + ", instantPause=" + String.valueOf(instantPause));

		if (hasFocus) {
			inFocusMask |= focusMask;
			instantPause = true;
			updateVolume();

			if (pauseTask != null) {
				pauseTask.cancel();
				pauseTimer.purge();
				pauseTask = null;
			}

			if (soundEnabled) {
				if (musicLoaded && (musicVolume > 0.01f)) {
					mediaPlayer.setVolume(musicVolume, musicVolume);
					mediaPlayer.start();
				}
			} else {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
			}
		} else {
			inFocusMask &= ~focusMask;

			if (inFocusMask == 0) {
				if (mediaPlayer.isPlaying()) {
					if (instantPause) {
						if (pauseTask != null) {
							pauseTask.cancel();
							pauseTimer.purge();
							pauseTask = null;
						}

						if (mediaPlayer.isPlaying()) {
							mediaPlayer.pause();
						}
					} else if (pauseTask == null) {
						pauseTask = new PauseTask();
						pauseTimer.schedule(pauseTask, 2000);
					}
				}
			}

			instantPause = true;
		}
	}

	@Override
	public void initialize() {
		getSoundThread(); // pre-load sounds
	}

	@Override
	public synchronized void shutdown() {
		if (soundThread != null && soundThread.isActive) {
			soundThread.isActive = false;
			soundThread = null;
		}
	}
}
