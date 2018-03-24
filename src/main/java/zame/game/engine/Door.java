package zame.game.engine;

import android.util.FloatMath;
import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;
import zame.game.managers.SoundManager;

public class Door extends DataListItem implements EngineObject, DataItem {
	protected static final int FIELD_X = 1;
	protected static final int FIELD_Y = 2;
	protected static final int FIELD_TEXTURE = 3;
	protected static final int FIELD_VERT = 4;
	protected static final int FIELD_OPEN_POS = 5;
	protected static final int FIELD_DIR = 6;
	protected static final int FIELD_STICKED = 7;
	protected static final int FIELD_REQUIRED_KEY = 8;
	protected static final int FIELD_UID = 9;

	public static final float OPEN_POS_MAX = 0.9f;
	public static final float OPEN_POS_PASSABLE = 0.7f;
	public static final int OPEN_TIME = 1000 * 5;

	protected Engine engine;
	protected State state;

	public int uid; // required for save/load for autoWalls
	public int x;
	public int y;
	public int texture;
	public boolean vert;
	public float openPos;
	public int dir;
	public boolean sticked;
	public int requiredKey;

	public long lastTime;
	public Mark mark;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
	}

	public void init() {
		openPos = 0.0f;
		dir = 0;
		lastTime = 0;
		sticked = false;
		requiredKey = 0;
		mark = null;
	}

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_X, x);
		writer.write(FIELD_Y, y);
		writer.write(FIELD_TEXTURE, TextureLoader.packTexId(texture));
		writer.write(FIELD_VERT, vert);
		writer.write(FIELD_OPEN_POS, openPos);
		writer.write(FIELD_DIR, dir);
		writer.write(FIELD_STICKED, sticked);
		writer.write(FIELD_REQUIRED_KEY, requiredKey);
		writer.write(FIELD_UID, uid);
	}

	public void readFrom(DataReader reader) {
		x = reader.readInt(FIELD_X);
		y = reader.readInt(FIELD_Y);
		texture = TextureLoader.unpackTexId(reader.readInt(FIELD_TEXTURE));
		vert = reader.readBoolean(FIELD_VERT);
		openPos = reader.readFloat(FIELD_OPEN_POS);
		dir = reader.readInt(FIELD_DIR);
		sticked = reader.readBoolean(FIELD_STICKED);
		requiredKey = reader.readInt(FIELD_REQUIRED_KEY);
		uid = reader.readInt(FIELD_UID);

		// previously was lastTime = elapsedTime, but that was incorrect,
		// becase at load process elapsedTime can be > than actual elapsedTime
		lastTime = 0;
	}

	public void stick(boolean opened) {
		sticked = true;
		dir = (opened ? 1 : -1);
		lastTime = 0;	// instant open or close
	}

	private float getVolume() {
		float dx = state.heroX - ((float)x + 0.5f);
		float dy = state.heroY - ((float)y + 0.5f);
		float dist = FloatMath.sqrt(dx * dx + dy * dy);

		return (1.0f / Math.max(1.0f, dist * 0.5f));
	}

	public boolean open() {
		if (dir != 0) {
			return false;
		}

		lastTime = engine.elapsedTime;
		dir = 1;

		engine.soundManager.playSound(SoundManager.SOUND_DOOR_OPEN, getVolume());
		return true;
	}

	public void tryClose() {
		if (sticked || (dir != 0) || (openPos < OPEN_POS_MAX)) {
			return;
		} else if ((state.passableMap[y][x] & Level.PASSABLE_MASK_DOOR) != 0) {
			lastTime = engine.elapsedTime;
			return;
		} else if ((engine.elapsedTime - lastTime) < OPEN_TIME) {
			return;
		}

		dir = -1;
		lastTime = engine.elapsedTime;
		engine.soundManager.playSound(SoundManager.SOUND_DOOR_CLOSE, getVolume());
	}

	public void update(long elapsedTime) {
		if (dir > 0) {
			state.wallsMap[y][x] = 0; // clear door mark for PortalTracer

			if (openPos >= OPEN_POS_PASSABLE) {
				state.passableMap[y][x] &= ~Level.PASSABLE_IS_DOOR;

				if (openPos >= OPEN_POS_MAX) {
					openPos = OPEN_POS_MAX;
					dir = 0;
				}
			}
		} else if (dir < 0) {
			if (openPos < OPEN_POS_PASSABLE) {
				if ((dir == -1) && ((state.passableMap[y][x] & Level.PASSABLE_MASK_DOOR) != 0)) {
					dir = 1;
					lastTime = elapsedTime;
				} else {
					dir = -2;
					state.passableMap[y][x] |= Level.PASSABLE_IS_DOOR;
				}

				if (openPos <= 0.0f) {
					state.wallsMap[y][x] = (vert ? -1 : -2); // mark door for PortalTracer
					openPos = 0.0f;
					dir = 0;
				}
			}
		}
	}
}
