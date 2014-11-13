package zame.game.engine;

import android.content.res.AssetManager;
import java.util.Locale;
import zame.game.Common;

public class LevelConfig {
	public static final int HIT_TYPE_EAT = 0;
	public static final int HIT_TYPE_PIST = 1;
	public static final int HIT_TYPE_SHTG = 2;
	public static final int HIT_TYPE_ROCKET = 3;

	public static class MonsterConfig {
		int health;
		int hits;
		int hitType;

		public MonsterConfig(int health, int hits, int hitType) {
			this.health = health;
			this.hits = hits;
			this.hitType = hitType;
		}
	}

	public String levelName;
	public int graphicsSet;
	public MonsterConfig[] monsters;

	public LevelConfig(String levelName) {
		this.levelName = levelName;
		this.graphicsSet = 1;

		this.monsters = new MonsterConfig[] {
			new MonsterConfig(4, 4, HIT_TYPE_PIST),
			new MonsterConfig(8, 8, HIT_TYPE_SHTG),
			new MonsterConfig(32, 32, HIT_TYPE_EAT),
			new MonsterConfig(64, 64, HIT_TYPE_EAT),
			new MonsterConfig(12, 12, HIT_TYPE_PIST),
			new MonsterConfig(24, 24, HIT_TYPE_SHTG),
			new MonsterConfig(24, 24, HIT_TYPE_EAT),
			new MonsterConfig(40, 40, HIT_TYPE_ROCKET),
		};
	}

	public static LevelConfig read(AssetManager assetManager, String levelName) {
		LevelConfig res = new LevelConfig(levelName);

		try {
			byte[] data = Common.readBytes(assetManager.open(String.format(Locale.US, "levels/%s.map", levelName)));

			int pos = 0;
			res.graphicsSet = data[pos++];

			while (pos < data.length) {
				int idx = (int)data[pos] & 0xFF;

				if (idx < 1 || idx > res.monsters.length) {
					break;
				}

				res.monsters[idx - 1].health = (int)data[pos + 1] & 0xFF;
				res.monsters[idx - 1].hits = (int)data[pos + 2] & 0xFF;
				res.monsters[idx - 1].hitType = (int)data[pos + 3] & 0xFF;
				pos += 4;
			}

			data = null;
		} catch (Exception ex) {
			Common.log(ex);
			throw new RuntimeException(ex);
		}

		return res;
	}
}
