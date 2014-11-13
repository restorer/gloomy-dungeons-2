package zame.game.engine;

import android.util.FloatMath;
import java.io.IOException;
import java.util.Locale;
import zame.game.Common;
import zame.game.MyApplication;

public class HeroControllerAuto extends HeroController {
	public static class PathPoint {
		public float x;
		public float y;
	}

	public static final int MAX_POSITION = 1000;
	public static final float MIN_DIST = 0.1f;
	public static final float WALK_NORMAL_ACC = 0.075f;
	public static final float WALK_SHOOT_ACC = 0.025f;

	public static final float[] ANG_TAB = new float[] {
		135.0f, 90.0f,  45.0f,
		180.0f, 0.0f,   0.0f,
		225.0f, 270.0f, 315.0f,
	};

	protected Engine engine;
	protected State state;
	protected Weapons weapons;
	protected Game game;
	protected PathPoint[] path = new PathPoint[MAX_POSITION];
	protected int pathCount = 0;
	protected float maxDistSq = 0.0f;
	protected float checkedDistSq = 0.0f;
	protected Monster lockedOnMonster = null;

	public HeroControllerAuto() {
		for (int i = 0; i < MAX_POSITION; i++) {
			path[i] = new PathPoint();
		}
	}

	@Override
	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.weapons = engine.weapons;
		this.game = engine.game;
	}

	@Override
	public void updateAfterLoadOrCreate() {
		pathCount = 0;
		byte[] data;

		try {
			data = Common.readBytes(MyApplication.self.getAssets().open(
				String.format(Locale.US, "solved/%s.slv", engine.state.levelName)
			));
		} catch (IOException ex) {
			Common.log(ex);
			return;
		}

		// first 2 bytes is level version, skip them
		while (pathCount < MAX_POSITION && (pathCount * 2 + 3) < data.length) {
			path[pathCount].x = (float)((int)data[pathCount * 2 + 2] & 0x000000FF) + 0.5f;
			path[pathCount].y = (float)((int)data[pathCount * 2 + 3] & 0x000000FF) + 0.5f;
			pathCount++;
		}
	}

	protected float getAngle(float x, float y, float nx, float ny) {
		int dx = (Math.abs(nx - x) < MIN_DIST ? 0 : (nx > x ? 1 : -1));
		int dy = (Math.abs(ny - y) < MIN_DIST ? 0 : (ny > y ? 1 : -1));

		if (dx == 0 && dy == 0) {
			return state.heroA;
		}

		return ANG_TAB[(dy + 1) * 3 + dx + 1];
	}

	protected boolean canHitMonster(Monster mon, float maxDistSqToCheck) {
		if (mon.health <= 0) {
			return false;
		}

		float dx = mon.x - state.heroX;
		float dy = mon.y - state.heroY;
		checkedDistSq = dx*dx + dy*dy;

		if (checkedDistSq > maxDistSqToCheck) {
			return false;
		}

		return engine.traceLine(state.heroX, state.heroY, mon.x, mon.y, Level.PASSABLE_MASK_BULLET_HERO);
	}

	protected boolean rotateToAngle(float angleToRotate) {
		float angleDiff = angleToRotate - state.heroA;

		if (angleDiff > 180) {
			angleDiff -= 360;
		} else if (angleDiff < -180) {
			angleDiff += 360;
		}

		angleDiff /= 15.0f;
		float absAngleDiff = Math.abs(angleDiff);

		if (absAngleDiff <= 0.5f) {
			state.setHeroA(angleToRotate);
		} else {
			state.setHeroA(state.heroA + angleDiff);
		}

		return (absAngleDiff <= 2.0f);
	}

	@Override
	public void updateHero() {
		if (state.wpPathIdx >= pathCount) {
			String nextLevelName = engine.profile.getLevel(state.levelName).getNextLevelName();

			if (!engine.level.exists(nextLevelName)) {
				nextLevelName = State.LEVEL_INITIAL;
			}

			state.levelName = nextLevelName;
			game.loadLevel(Game.LOAD_LEVEL_RELOAD);
			return;
		}

		float maxDist = (weapons.currentParams.isNear ? 1.35f : 10.0f);
		maxDistSq = maxDist * maxDist;

		if (lockedOnMonster != null && !canHitMonster(lockedOnMonster, maxDistSq)) {
			lockedOnMonster = null;
		}

		if (lockedOnMonster == null) {
			float lockedOnDistSq = maxDistSq;

			for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
				if (canHitMonster(mon, lockedOnDistSq)) {
					lockedOnMonster = mon;
					lockedOnDistSq = checkedDistSq;
				}
			}
		}

		game.actionFire = 0;

		if (lockedOnMonster != null) {
			float dx = lockedOnMonster.x - state.heroX;
			float dy = lockedOnMonster.y - state.heroY;
			float angleToRotate = GameMath.getAngle(dx, dy) * GameMath.RAD2G_F;

			if (rotateToAngle(angleToRotate)) {
				game.actionFire = 1;
			}
		} else if (state.wpPathIdx + 1 < pathCount) {
			float angleToRotate = getAngle(
				path[state.wpPathIdx].x,
				path[state.wpPathIdx].y,
				path[state.wpPathIdx + 1].x,
				path[state.wpPathIdx + 1].y
			);

			rotateToAngle(angleToRotate);
		}

		float px = path[state.wpPathIdx].x;
		float py = path[state.wpPathIdx].y;

		float angle = getAngle(state.heroX, state.heroY, px, py);
		float ar = angle * GameMath.G2RAD_F;
		float cs = FloatMath.cos(ar);
		float sn = FloatMath.sin(ar);
		boolean positionUpdated = game.updateHeroPosition(cs, -sn, (game.actionFire == 0 ? WALK_NORMAL_ACC : WALK_SHOOT_ACC ));

		if ((int)px < 0 || (int)py < 0 || (int)px >= state.levelWidth || (int)py >= state.levelHeight) {
			state.wpPathIdx++;
		} else if (!positionUpdated && ((state.passableMap[(int)py][(int)px] & Level.PASSABLE_IS_WALL) != 0)) {
			state.wpPathIdx++;
		} else {
			float dx = Math.abs(px - state.heroX);
			float dy = Math.abs(py - state.heroY);

			if (dx < MIN_DIST && dy < MIN_DIST) {
				state.wpPathIdx++;
			}
		}
	}
}
