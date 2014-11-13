package zame.game.store.achievements;

import java.util.Locale;
import zame.game.engine.Engine;
import zame.game.engine.State;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;

public class Achievement {
	public int id;
	public int statId;
	public int maxValue;
	public int titleResourceId;
	public int descriptionResourceId;

	public Achievement(int id, int statId, int maxValue, int titleResourceId, int descriptionResourceId) {
		this.id = id;
		this.statId = statId;
		this.maxValue = maxValue;
		this.titleResourceId = titleResourceId;
		this.descriptionResourceId = descriptionResourceId;
	}

	public void update(Profile profile, Engine engine, State state) {
		if (engine.inWallpaperMode) {
			return;
		}

		if (!profile.achieved[id] && state.stats[statId] >= maxValue) {
			profile.achieved[id] = true;
			profile.update();

			engine.overlay.showAchievement(titleResourceId);
			engine.soundManager.playSound(SoundManager.SOUND_ACHIEVEMENT_UNLOCKED);
		}
	}

	public boolean isAchieved(Profile profile) {
		return profile.achieved[id];
	}

	public String getStatusText(Profile profile, State state) {
		return String.format(Locale.US, "%d/%d", (state.stats[statId] < 0 ? 0 : state.stats[statId]), maxValue);
	}
}
