package zame.game.engine;

import android.util.FloatMath;
import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.data.DataList;
import zame.game.store.Profile;
import zame.game.store.Store;

public class LevelRenderer implements EngineObject {
	protected static final int MAX_LIGHT_TAB = 1000;
	protected static final int LAST_LIGHT_TAB = MAX_LIGHT_TAB - 1;
	protected static final float LIGHT_TAB_MULT = MAX_LIGHT_TAB / 10f;

	protected static final int AUTO_WALL_TYPE_WALL = 0;
	protected static final int AUTO_WALL_TYPE_TRANSP = 1;
	protected static final int AUTO_WALL_TYPE_DOOR = 2;

	protected static final int AUTO_WALL_MASK_HORIZONTAL = 1;
	protected static final int AUTO_WALL_MASK_VERTICAL = 2;
	protected static final int AUTO_WALL_MASK_DOOR = 4;

	public static final int MAX_AUTO_WALLS = Level.MAX_WIDTH * Level.MAX_HEIGHT * 2;
	public static final int MAX_AW_CELLS = Level.MAX_WIDTH * Level.MAX_HEIGHT;
	public static final float HALF_WALL = 0.4f;
	public static final float HALF_WALL_PLUS_EXTRUDE = HALF_WALL + 0.05f;
	public static final float HALF_DOOR_WIDTH = 0.025f;
	public static final float WALL_HGT = HALF_WALL * 2.0f;
	public static final float LIGHT_OBJECT = 1.0f;
	public static final float LIGHT_HIGHLIGHT = 1.0f;
	public static final float MONSTER_SIZE_MULT = 0.75f;
	public static final float EXTRUDE_LIGHT_MULT = 0.5f;

	public PortalTracer tracer = new PortalTracer();
	public boolean showMonstersOnMap = false;
	public boolean[][] awTouchedCellsMap = new boolean[Level.MAX_HEIGHT][Level.MAX_WIDTH];
	public DataList<TraceInfo> tracesInfo = new DataList<TraceInfo>(TraceInfo.class, Level.MAX_BULLETS * 3);

	protected Engine engine;
	protected State state;
	protected Renderer renderer;
	protected Level level;
	protected Config config;
	protected TextureLoader textureLoader;
	protected Profile profile;
	protected float flatObjDx;
	protected float flatObjDy;
	protected float[] lightTab = new float[MAX_LIGHT_TAB];
	protected float currentHeroX;
	protected float currentHeroY;
	protected float currentHeroA;
	protected float currentHeroAr;
	protected float currentHeroCs;
	protected float currentHeroSn;
	protected float[][] lightMap = new float[Level.MAX_HEIGHT * 2 + 1][Level.MAX_WIDTH * 2 + 1];
	protected float[][] tmpLights = new float[Level.MAX_HEIGHT][Level.MAX_WIDTH];

	public LevelRenderer() {
		final float ambient = 0.5f;
		final float heroLightness = 0.2f;

		for (int i = 0; i < MAX_LIGHT_TAB; i++) {
			lightTab[i] = FloatMath.sin(((float)i / (float)MAX_LIGHT_TAB) * GameMath.PI_F + GameMath.PI_D2F) * heroLightness + ambient;
		}
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.renderer = engine.renderer;
		this.level = engine.level;
		this.config = engine.config;
		this.textureLoader = engine.textureLoader;
		this.profile = engine.profile;

		tracer.setEngine(engine);
	}

	public void updateAfterLoadOrCreate() {
		for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
			if (aw.doorUid >= 0) {
				for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
					if (door.uid == aw.doorUid) {
						aw.door = door;
						break;
					}
				}

				if (aw.door == null) {
					aw.doorUid = -1;
				}
			}
		}

		for (Bullet bullet = state.bullets.first(); bullet != null; bullet = (Bullet)bullet.next) {
			if (bullet.monUid >= 0) {
				for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
					if (mon.uid == bullet.monUid) {
						bullet.mon = mon;
						break;
					}
				}
			}
		}

		for (Explosion explosion = state.explosions.first(); explosion != null; explosion = (Explosion)explosion.next) {
			if (explosion.monUid >= 0) {
				for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
					if (mon.uid == explosion.monUid) {
						explosion.mon = mon;
						break;
					}
				}
			}
		}

		for (int i = 0, levelHeight = state.levelHeight; i < levelHeight; i++) {
			for (int j = 0, levelWidth = state.levelWidth; j < levelWidth; j++) {
				awTouchedCellsMap[i][j] = false;
			}
		}

		for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
			awTouchedCellsMap[tc.y][tc.x] = true;
		}

		for (int ly = 0, maxLy = state.levelHeight * 2 + 1; ly < maxLy; ly++) {
			for (int lx = 0, maxLx = state.levelWidth * 2 + 1; lx < maxLx; lx++) {
				lightMap[ly][lx] = 0.0f;
			}
		}

		for (int y = 0, levelHeight = state.levelHeight; y < levelHeight; y++) {
			for (int x = 0, levelWidth = state.levelWidth; x < levelWidth; x++) {
				modLightMap(x, y, getLightMapValue(x, y));
			}
		}
	}

	public float getLightMapValue(int x, int y) {
		float value = 0.0f;
		int pass = state.passableMap[y][x];

		if (level.doorsMap[y][x] != null) {
			value += 0.5f;
		} else if (((pass & Level.PASSABLE_IS_DECOR_LAMP) != 0) || ((pass & Level.PASSABLE_IS_OBJECT) != 0)) {
			value += LIGHT_OBJECT;
		} else if ((pass & Level.PASSABLE_IS_WALL) != 0) {
			int tex = state.wallsMap[y][x];

			for (int i = 0, len = TextureLoader.WALL_LIGHTS.length; i < len; i++) {
				if (TextureLoader.WALL_LIGHTS[i] == tex) {
					value += 1.0f;
					break;
				}
			}
		} else if ((pass & Level.PASSABLE_IS_DECOR_ITEM) != 0) {
			int tex = state.texMap[y][x];

			for (int i = 0, len = TextureLoader.DITEM_LIGHTS.length; i < len; i++) {
				if (TextureLoader.DITEM_LIGHTS[i] == tex) {
					value += 1.0f;
					break;
				}
			}
		}

		int ceil1 = state.ceilMap1[y][x];
		int ceil2 = state.ceilMap2[y][x];
		int ceil3 = state.ceilMap3[y][x];
		int ceil4 = state.ceilMap4[y][x];

		for (int i = 0, len = TextureLoader.CEIL_LIGHTS.length; i < len; i++) {
			int cl = TextureLoader.CEIL_LIGHTS[i];

			if (ceil1 == cl || ceil2 == cl || ceil3 == cl || ceil4 == cl) {
				value += 1.0f;
				break;
			}
		}

		if (state.arrowsMap[y][x] != 0) {
			value += 1.0f;
		}

		return value;
	}

	public void modLightMap(int cx, int cy, float val) {
		int lx = cx * 2;
		int ly = cy * 2;

		lightMap[ly][lx] += val;
		lightMap[ly][lx + 1] += val;
		lightMap[ly][lx + 2] += val;

		ly++;
		lightMap[ly][lx] += val;
		lightMap[ly][lx + 1] += val;
		lightMap[ly][lx + 2] += val;

		ly++;
		lightMap[ly][lx] += val;
		lightMap[ly][lx + 1] += val;
		lightMap[ly][lx + 2] += val;
	}

	protected void updateDoors(long elapsedTime) {
		for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
			if (door.dir != 0) {
				if (door.dir > 0) {
					door.openPos = (float)(elapsedTime - door.lastTime) / 300.0f;
				} else {
					door.openPos = Door.OPEN_POS_MAX - (float)(elapsedTime - door.lastTime) / 200.0f;
				}

				door.update(elapsedTime);
			}
		}
	}

	public float getLightness(float x, float y) {
		int d = (int)(((x - currentHeroX) * currentHeroCs - (y - currentHeroY) * currentHeroSn - 0.5f) * LIGHT_TAB_MULT);

		try {
			return lightTab[d < 0 ? 0 : (d > LAST_LIGHT_TAB ? LAST_LIGHT_TAB : d)] + lightMap[(int)((y + 0.25f) * 2.0f)][(int)((x + 0.25f) * 2.0f)];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return 0.0f;
		}
	}

	public void setObjLighting(float x, float y) {
		float l = getLightness(x, y);

		renderer.r1 = l; renderer.g1 = l; renderer.b1 = l;
		renderer.r2 = l; renderer.g2 = l; renderer.b2 = l;
		renderer.r3 = l; renderer.g3 = l; renderer.b3 = l;
		renderer.r4 = l; renderer.g4 = l; renderer.b4 = l;
	}

	public void setWallLighting(float fromX, float fromY, float toX, float toY, boolean vert) {
		int ang = ((int)currentHeroA + (vert ? 0 : 270)) % 360;

		if (ang > 90) {
			if (ang < 180) {
				ang = 180 - ang;
			} else if (ang < 270) {
				ang = ang - 180;
			} else {
				ang = 360 - ang;
			}
		}

		float l = 1.0f - 0.5f * (float)ang / 90.0f;

		float l1 = getLightness(fromX, fromY) * l;
		renderer.r1 = l1; renderer.g1 = l1; renderer.b1 = l1;
		renderer.r2 = l1; renderer.g2 = l1; renderer.b2 = l1;

		float l2 = getLightness(toX, toY) * l;
		renderer.r3 = l2; renderer.g3 = l2; renderer.b3 = l2;
		renderer.r4 = l2; renderer.g4 = l2; renderer.b4 = l2;
	}

	// This method:
	// did *not* check for available space (MAX_AUTO_WALLS),
	// did *not* check if wall already exists,
	// did *not* append wall mask,
	// did *not* add doors
	public void appendAutoWall(int fromX, int fromY, int toX, int toY, int type) {
		AutoWall foundAw = null;
		boolean vert = (fromX == toX);

		for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
			if ((aw.door != null) || (aw.vert != vert) || (aw.type != type)) {
				continue;
			}

			if ((int)aw.fromX == fromX && (int)aw.fromY == fromY) {
				aw.fromX = (float)toX;
				aw.fromY = (float)toY;
				foundAw = aw;
				break;
			} else if ((int)aw.toX == fromX && (int)aw.toY == fromY) {
				aw.toX = (float)toX;
				aw.toY = (float)toY;
				foundAw = aw;
				break;
			} else if ((int)aw.fromX == toX && (int)aw.fromY == toY) {
				aw.fromX = (float)fromX;
				aw.fromY = (float)fromY;
				foundAw = aw;
				break;
			} else if ((int)aw.toX == toX && (int)aw.toY == toY) {
				aw.toX = (float)fromX;
				aw.toY = (float)fromY;
				foundAw = aw;
				break;
			}
		}

		if (foundAw == null) {
			AutoWall aw = state.autoWalls.take();

			aw.fromX = (float)fromX;
			aw.fromY = (float)fromY;
			aw.toX = (float)toX;
			aw.toY = (float)toY;
			aw.vert = vert;
			aw.type = type;
			aw.doorUid = -1;
			aw.door = null;

			return;
		}

		for (;;) {
			AutoWall nextFoundAw = null;

			for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
				if ((aw == foundAw) || (aw.door != null) || (aw.vert != foundAw.vert) || (aw.type != foundAw.type)) {
					continue;
				}

				if ((int)aw.fromX == foundAw.fromX && (int)aw.fromY == foundAw.fromY) {
					aw.fromX = foundAw.toX;
					aw.fromY = foundAw.toY;
					nextFoundAw = aw;
					break;
				} else if ((int)aw.toX == foundAw.fromX && (int)aw.toY == foundAw.fromY) {
					aw.toX = foundAw.toX;
					aw.toY = foundAw.toY;
					nextFoundAw = aw;
					break;
				} else if ((int)aw.fromX == foundAw.toX && (int)aw.fromY == foundAw.toY) {
					aw.fromX = foundAw.fromX;
					aw.fromY = foundAw.fromY;
					nextFoundAw = aw;
					break;
				} else if ((int)aw.toX == foundAw.toX && (int)aw.toY == foundAw.toY) {
					aw.toX = foundAw.fromX;
					aw.toY = foundAw.fromY;
					nextFoundAw = aw;
					break;
				}
			}

			if (nextFoundAw == null) {
				break;
			}

			state.autoWalls.release(foundAw);
			foundAw = nextFoundAw;
		}
	}

	public void renderLevel() {
		renderer.z1 = -HALF_WALL;
		renderer.z2 = HALF_WALL;
		renderer.z3 = HALF_WALL;
		renderer.z4 = -HALF_WALL;

		PortalTracer.Wall[] localWalls = tracer.walls;
		Door[][] localDoorsMap = level.doorsMap;
		int[][] localDrawedAutoWalls = state.drawedAutoWalls;

		for (int i = 0, len = tracer.wallsCount; i < len; i++) {
			int autoWallMask;
			Door door;
			PortalTracer.Wall wall = localWalls[i];

			if (wall.fromX == wall.toX) {
				door = (wall.fromY < wall.toY ? localDoorsMap[wall.fromY][wall.fromX - 1] : localDoorsMap[wall.toY][wall.fromX]);
				autoWallMask = AUTO_WALL_MASK_VERTICAL;
			} else {
				door = (wall.fromX < wall.toX ? localDoorsMap[wall.fromY][wall.fromX] : localDoorsMap[wall.fromY - 1][wall.toX]);
				autoWallMask = AUTO_WALL_MASK_HORIZONTAL;
			}

			// by the way, mx and my *not* always equal to wall.cellX and wall.cellY
			int mx = (wall.fromX < wall.toX ? wall.fromX : wall.toX);
			int my = (wall.fromY < wall.toY ? wall.fromY : wall.toY);

			if (((localDrawedAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
				localDrawedAutoWalls[my][mx] |= autoWallMask;
				appendAutoWall(wall.fromX, wall.fromY, wall.toX, wall.toY, AUTO_WALL_TYPE_WALL);
			}

			renderer.x1 = (float)wall.fromX; renderer.y1 = -(float)wall.fromY;
			renderer.x2 = (float)wall.fromX; renderer.y2 = -(float)wall.fromY;
			renderer.x3 = (float)wall.toX; renderer.y3 = -(float)wall.toY;
			renderer.x4 = (float)wall.toX; renderer.y4 = -(float)wall.toY;

			setWallLighting((float)wall.fromX, (float)wall.fromY, (float)wall.toX, (float)wall.toY, (wall.fromX == wall.toX));

			if (door != null) {
				renderer.drawQuad(door.texture + TextureLoader.BASE_DOORS_S);
			} else if (wall.flipTexture) {
				renderer.drawQuadFlipLR(wall.texture);
			} else {
				renderer.drawQuad(wall.texture);
			}
		}

		TouchedCell[] localTouchedCells = tracer.touchedCells;

		for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
			TouchedCell tc = localTouchedCells[i];

			if (!awTouchedCellsMap[tc.y][tc.x] && state.awTouchedCells.canTake()) {
				awTouchedCellsMap[tc.y][tc.x] = true;
				state.awTouchedCells.take().copyFrom(tc);
			}
		}
	}

	protected void renderDoors() {
		TouchedCell[] localTouchedCells = tracer.touchedCells;
		Door[][] localDoorsMap = level.doorsMap;
		int[][] localDrawedAutoWalls = state.drawedAutoWalls;

		for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
			TouchedCell tc = localTouchedCells[i];
			Door door = localDoorsMap[tc.y][tc.x];

			if (door == null) {
				continue;
			}

			float fromX, fromY, toX, toY;
			float fromX1, fromY1, toX1, toY1;
			float fromX2, fromY2, toX2, toY2;

			if (door.vert) {
				fromX = (float)door.x + 0.5f;
				toX = fromX;
				fromY = (float)door.y;
				toY = fromY + 1.0f;
			} else {
				fromX = (float)door.x;
				toX = fromX + 1.0f;
				fromY = (float)door.y + 0.5f;
				toY = fromY;
			}

			if (((localDrawedAutoWalls[door.y][door.x] & AUTO_WALL_MASK_DOOR) == 0) && state.autoWalls.canTake()) {
				localDrawedAutoWalls[door.y][door.x] |= AUTO_WALL_MASK_DOOR;
				AutoWall aw = state.autoWalls.take();

				aw.fromX = fromX;
				aw.fromY = fromY;
				aw.toX = toX;
				aw.toY = toY;
				aw.vert = door.vert;
				aw.type = AUTO_WALL_TYPE_DOOR;
				aw.doorUid = door.uid;
				aw.door = door;
			}

			if (door.vert) {
				fromY += door.openPos;
				toY += door.openPos;

				fromX1 = fromX - HALF_DOOR_WIDTH;
				toX1 = fromX1;
				fromY1 = -fromY;
				toY1 = -toY;

				fromX2 = fromX + HALF_DOOR_WIDTH;
				toX2 = fromX2;
				fromY2 = -fromY;
				toY2 = -toY;
			} else {
				fromX += door.openPos;
				toX += door.openPos;

				fromX1 = fromX;
				toX1 = toX;
				fromY1 = -(fromY - HALF_DOOR_WIDTH);
				toY1 = fromY1;

				fromX2 = fromX;
				toX2 = toX;
				fromY2 = -(fromY + HALF_DOOR_WIDTH);
				toY2 = fromY2;
			}

			setWallLighting(fromX, fromY, toX, toY, door.vert);

			renderer.x1 = fromX1; renderer.y1 = fromY1;
			renderer.x2 = fromX1; renderer.y2 = fromY1;
			renderer.x3 = toX1; renderer.y3 = toY1;
			renderer.x4 = toX1; renderer.y4 = toY1;
			renderer.drawQuad(door.texture + TextureLoader.BASE_DOORS_F);

			renderer.x1 = fromX2; renderer.y1 = fromY2;
			renderer.x2 = fromX2; renderer.y2 = fromY2;
			renderer.x3 = toX2; renderer.y3 = toY2;
			renderer.x4 = toX2; renderer.y4 = toY2;
			renderer.drawQuad(door.texture + TextureLoader.BASE_DOORS_F);

			setWallLighting(fromX1, -fromY1, fromX2, -fromY2, !door.vert);

			renderer.r1 *= EXTRUDE_LIGHT_MULT; renderer.g1 *= EXTRUDE_LIGHT_MULT; renderer.b1 *= EXTRUDE_LIGHT_MULT;
			renderer.r2 *= EXTRUDE_LIGHT_MULT; renderer.g2 *= EXTRUDE_LIGHT_MULT; renderer.b2 *= EXTRUDE_LIGHT_MULT;
			renderer.r3 *= EXTRUDE_LIGHT_MULT; renderer.g3 *= EXTRUDE_LIGHT_MULT; renderer.b3 *= EXTRUDE_LIGHT_MULT;
			renderer.r4 *= EXTRUDE_LIGHT_MULT; renderer.g4 *= EXTRUDE_LIGHT_MULT; renderer.b4 *= EXTRUDE_LIGHT_MULT;

			renderer.x1 = fromX1; renderer.y1 = fromY1;
			renderer.x2 = fromX1; renderer.y2 = fromY1;
			renderer.x3 = fromX2; renderer.y3 = fromY2;
			renderer.x4 = fromX2; renderer.y4 = fromY2;
			renderer.drawQuad4PX(door.texture + TextureLoader.BASE_DOORS_S);
		}

		renderer.z1 = -HALF_WALL;
		renderer.z2 = HALF_WALL;
		renderer.z3 = HALF_WALL;
		renderer.z4 = -HALF_WALL;
	}

	// render objects, decorations and transparents
	protected void renderObjects() {
		TouchedCell[] localTouchedCells = tracer.touchedCells;
		int[][] localPassableMap = state.passableMap;
		int[][] localTexMap = state.texMap;
		int[][] localDrawedAutoWalls = state.drawedAutoWalls;

		for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
			TouchedCell tc = localTouchedCells[i];
			int pass = localPassableMap[tc.y][tc.x];
			int tex = localTexMap[tc.y][tc.x];

			if ((pass & Level.PASSABLE_MASK_OBJ_OR_DECOR) != 0) {
				float mx = (float)tc.x + 0.5f;
				float my = (float)tc.y + 0.5f;

				float fromX = mx + flatObjDy;
				float toX = mx - flatObjDy;
				float fromY = my - flatObjDx;
				float toY = my + flatObjDx;

				renderer.x1 = fromX; renderer.y1 = -fromY;
				renderer.x2 = fromX; renderer.y2 = -fromY;
				renderer.x3 = toX; renderer.y3 = -toY;
				renderer.x4 = toX; renderer.y4 = -toY;

				setObjLighting(mx, my);

				if ((pass & Level.PASSABLE_IS_OBJECT) != 0) {
					renderer.drawQuad(state.objectsMap[tc.y][tc.x]);
				}

				if ((pass & Level.PASSABLE_MASK_DECORATION) != 0) {
					renderer.drawQuad(tex);
				}
			}

			if ((pass & Level.PASSABLE_IS_TRANSP_WINDOW) != 0) {
				float fromX, fromY, toX, toY;
				boolean vert = ((pass & Level.PASSABLE_IS_TRANSP_WINDOW_VERT) != 0);

				if (vert) {
					fromX = (float)tc.x + 0.5f;
					toX = fromX;
					fromY = (float)tc.y;
					toY = fromY + 1.0f;
				} else {
					fromX = (float)tc.x;
					toX = fromX + 1.0f;
					fromY = (float)tc.y + 0.5f;
					toY = fromY;
				}

				if (((localDrawedAutoWalls[tc.y][tc.x] & AUTO_WALL_MASK_DOOR) == 0) && state.autoWalls.canTake()) {
					localDrawedAutoWalls[tc.y][tc.x] |= AUTO_WALL_MASK_DOOR;
					AutoWall aw = state.autoWalls.take();

					aw.fromX = fromX;
					aw.fromY = fromY;
					aw.toX = toX;
					aw.toY = toY;
					aw.vert = vert;
					aw.type = AUTO_WALL_TYPE_TRANSP;
					aw.doorUid = -1;
					aw.door = null;
				}

				renderer.x1 = fromX; renderer.y1 = -fromY;
				renderer.x2 = fromX; renderer.y2 = -fromY;
				renderer.x3 = toX; renderer.y3 = -toY;
				renderer.x4 = toX; renderer.y4 = -toY;

				setWallLighting(fromX, fromY, toX, toY, vert);
				renderer.drawQuad(tex);
			} else if (
				((pass & Level.PASSABLE_IS_TRANSP) != 0) &&
				((pass & Level.PASSABLE_IS_NOTRANS) == 0) &&
				(tex != 0)
			) {
				for (int s = 0; s < 4; s++) {
					if ((
						localPassableMap[tc.y + PortalTracer.Y_CELL_ADD[s]][tc.x + PortalTracer.X_CELL_ADD[s]] &
						Level.PASSABLE_MASK_WALL_N_TRANSP
					) == 0) {
						int fromX = tc.x + PortalTracer.X_ADD[s];
						int fromY = tc.y + PortalTracer.Y_ADD[s];
						int toX = tc.x + PortalTracer.X_ADD[(s + 1) % 4];
						int toY = tc.y + PortalTracer.Y_ADD[(s + 1) % 4];

						renderer.x1 = (float)fromX; renderer.y1 = -(float)fromY;
						renderer.x2 = (float)fromX; renderer.y2 = -(float)fromY;
						renderer.x3 = (float)toX; renderer.y3 = -(float)toY;
						renderer.x4 = (float)toX; renderer.y4 = -(float)toY;

						int mx = (fromX < toX ? fromX : toX);
						int my = (fromY < toY ? fromY : toY);
						int autoWallMask = ((s == 1 || s == 3) ? AUTO_WALL_MASK_VERTICAL : AUTO_WALL_MASK_HORIZONTAL);

						if (((localDrawedAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
							localDrawedAutoWalls[my][mx] |= autoWallMask;
							appendAutoWall(fromX, fromY, toX, toY, AUTO_WALL_TYPE_TRANSP);
						}

						setWallLighting((float)fromX, (float)fromY, (float)toX, (float)toY, (s == 1 || s == 3));

						if (s == 0 || s == 3) {
							renderer.drawQuadFlipLR(tex);
						} else {
							renderer.drawQuad(tex);
						}
					}
				}
			}
		}
	}

	protected void renderMonsters(long elapsedTime, boolean deadCorpses, int texmap) {
		renderer.z1 = -HALF_WALL;
		renderer.z2 = HALF_WALL * MONSTER_SIZE_MULT;
		renderer.z3 = HALF_WALL * MONSTER_SIZE_MULT;
		renderer.z4 = -HALF_WALL;

		boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

		for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
			if ((mon.texmap != texmap) ||
				(deadCorpses && mon.health > 0) ||
				(!deadCorpses && mon.health <= 0) ||
				!(localTouchedCellsMap[mon.prevY][mon.prevX] || localTouchedCellsMap[mon.cellY][mon.cellX])
			) {
				continue;
			}

			float fromX = mon.x + flatObjDy * MONSTER_SIZE_MULT;
			float toX = mon.x - flatObjDy * MONSTER_SIZE_MULT;
			float fromY = mon.y - flatObjDx * MONSTER_SIZE_MULT;
			float toY = mon.y + flatObjDx * MONSTER_SIZE_MULT;

			int tex = mon.texture;
			setObjLighting(mon.x, mon.y);

			renderer.x1 = fromX; renderer.y1 = -fromY;
			renderer.x2 = fromX; renderer.y2 = -fromY;
			renderer.x3 = toX; renderer.y3 = -toY;
			renderer.x4 = toX; renderer.y4 = -toY;

			if (mon.health > 0) {
				if ((mon.hitTimeout <= 0) && (mon.attackTimeout > 0)) {
					tex += 15;
				} else {
					if (mon.isAimedOnHero) {
						tex += 2;
					} else if (mon.chaseMode) {
						tex += ((((int)currentHeroA + 360 + 45 - mon.dir * 90) % 360) / 90);
					}

					if (mon.hitTimeout > 0) {
						tex += 8;
					} else if (!mon.isInAttackState && ((elapsedTime % 800) > 400)) {
						tex += 4;
					}
				}
			} else {
				if (mon.dieTime == 0) {
					mon.dieTime = elapsedTime;
				}

				tex += 12 + (mon.dieTime < 0 ? 2 : Math.min(2, (elapsedTime - mon.dieTime) / 150));
			}

			renderer.drawQuadMon(tex);
		}
	}

	protected void renderFloorArrows() {
		renderer.z1 = -HALF_WALL + GameMath.INFINITY;
		renderer.z2 = -HALF_WALL + GameMath.INFINITY;
		renderer.z3 = -HALF_WALL + GameMath.INFINITY;
		renderer.z4 = -HALF_WALL + GameMath.INFINITY;

		TouchedCell[] localTouchedCells = tracer.touchedCells;
		int[][] localWallsMap = state.wallsMap;
		int[][] localArrowsMap = state.arrowsMap;

		for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
			TouchedCell tc = localTouchedCells[i];
			int arrowTex = localArrowsMap[tc.y][tc.x];

			if ((localWallsMap[tc.y][tc.x] > 0) || (arrowTex == 0)) {
				continue;
			}

			float fx = (float)tc.x;
			float tx = (float)(tc.x + 1);
			float fy = (float)tc.y;
			float ty = (float)(tc.y + 1);

			float lff = getLightness(fx, fy);
			float lft = getLightness(fx, ty);
			float ltf = getLightness(tx, fy);
			float ltt = getLightness(tx, ty);

			renderer.r1 = lft; renderer.g1 = lft; renderer.b1 = lft;
			renderer.r2 = lff; renderer.g2 = lff; renderer.b2 = lff;
			renderer.r3 = ltf; renderer.g3 = ltf; renderer.b3 = ltf;
			renderer.r4 = ltt; renderer.g4 = ltt; renderer.b4 = ltt;

			renderer.x1 = fx; renderer.y1 = -ty;
			renderer.x2 = fx; renderer.y2 = -fy;
			renderer.x3 = tx; renderer.y3 = -fy;
			renderer.x4 = tx; renderer.y4 = -ty;

			renderer.drawQuad(arrowTex);
		}
	}

	protected void renderFloorAndCeil() {
		TouchedCell[] localTouchedCells = tracer.touchedCells;
		int[][] localWallsMap = state.wallsMap;
		int[][] localFloorMap1 = state.floorMap1;
		int[][] localFloorMap2 = state.floorMap2;
		int[][] localFloorMap3 = state.floorMap3;
		int[][] localFloorMap4 = state.floorMap4;
		int[][] localCeilMap1 = state.ceilMap1;
		int[][] localCeilMap2 = state.ceilMap2;
		int[][] localCeilMap3 = state.ceilMap3;
		int[][] localCeilMap4 = state.ceilMap4;

		for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
			TouchedCell tc = localTouchedCells[i];

			// calc params

			int floorTex1 = localFloorMap1[tc.y][tc.x];
			int floorTex2 = localFloorMap2[tc.y][tc.x];
			int floorTex3 = localFloorMap3[tc.y][tc.x];
			int floorTex4 = localFloorMap4[tc.y][tc.x];
			int ceilTex1 = localCeilMap1[tc.y][tc.x];
			int ceilTex2 = localCeilMap2[tc.y][tc.x];
			int ceilTex3 = localCeilMap3[tc.y][tc.x];
			int ceilTex4 = localCeilMap4[tc.y][tc.x];

			float fx = (float)tc.x;
			float tx = (float)(tc.x + 1);
			float fy = (float)tc.y;
			float ty = (float)(tc.y + 1);
			float mx = fx + 0.5f;
			float my = fy + 0.5f;

			float lff = getLightness(fx, fy);
			float lft = getLightness(fx, ty);
			float ltf = getLightness(tx, fy);
			float ltt = getLightness(tx, ty);
			float lfm = getLightness(fx, my);
			float lmf = getLightness(mx, fy);
			float lmm = getLightness(mx, my);
			float lmt = getLightness(mx, ty);
			float ltm = getLightness(tx, my);

			fy = -fy;
			ty = -ty;
			my = -my;

			if (localWallsMap[tc.y][tc.x] <= 0) {
				// render floor

				renderer.z1 = -HALF_WALL;
				renderer.z2 = -HALF_WALL;
				renderer.z3 = -HALF_WALL;
				renderer.z4 = -HALF_WALL;

				if (floorTex1 == floorTex2 && floorTex1 == floorTex3 && floorTex1 == floorTex4) {
					if (floorTex1 != 0) {
						renderer.r1 = lft; renderer.g1 = lft; renderer.b1 = lft;
						renderer.r2 = lff; renderer.g2 = lff; renderer.b2 = lff;
						renderer.r3 = ltf; renderer.g3 = ltf; renderer.b3 = ltf;
						renderer.r4 = ltt; renderer.g4 = ltt; renderer.b4 = ltt;

						renderer.x1 = fx; renderer.y1 = ty;
						renderer.x2 = fx; renderer.y2 = fy;
						renderer.x3 = tx; renderer.y3 = fy;
						renderer.x4 = tx; renderer.y4 = ty;

						renderer.drawQuad(floorTex1);
					}
				} else {
					if (floorTex1 != 0) {
						renderer.r1 = lfm; renderer.g1 = lfm; renderer.b1 = lfm;
						renderer.r2 = lff; renderer.g2 = lff; renderer.b2 = lff;
						renderer.r3 = lmf; renderer.g3 = lmf; renderer.b3 = lmf;
						renderer.r4 = lmm; renderer.g4 = lmm; renderer.b4 = lmm;

						renderer.x1 = fx; renderer.y1 = my;
						renderer.x2 = fx; renderer.y2 = fy;
						renderer.x3 = mx; renderer.y3 = fy;
						renderer.x4 = mx; renderer.y4 = my;

						renderer.drawQuad1(floorTex1);
					}

					if (floorTex2 != 0) {
						renderer.r1 = lmm; renderer.g1 = lmm; renderer.b1 = lmm;
						renderer.r2 = lmf; renderer.g2 = lmf; renderer.b2 = lmf;
						renderer.r3 = ltf; renderer.g3 = ltf; renderer.b3 = ltf;
						renderer.r4 = ltm; renderer.g4 = ltm; renderer.b4 = ltm;

						renderer.x1 = mx; renderer.y1 = my;
						renderer.x2 = mx; renderer.y2 = fy;
						renderer.x3 = tx; renderer.y3 = fy;
						renderer.x4 = tx; renderer.y4 = my;

						renderer.drawQuad2(floorTex2);
					}

					if (floorTex3 != 0) {
						renderer.r1 = lft; renderer.g1 = lft; renderer.b1 = lft;
						renderer.r2 = lfm; renderer.g2 = lfm; renderer.b2 = lfm;
						renderer.r3 = lmm; renderer.g3 = lmm; renderer.b3 = lmm;
						renderer.r4 = lmt; renderer.g4 = lmt; renderer.b4 = lmt;

						renderer.x1 = fx; renderer.y1 = ty;
						renderer.x2 = fx; renderer.y2 = my;
						renderer.x3 = mx; renderer.y3 = my;
						renderer.x4 = mx; renderer.y4 = ty;

						renderer.drawQuad3(floorTex3);
					}

					if (floorTex4 != 0) {
						renderer.r1 = lmt; renderer.g1 = lmt; renderer.b1 = lmt;
						renderer.r2 = lmm; renderer.g2 = lmm; renderer.b2 = lmm;
						renderer.r3 = ltm; renderer.g3 = ltm; renderer.b3 = ltm;
						renderer.r4 = ltt; renderer.g4 = ltt; renderer.b4 = ltt;

						renderer.x1 = mx; renderer.y1 = ty;
						renderer.x2 = mx; renderer.y2 = my;
						renderer.x3 = tx; renderer.y3 = my;
						renderer.x4 = tx; renderer.y4 = ty;

						renderer.drawQuad4(floorTex4);
					}
				}

				// render ceil

				renderer.z1 = HALF_WALL;
				renderer.z2 = HALF_WALL;
				renderer.z3 = HALF_WALL;
				renderer.z4 = HALF_WALL;

				if (ceilTex1 == ceilTex2 && ceilTex1 == ceilTex3 && ceilTex1 == ceilTex4) {
					if (ceilTex1 != 0) {
						renderer.r1 = ltt; renderer.g1 = ltt; renderer.b1 = ltt;
						renderer.r2 = ltf; renderer.g2 = ltf; renderer.b2 = ltf;
						renderer.r3 = lff; renderer.g3 = lff; renderer.b3 = lff;
						renderer.r4 = lft; renderer.g4 = lft; renderer.b4 = lft;

						renderer.x1 = tx; renderer.y1 = ty;
						renderer.x2 = tx; renderer.y2 = fy;
						renderer.x3 = fx; renderer.y3 = fy;
						renderer.x4 = fx; renderer.y4 = ty;

						renderer.drawQuadFlipLR(ceilTex1);
					}
				} else {
					if (ceilTex1 != 0) {
						renderer.r1 = lmm; renderer.g1 = lmm; renderer.b1 = lmm;
						renderer.r2 = lmf; renderer.g2 = lmf; renderer.b2 = lmf;
						renderer.r3 = lff; renderer.g3 = lff; renderer.b3 = lff;
						renderer.r4 = lmt; renderer.g4 = lmt; renderer.b4 = lmt;

						renderer.x1 = mx; renderer.y1 = my;
						renderer.x2 = mx; renderer.y2 = fy;
						renderer.x3 = fx; renderer.y3 = fy;
						renderer.x4 = fx; renderer.y4 = my;

						renderer.drawQuadFlipLR1(ceilTex1);
					}

					if (ceilTex2 != 0) {
						renderer.r1 = ltm; renderer.g1 = ltm; renderer.b1 = ltm;
						renderer.r2 = ltf; renderer.g2 = ltf; renderer.b2 = ltf;
						renderer.r3 = lmf; renderer.g3 = lmf; renderer.b3 = lmf;
						renderer.r4 = lmm; renderer.g4 = lmm; renderer.b4 = lmm;

						renderer.x1 = tx; renderer.y1 = my;
						renderer.x2 = tx; renderer.y2 = fy;
						renderer.x3 = mx; renderer.y3 = fy;
						renderer.x4 = mx; renderer.y4 = my;

						renderer.drawQuadFlipLR2(ceilTex2);
					}

					if (ceilTex3 != 0) {
						renderer.r1 = lmt; renderer.g1 = lmt; renderer.b1 = lmt;
						renderer.r2 = lmm; renderer.g2 = lmm; renderer.b2 = lmm;
						renderer.r3 = lfm; renderer.g3 = lfm; renderer.b3 = lfm;
						renderer.r4 = lft; renderer.g4 = lft; renderer.b4 = lft;

						renderer.x1 = mx; renderer.y1 = ty;
						renderer.x2 = mx; renderer.y2 = my;
						renderer.x3 = fx; renderer.y3 = my;
						renderer.x4 = fx; renderer.y4 = ty;

						renderer.drawQuadFlipLR3(ceilTex3);
					}

					if (ceilTex4 != 0) {
						renderer.r1 = ltt; renderer.g1 = ltt; renderer.b1 = ltt;
						renderer.r2 = ltm; renderer.g2 = ltm; renderer.b2 = ltm;
						renderer.r3 = lmm; renderer.g3 = lmm; renderer.b3 = lmm;
						renderer.r4 = lmt; renderer.g4 = lmt; renderer.b4 = lmt;

						renderer.x1 = tx; renderer.y1 = ty;
						renderer.x2 = tx; renderer.y2 = my;
						renderer.x3 = mx; renderer.y3 = my;
						renderer.x4 = mx; renderer.y4 = ty;

						renderer.drawQuadFlipLR4(ceilTex4);
					}
				}
			}

			// render extrude

			// 2l | 1  2
			// 4l | 3  4
			// ---+------
			//    | 1d 2d

			renderer.z1 = HALF_WALL_PLUS_EXTRUDE;
			renderer.z2 = HALF_WALL_PLUS_EXTRUDE;
			renderer.z3 = HALF_WALL;
			renderer.z4 = HALF_WALL;

			lff *= EXTRUDE_LIGHT_MULT;
			lft *= EXTRUDE_LIGHT_MULT;
			ltf *= EXTRUDE_LIGHT_MULT;
			ltt *= EXTRUDE_LIGHT_MULT;
			lfm *= EXTRUDE_LIGHT_MULT;
			lmf *= EXTRUDE_LIGHT_MULT;
			lmm *= EXTRUDE_LIGHT_MULT;
			lmt *= EXTRUDE_LIGHT_MULT;
			ltm *= EXTRUDE_LIGHT_MULT;

			if (tc.x > 0) {
				int ceilTex2l = localCeilMap2[tc.y][tc.x - 1];
				int ceilTex4l = localCeilMap4[tc.y][tc.x - 1];

				if ((ceilTex2l == 0 && ceilTex1 != 0) || (ceilTex2l != 0 && ceilTex1 == 0)) {
					renderer.x1 = fx; renderer.y1 = fy;
					renderer.x2 = fx; renderer.y2 = my;
					renderer.x3 = fx; renderer.y3 = my;
					renderer.x4 = fx; renderer.y4 = fy;

					renderer.r1 = lff; renderer.g1 = lff; renderer.b1 = lff;
					renderer.r2 = lfm; renderer.g2 = lfm; renderer.b2 = lfm;
					renderer.r3 = lfm; renderer.g3 = lfm; renderer.b3 = lfm;
					renderer.r4 = lff; renderer.g4 = lff; renderer.b4 = lff;

					if (ceilTex1 != 0) {
						renderer.drawQuad1(ceilTex1);
					} else {
						renderer.drawQuad2(ceilTex2l);
					}
				}

				if ((ceilTex4l == 0 && ceilTex3 != 0) || (ceilTex4l != 0 && ceilTex3 == 0)) {
					renderer.x1 = fx; renderer.y1 = my;
					renderer.x2 = fx; renderer.y2 = ty;
					renderer.x3 = fx; renderer.y3 = ty;
					renderer.x4 = fx; renderer.y4 = my;

					renderer.r1 = lfm; renderer.g1 = lfm; renderer.b1 = lfm;
					renderer.r2 = lft; renderer.g2 = lft; renderer.b2 = lft;
					renderer.r3 = lft; renderer.g3 = lft; renderer.b3 = lft;
					renderer.r4 = lfm; renderer.g4 = lfm; renderer.b4 = lfm;

					if (ceilTex3 != 0) {
						renderer.drawQuad3(ceilTex3);
					} else {
						renderer.drawQuad4(ceilTex4l);
					}
				}
			}

			if (tc.y < (state.levelHeight - 1)) {
				int ceilTex1d = localCeilMap1[tc.y + 1][tc.x];
				int ceilTex2d = localCeilMap2[tc.y + 1][tc.x];

				if ((ceilTex1d == 0 && ceilTex3 != 0) || (ceilTex1d != 0 && ceilTex3 == 0)) {
					renderer.x1 = fx; renderer.y1 = ty;
					renderer.x2 = mx; renderer.y2 = ty;
					renderer.x3 = mx; renderer.y3 = ty;
					renderer.x4 = fx; renderer.y4 = ty;

					renderer.r1 = lft; renderer.g1 = lft; renderer.b1 = lft;
					renderer.r2 = lmt; renderer.g2 = lmt; renderer.b2 = lmt;
					renderer.r3 = lmt; renderer.g3 = lmt; renderer.b3 = lmt;
					renderer.r4 = lft; renderer.g4 = lft; renderer.b4 = lft;

					if (ceilTex3 != 0) {
						renderer.drawQuad3(ceilTex3);
					} else {
						renderer.drawQuad3(ceilTex1d);
					}
				}

				if ((ceilTex2d == 0 && ceilTex4 != 0) || (ceilTex2d != 0 && ceilTex4 == 0)) {
					renderer.x1 = mx; renderer.y1 = ty;
					renderer.x2 = tx; renderer.y2 = ty;
					renderer.x3 = tx; renderer.y3 = ty;
					renderer.x4 = mx; renderer.y4 = ty;

					renderer.r1 = lmt; renderer.g1 = lmt; renderer.b1 = lmt;
					renderer.r2 = ltt; renderer.g2 = ltt; renderer.b2 = ltt;
					renderer.r3 = ltt; renderer.g3 = ltt; renderer.b3 = ltt;
					renderer.r4 = lmt; renderer.g4 = lmt; renderer.b4 = lmt;

					if (ceilTex4 != 0) {
						renderer.drawQuad4(ceilTex4);
					} else {
						renderer.drawQuad2(ceilTex2d);
					}
				}
			}

			if ((ceilTex1 == 0 && ceilTex2 != 0) || (ceilTex1 != 0 && ceilTex2 == 0)) {
				renderer.x1 = mx; renderer.y1 = fy;
				renderer.x2 = mx; renderer.y2 = my;
				renderer.x3 = mx; renderer.y3 = my;
				renderer.x4 = mx; renderer.y4 = fy;

				renderer.r1 = lmf; renderer.g1 = lmf; renderer.b1 = lmf;
				renderer.r2 = lmm; renderer.g2 = lmm; renderer.b2 = lmm;
				renderer.r3 = lmm; renderer.g3 = lmm; renderer.b3 = lmm;
				renderer.r4 = lmf; renderer.g4 = lmf; renderer.b4 = lmf;

				if (ceilTex2 != 0) {
					renderer.drawQuad2(ceilTex2);
				} else {
					renderer.drawQuad1(ceilTex1);
				}
			}

			if ((ceilTex3 == 0 && ceilTex4 != 0) || (ceilTex3 != 0 && ceilTex4 == 0)) {
				renderer.x1 = mx; renderer.y1 = my;
				renderer.x2 = mx; renderer.y2 = ty;
				renderer.x3 = mx; renderer.y3 = ty;
				renderer.x4 = mx; renderer.y4 = my;

				renderer.r1 = lmm; renderer.g1 = lmm; renderer.b1 = lmm;
				renderer.r2 = lmt; renderer.g2 = lmt; renderer.b2 = lmt;
				renderer.r3 = lmt; renderer.g3 = lmt; renderer.b3 = lmt;
				renderer.r4 = lmm; renderer.g4 = lmm; renderer.b4 = lmm;

				if (ceilTex4 != 0) {
					renderer.drawQuad4(ceilTex4);
				} else {
					renderer.drawQuad3(ceilTex3);
				}
			}

			if ((ceilTex1 == 0 && ceilTex3 != 0) || (ceilTex1 != 0 && ceilTex3 == 0)) {
				renderer.x1 = fx; renderer.y1 = my;
				renderer.x2 = mx; renderer.y2 = my;
				renderer.x3 = mx; renderer.y3 = my;
				renderer.x4 = fx; renderer.y4 = my;

				renderer.r1 = lfm; renderer.g1 = lfm; renderer.b1 = lfm;
				renderer.r2 = lmm; renderer.g2 = lmm; renderer.b2 = lmm;
				renderer.r3 = lmm; renderer.g3 = lmm; renderer.b3 = lmm;
				renderer.r4 = lfm; renderer.g4 = lfm; renderer.b4 = lfm;

				if (ceilTex3 != 0) {
					renderer.drawQuad3(ceilTex3);
				} else {
					renderer.drawQuad1(ceilTex1);
				}
			}

			if ((ceilTex2 == 0 && ceilTex4 != 0) || (ceilTex2 != 0 && ceilTex4 == 0)) {
				renderer.x1 = mx; renderer.y1 = my;
				renderer.x2 = tx; renderer.y2 = my;
				renderer.x3 = tx; renderer.y3 = my;
				renderer.x4 = mx; renderer.y4 = my;

				renderer.r1 = lmm; renderer.g1 = lmm; renderer.b1 = lmm;
				renderer.r2 = ltm; renderer.g2 = ltm; renderer.b2 = ltm;
				renderer.r3 = ltm; renderer.g3 = ltm; renderer.b3 = ltm;
				renderer.r4 = lmm; renderer.g4 = lmm; renderer.b4 = lmm;

				if (ceilTex4 != 0) {
					renderer.drawQuad4(ceilTex4);
				} else {
					renderer.drawQuad2(ceilTex2);
				}
			}
		}
	}

	protected void renderBullets() {
		boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

		for (Bullet bullet = state.bullets.first(); bullet != null; bullet = (Bullet)bullet.next) {
			int tex = bullet.getTexture();

			if (tex < 0 || !localTouchedCellsMap[(int)bullet.y][(int)bullet.x]) {
				continue;
			}

			float fromX = bullet.x + flatObjDy;
			float toX = bullet.x - flatObjDy;
			float fromY = bullet.y - flatObjDx;
			float toY = bullet.y + flatObjDx;

			renderer.x1 = fromX; renderer.y1 = -fromY;
			renderer.x2 = fromX; renderer.y2 = -fromY;
			renderer.x3 = toX; renderer.y3 = -toY;
			renderer.x4 = toX; renderer.y4 = -toY;

			setObjLighting(bullet.x, bullet.y);
			renderer.drawQuad(TextureLoader.BASE_BULLETS + tex);
		}
	}

	protected void renderExplosions() {
		boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

		for (Explosion explosion = state.explosions.first(); explosion != null; explosion = (Explosion)explosion.next) {
			int tex = explosion.getTexture();

			if (tex < 0 || !localTouchedCellsMap[(int)explosion.y][(int)explosion.x]) {
				continue;
			}

			float fromX = explosion.x + flatObjDy;
			float toX = explosion.x - flatObjDy;
			float fromY = explosion.y - flatObjDx;
			float toY = explosion.y + flatObjDx;

			renderer.x1 = fromX; renderer.y1 = -fromY;
			renderer.x2 = fromX; renderer.y2 = -fromY;
			renderer.x3 = toX; renderer.y3 = -toY;
			renderer.x4 = toX; renderer.y4 = -toY;

			setObjLighting(explosion.x, explosion.y);
			renderer.drawQuad(TextureLoader.BASE_EXPLOSIONS + tex);
		}
	}

	public void render(GL10 gl, long elapsedTime, float ypos) {
		updateDoors(elapsedTime);

		currentHeroX = state.heroX;
		currentHeroY = state.heroY;
		currentHeroA = state.heroA;
		currentHeroAr = engine.heroAr;
		currentHeroCs = engine.heroCs;
		currentHeroSn = engine.heroSn;

		tracer.trace(currentHeroX, currentHeroY, currentHeroAr, 44.0f * GameMath.G2RAD_F);
		renderer.frustrumModelIdentity(gl);

		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_DEPTH_TEST);

		gl.glTranslatef(0f, ypos, -0.1f);
		gl.glRotatef(-90f - state.heroVertA, 1.0f, 0f, 0f);
		gl.glRotatef(90.0f - currentHeroA, 0f, 0f, 1.0f);
		gl.glTranslatef(- currentHeroX, currentHeroY, 0f);

		renderer.a1 = 1.0f;
		renderer.a2 = 1.0f;
		renderer.a3 = 1.0f;
		renderer.a4 = 1.0f;

		// Arrows on floor

		renderer.init();
		renderFloorArrows();
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL10.GL_GREATER, Renderer.ALPHA_VALUE);
		renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
		renderer.flush(gl);
		gl.glDisable(GL10.GL_ALPHA_TEST);

		// Floor and Ceiling

		renderer.init();
		renderFloorAndCeil();
		gl.glDisable(GL10.GL_CULL_FACE); // necessary for extrude, and has no affect on floor and ceil
		renderer.flush(gl);
		gl.glEnable(GL10.GL_CULL_FACE);

		// Walls and Door Sides

		renderer.init();
		renderLevel();
		renderer.flush(gl);

		// Doors

		renderer.init();
		renderDoors();

		gl.glDisable(GL10.GL_CULL_FACE); // necessary for doors and transparents, and has no affect on monsters and objects
		renderer.flush(gl);

		// Monsters, Objects & Transparents

		flatObjDx = FloatMath.cos(- currentHeroAr) * 0.5f;
		flatObjDy = FloatMath.sin(- currentHeroAr) * 0.5f;

		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL10.GL_GREATER, Renderer.ALPHA_VALUE);

		// objects rendered after monsters (so if monster stay in cell with object, monster will be in front)
		// or reverse order, but set appropriate depth test function

		for (int texmap = 0; texmap < 2; texmap++) {
			renderer.init();
			renderMonsters(elapsedTime, false, texmap);
			renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MONSTERS + texmap]);
			renderer.flush(gl);
		}

		renderer.z1 = -HALF_WALL;
		renderer.z2 = HALF_WALL;
		renderer.z3 = HALF_WALL;
		renderer.z4 = -HALF_WALL;

		renderer.init();
		renderExplosions();
		renderBullets();
		renderObjects();
		renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
		renderer.flush(gl);

		// dead corpses rendered to be in back

		for (int texmap = 0; texmap < 2; texmap++) {
			renderer.init();
			renderMonsters(elapsedTime, true, texmap);
			renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MONSTERS + texmap]);
			renderer.flush(gl);
		}

		gl.glDisable(GL10.GL_ALPHA_TEST);
		gl.glEnable(GL10.GL_CULL_FACE);
	}

	public void renderAutoMap(GL10 gl) {
		gl.glDisable(GL10.GL_DEPTH_TEST);

		float autoMapZoom = 20.0f;

		renderer.initOrtho(gl, true, false, -autoMapZoom * engine.ratio, autoMapZoom * engine.ratio, -autoMapZoom, autoMapZoom, 0f, 1.0f);
		gl.glPushMatrix(); // push model matrix
		gl.glTranslatef(config.mapPosition * autoMapZoom, 0f, 0f);

		gl.glRotatef(90.0f - currentHeroA, 0f, 0f, 1.0f);
		gl.glTranslatef(- currentHeroX, currentHeroY, 0f);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		renderer.init();

		renderer.a1 = 0.5f; renderer.a2 = 0.5f;
		renderer.b1 = 0.0f; renderer.b2 = 0.0f;

		for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
			if (aw.door != null) {
				renderer.r1 = 0.0f; renderer.r2 = 0.0f;
				renderer.g1 = 1.0f; renderer.g2 = 1.0f;

				float fromX = aw.fromX;
				float fromY = aw.fromY;
				float toX = aw.toX;
				float toY = aw.toY;

				if (aw.vert) {
					fromY += aw.door.openPos;
				} else {
					fromX += aw.door.openPos;
				}

				renderer.drawLine(fromX, -fromY, toX, -toY);
			} else {
				if (aw.type == AUTO_WALL_TYPE_WALL) {
					renderer.r1 = 1.0f; renderer.r2 = 1.0f;
					renderer.g1 = 1.0f; renderer.g2 = 1.0f;
				} else {
					renderer.r1 = 0.5f; renderer.r2 = 0.5f;
					renderer.g1 = 0.5f; renderer.g2 = 0.5f;
				}

				renderer.drawLine(aw.fromX, -aw.fromY, aw.toX, -aw.toY);
			}
		}

		int[][] localPassableMap = state.passableMap;
		int[][] localArrowsMap = state.arrowsMap;

		if (profile.isPurchased(Store.SECRETS)) {
			renderer.r1 = 0.0f; renderer.r2 = 0.0f;
			renderer.g1 = 1.0f; renderer.g2 = 1.0f;

			for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
				if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_SECRET) != 0) {
					float x = (float)tc.x;
					float y = (float)tc.y;

					renderer.drawLine(x + 0.35f, -(y + 0.5f), x + 0.65f, -(y + 0.5f));
					renderer.drawLine(x + 0.5f, -(y + 0.35f), x + 0.5f, -(y + 0.65f));
				}
			}
		}

		renderer.r1 = 1.0f; renderer.g1 = 1.0f; renderer.b1 = 1.0f;
		renderer.r2 = 1.0f; renderer.g2 = 1.0f; renderer.b2 = 1.0f;
		float arrCycle = (float)(engine.elapsedTime % 1000) / 1000.0f;

		for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
			int arrowTex = localArrowsMap[tc.y][tc.x];

			if (arrowTex != 0) {
				float x = (float)tc.x;
				float y = (float)tc.y;
				float arrOffset = arrCycle * 0.5f - 0.5f;

				for (int i = 0; i < 2; i++) {
					if (i == 0) {
						renderer.a1 = arrCycle;
						renderer.a2 = arrCycle;
					} else {
						renderer.a1 = 1.0f - arrCycle;
						renderer.a2 = 1.0f - arrCycle;
					}

					switch (arrowTex) {
						case TextureLoader.ARROW_UP:
							renderer.drawLine(x + 0.25f, -(y + 0.75f - arrOffset), x + 0.5f, -(y + 0.25f - arrOffset));
							renderer.drawLine(x + 0.5f, -(y + 0.25f - arrOffset), x + 0.75f, -(y + 0.75f - arrOffset));
							break;

						case TextureLoader.ARROW_RT:
							renderer.drawLine(x + 0.25f + arrOffset, -(y + 0.75f), x + 0.75f + arrOffset, -(y + 0.5f));
							renderer.drawLine(x + 0.75f + arrOffset, -(y + 0.5f), x + 0.25f + arrOffset, -(y + 0.25f));
							break;

						case TextureLoader.ARROW_DN:
							renderer.drawLine(x + 0.25f, -(y + 0.25f + arrOffset), x + 0.5f, -(y + 0.75f + arrOffset));
							renderer.drawLine(x + 0.5f, -(y + 0.75f + arrOffset), x + 0.75f, -(y + 0.25f + arrOffset));
							break;

						case TextureLoader.ARROW_LT:
							renderer.drawLine(x + 0.75f - arrOffset, -(y + 0.75f), x + 0.25f - arrOffset, -(y + 0.5f));
							renderer.drawLine(x + 0.25f - arrOffset, -(y + 0.5f), x + 0.75f - arrOffset, -(y + 0.25f));
							break;
					}

					arrOffset += 0.5f;
				}
			}
		}

		renderer.r1 = 0.5f; renderer.r2 = 0.5f;
		renderer.g1 = 0.5f; renderer.g2 = 0.5f;
		renderer.b1 = 0.0f; renderer.b2 = 0.0f;
		renderer.a1 = 0.5f; renderer.a2 = 0.5f;

		for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
			if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_DECOR_ITEM) != 0) {
				float x = (float)tc.x + 0.25f;
				float y = (float)tc.y + 0.25f;

				renderer.drawLine(x, -y, x + 0.5f, -y);
				renderer.drawLine(x + 0.5f, -y, x + 0.5f, -(y + 0.5f));
				renderer.drawLine(x + 0.5f, -(y + 0.5f), x, -(y + 0.5f));
				renderer.drawLine(x, -(y + 0.5f), x, -y);
			}
		}

		renderer.b1 = 0.5f;
		renderer.b2 = 0.5f;

		for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
			if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_OBJECT) != 0) {
				float x = (float)tc.x;
				float y = (float)tc.y;

				renderer.drawLine(x + 0.5f, -(y + 0.25f), x + 0.75f, -(y + 0.5f));
				renderer.drawLine(x + 0.75f, -(y + 0.5f), x + 0.5f, -(y + 0.75f));
				renderer.drawLine(x + 0.5f, -(y + 0.75f), x + 0.25f, -(y + 0.5f));
				renderer.drawLine(x + 0.25f, -(y + 0.5f), x + 0.5f, -(y + 0.25f));
			}
		}

		if (showMonstersOnMap) {
			renderer.g1 = 0.0f;
			renderer.g2 = 0.0f;

			for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
				if (mon.health <= 0) {
					renderer.r1 = 0.5f; renderer.b1 = 0.5f;
					renderer.r2 = 0.5f; renderer.b2 = 0.5f;
				} else if (mon.chaseMode) {
					renderer.r1 = 1.0f; renderer.b1 = 0.0f;
					renderer.r2 = 1.0f; renderer.b2 = 0.0f;
				} else {
					renderer.r1 = 0.0f; renderer.b1 = 1.0f;
					renderer.r2 = 0.0f; renderer.b2 = 1.0f;
				}

				float mdx = FloatMath.cos((float)mon.shootAngle * GameMath.G2RAD_F) * 0.75f;
				float mdy = FloatMath.sin((float)mon.shootAngle * GameMath.G2RAD_F) * 0.75f;

				renderer.drawLine(mon.x, -mon.y, mon.x+mdx, -mon.y+mdy);

				mdx *= 0.25;
				mdy *= 0.25;

				renderer.drawLine(mon.x+mdy, -mon.y-mdx, mon.x-mdy, -mon.y+mdx);
			}

			renderer.r1 = 1.0f; renderer.b1 = 0.5f;
			renderer.r2 = 1.0f; renderer.b2 = 0.5f;

			for (Bullet bullet = state.bullets.first(); bullet != null; bullet = (Bullet)bullet.next) {
				float x = bullet.x;
				float y = bullet.y;

				renderer.drawLine(x, -(y - 0.125f), x + 0.125f, -y);
				renderer.drawLine(x + 0.125f, -y, x, -(y + 0.125f));
				renderer.drawLine(x, -(y + 0.125f), x - 0.125f, -y);
				renderer.drawLine(x - 0.125f, -y, x, -(y - 0.125f));
			}

			renderer.g1 = 0.5f;
			renderer.g2 = 0.5f;

			for (Explosion explosion = state.explosions.first(); explosion != null; explosion = (Explosion)explosion.next) {
				float x = explosion.x;
				float y = explosion.y;

				renderer.drawLine(x, -(y - 0.125f), x + 0.125f, -y);
				renderer.drawLine(x + 0.125f, -y, x, -(y + 0.125f));
				renderer.drawLine(x, -(y + 0.125f), x - 0.125f, -y);
				renderer.drawLine(x - 0.125f, -y, x, -(y - 0.125f));
			}

			for (TraceInfo traceInfo = tracesInfo.first(); traceInfo != null;) {
				TraceInfo nextTraceInfo = (TraceInfo)traceInfo.next;

				if (traceInfo.hit == Bullet.HitParams.HIT_OUT) {
					renderer.setLineRGB(0.0f, 1.0f, 0.0f);
				} else if (traceInfo.hit == Bullet.HitParams.HIT_WALL) {
					renderer.setLineRGB(0.5f, 0.5f, 0.5f);
				} else if (traceInfo.hit == Bullet.HitParams.HIT_MONSTER) {
					renderer.setLineRGB(0.0f, 1.0f, 0.0f);
				} else if (traceInfo.hit == Bullet.HitParams.HIT_HERO) {
					renderer.setLineRGB(1.0f, 0.0f, 0.0f);
				} else {
					renderer.setLineRGB(0.0f, 0.0f, 1.0f);
				}

				renderer.drawLine(traceInfo.sx, -traceInfo.sy, traceInfo.ex, -traceInfo.ey);
				traceInfo.ticks++;

				if (traceInfo.ticks > 10) {
					tracesInfo.release(traceInfo);
				}

				traceInfo = nextTraceInfo;
			}
		}

		renderer.flush(gl, false);

		gl.glPopMatrix(); // pop model matrix
		gl.glTranslatef(config.mapPosition * autoMapZoom, 0f, 0f);
		renderer.init();

		final float hw = 0.4f;
		final float hh = 0.5f;

		renderer.r1 = 1.0f; renderer.g1 = 1.0f; renderer.b1 = 1.0f; renderer.a1 = 0.9f;
		renderer.r2 = 1.0f; renderer.g2 = 1.0f; renderer.b2 = 1.0f; renderer.a2 = 0.9f;

		renderer.drawLine(-hw, -hh, 0.0f, hh);
		renderer.drawLine(0.0f, hh, hw, -hh);
		renderer.drawLine(-hw, -hh, hw, -hh);

		renderer.flush(gl, false);

		gl.glDisable(GL10.GL_BLEND);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	public void openSecrets() {
		int[][] localPassableMap = state.passableMap;

		for (int cy = 0, levelHeight = state.levelHeight; cy < levelHeight; cy++) {
			for (int cx = 0, levelWidth = state.levelWidth; cx < levelWidth; cx++) {
				int pass = localPassableMap[cy][cx];

				if ((pass & Level.PASSABLE_IS_SECRET) != 0
					&& (pass & Level.PASSABLE_IS_WALL) == 0
					&& !awTouchedCellsMap[cy][cx]
					&& state.awTouchedCells.canTake()
				) {
					awTouchedCellsMap[cy][cx] = true;
					state.awTouchedCells.take().initFrom(cx, cy);
				}
			}
		}
	}

	public void openAllMap() {
		int[][] localTexMap = state.texMap;
		int[][] localPassableMap = state.passableMap;
		int[][] localDrawedAutoWalls = state.drawedAutoWalls;
		Door[][] localDoorsMap = level.doorsMap;

		for (int cy = 0, levelHeight = state.levelHeight; cy < levelHeight; cy++) {
			for (int cx = 0, levelWidth = state.levelWidth; cx < levelWidth; cx++) {
				int tex = localTexMap[cy][cx];
				int pass = localPassableMap[cy][cx];
				Door door = localDoorsMap[cy][cx];

				if ((pass & Level.PASSABLE_IS_WALL) != 0) {
					for (int s = 0; s < 4; s++) {
						int tx = cx + PortalTracer.X_CELL_ADD[s];
						int ty = cy + PortalTracer.Y_CELL_ADD[s];

						if (tx > 0 &&
							ty > 0 &&
							tx < levelWidth &&
							ty < levelHeight &&
							(localPassableMap[ty][tx] & Level.PASSABLE_IS_WALL) == 0
						) {
							int fromX = cx + PortalTracer.X_ADD[s];
							int fromY = cy + PortalTracer.Y_ADD[s];
							int toX = cx + PortalTracer.X_ADD[(s + 1) % 4];
							int toY = cy + PortalTracer.Y_ADD[(s + 1) % 4];

							int mx = (fromX < toX ? fromX : toX);
							int my = (fromY < toY ? fromY : toY);
							int autoWallMask = ((s == 1 || s == 3) ? AUTO_WALL_MASK_VERTICAL : AUTO_WALL_MASK_HORIZONTAL);

							if (((localDrawedAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
								localDrawedAutoWalls[my][mx] |= autoWallMask;
								appendAutoWall(fromX, fromY, toX, toY, AUTO_WALL_TYPE_WALL);
							}
						}
					}
				} else if ((pass & Level.PASSABLE_IS_TRANSP_WINDOW) != 0) {
					float fromX, fromY, toX, toY;
					boolean vert = ((pass & Level.PASSABLE_IS_TRANSP_WINDOW_VERT) != 0);

					if (vert) {
						fromX = (float)cx + 0.5f;
						toX = fromX;
						fromY = (float)cy;
						toY = fromY + 1.0f;
					} else {
						fromX = (float)cx;
						toX = fromX + 1.0f;
						fromY = (float)cy + 0.5f;
						toY = fromY;
					}

					if (((localDrawedAutoWalls[cy][cx] & AUTO_WALL_MASK_DOOR) == 0) && state.autoWalls.canTake()) {
						localDrawedAutoWalls[cy][cx] |= AUTO_WALL_MASK_DOOR;
						AutoWall aw = state.autoWalls.take();

						aw.fromX = fromX;
						aw.fromY = fromY;
						aw.toX = toX;
						aw.toY = toY;
						aw.vert = vert;
						aw.type = AUTO_WALL_TYPE_TRANSP;
						aw.doorUid = -1;
						aw.door = null;
					}
				} else if (((pass & Level.PASSABLE_IS_TRANSP) != 0) && (tex != 0)) {
					for (int s = 0; s < 4; s++) {
						int tx = cx + PortalTracer.X_CELL_ADD[s];
						int ty = cy + PortalTracer.Y_CELL_ADD[s];

						if (tx > 0 &&
							ty > 0 &&
							tx < levelWidth &&
							ty < levelHeight &&
							(localPassableMap[ty][tx] & Level.PASSABLE_MASK_WALL_N_TRANSP) == 0
						) {
							int fromX = cx + PortalTracer.X_ADD[s];
							int fromY = cy + PortalTracer.Y_ADD[s];
							int toX = cx + PortalTracer.X_ADD[(s + 1) % 4];
							int toY = cy + PortalTracer.Y_ADD[(s + 1) % 4];

							int mx = (fromX < toX ? fromX : toX);
							int my = (fromY < toY ? fromY : toY);
							int autoWallMask = ((s == 1 || s == 3) ? AUTO_WALL_MASK_VERTICAL : AUTO_WALL_MASK_HORIZONTAL);

							if (((localDrawedAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
								localDrawedAutoWalls[my][mx] |= autoWallMask;
								appendAutoWall(fromX, fromY, toX, toY, AUTO_WALL_TYPE_TRANSP);
							}
						}
					}
				} else if (door != null) {
					float fromX, fromY, toX, toY;

					if (door.vert) {
						fromX = (float)door.x + 0.5f;
						toX = fromX;
						fromY = (float)door.y;
						toY = fromY + 1.0f;
					} else {
						fromX = (float)door.x;
						toX = fromX + 1.0f;
						fromY = (float)door.y + 0.5f;
						toY = fromY;
					}

					if (((localDrawedAutoWalls[door.y][door.x] & AUTO_WALL_MASK_DOOR) == 0) && state.autoWalls.canTake()) {
						localDrawedAutoWalls[door.y][door.x] |= AUTO_WALL_MASK_DOOR;
						AutoWall aw = state.autoWalls.take();

						aw.fromX = fromX;
						aw.fromY = fromY;
						aw.toX = toX;
						aw.toY = toY;
						aw.vert = door.vert;
						aw.type = AUTO_WALL_TYPE_DOOR;
						aw.doorUid = door.uid;
						aw.door = door;
					}
				}

				if ((pass & Level.PASSABLE_IS_WALL) == 0 && !awTouchedCellsMap[cy][cx] && state.awTouchedCells.canTake()) {
					awTouchedCellsMap[cy][cx] = true;
					state.awTouchedCells.take().initFrom(cx, cy);
				}
			}
		}
	}

	public void surfaceSizeChanged(GL10 gl) {
		float size = 0.1f * (float)Math.tan(Math.toRadians(50.0) / 2);
		renderer.initFrustum(gl, -size, size, -size / engine.ratio, size / engine.ratio, 0.1f, 100.0f);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
}
