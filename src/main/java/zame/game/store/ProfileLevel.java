package zame.game.store;

public class ProfileLevel {
	protected Profile profile;
	protected ProfileLevel prev;
	protected ProfileLevel next;

	public String name;
	public int storeEpisodeId;
	public boolean purchased;
	public int profileIndex;
	public int episodeIndex;
	public int episodeLevelsCount = 1;

	public ProfileLevel(String name, int storeEpisodeId) {
		this.name = name;
		this.storeEpisodeId = storeEpisodeId;
		this.purchased = false;
	}

	public void setProfile(Profile profile, int profileIndex, int episodeIndex, ProfileLevel prev, ProfileLevel next) {
		this.profile = profile;
		this.profileIndex = profileIndex;
		this.episodeIndex = episodeIndex;
		this.prev = prev;
		this.next = next;
	}

	public static void updateProfileLevels(Profile profile) {
		for (ProfileLevel profileLevel : profile.levels) {
			if (profileLevel.storeEpisodeId < 0 || profile.isPurchased(profileLevel.storeEpisodeId)) {
				profileLevel.purchased = true;
			}
		}
	}

	public String getNextLevelName() {
		return (next == null ? "" : next.name);
	}
}
