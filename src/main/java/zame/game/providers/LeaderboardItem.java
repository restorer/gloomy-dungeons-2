package zame.game.providers;

public class LeaderboardItem {
	public String uid;
	public int exp;
	public String name;
	public int achievementsCount;

	public LeaderboardItem(String uid, int exp, String name, int achievementsCount) {
		this.uid = uid;
		this.exp = exp;
		this.name = name;
		this.achievementsCount = achievementsCount;
	}
}
