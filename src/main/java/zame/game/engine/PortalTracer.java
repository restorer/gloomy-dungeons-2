package zame.game.engine;

import android.util.FloatMath;

public class PortalTracer implements EngineObject {
	public static class Wall {
		// public int cellX;
		// public int cellY;
		public int fromX;
		public int fromY;
		public int toX;
		public int toY;
		public int texture;
		public boolean flipTexture;
	}

	// +----> x
	// |
	// v
	// y

	// pts    sides
	//
	// 1 | 0  /-0-\
	// --+--  1   3
	// 2 | 3  \-2-/

	public static final int[] X_ADD = { 1, 0, 0, 1 };
	public static final int[] Y_ADD = { 0, 0, 1, 1 };

	// cell additions used by other classes
	public static final int[] X_CELL_ADD = {  0, -1,  0,  1 };
	public static final int[] Y_CELL_ADD = { -1,  0,  1,  0 };

	protected static final int MAX_WALLS = 1024;
	protected static final int MAX_TOUCHED_CELLS = 2048;

	protected Engine engine;
	protected State state;
	protected int levelWidth;
	protected int levelHeight;
	protected int[][] level;
	protected float heroX;
	protected float heroY;
	protected int[][] drawedWalls = new int[Level.MAX_HEIGHT][Level.MAX_WIDTH];
	protected int tToSide = -1;
	protected int tFromSide = -1;

	public Wall[] walls = new Wall[MAX_WALLS];
	public TouchedCell[] touchedCells = new TouchedCell[MAX_TOUCHED_CELLS];
	public boolean[][] touchedCellsMap = new boolean[Level.MAX_HEIGHT][Level.MAX_WIDTH];

	public int wallsCount = 0;
	public int touchedCellsCount = 0;

	public PortalTracer() {
		for (int i = 0; i < MAX_WALLS; i++) {
			walls[i] = new Wall();
		}

		for (int i = 0; i < MAX_TOUCHED_CELLS; i++) {
			touchedCells[i] = new TouchedCell();
		}
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
	}

	protected void addWallToDraw(int cellX, int cellY, int side, int texture) {
		int mask = 2 << side;

		if ((wallsCount >= MAX_WALLS) || ((drawedWalls[cellY][cellX] & mask) != 0)) {
			return;
		}

		drawedWalls[cellY][cellX] |= mask;
		Wall wall = walls[wallsCount++];

		// wall.cellX = cellX;
		// wall.cellY = cellY;
		wall.fromX = cellX + X_ADD[side];
		wall.fromY = cellY + Y_ADD[side];
		wall.toX = cellX + X_ADD[(side + 1) % 4];
		wall.toY = cellY + Y_ADD[(side + 1) % 4];
		wall.texture = texture;
		wall.flipTexture = (side == 0 || side == 3);
	}

	protected float angleDiff(float fromAngle, float toAngle) {
		if (fromAngle > toAngle) {
			return (toAngle - fromAngle + GameMath.PI_M2F);
		} else {
			return (fromAngle - toAngle);
		}
	}

	protected void addWallBlock(int x, int y, boolean includeDoors) {
		tToSide = -1;
		tFromSide = -1;

		float dy = (float)y + 0.5f - heroY;
		float dx = (float)x + 0.5f - heroX;

		boolean vis_0, vis_1, vis_2, vis_3;
		int[][] localLevel = level;

		if (includeDoors) {
			vis_0 = (dy > 0) && (y > 0) && (localLevel[y-1][x]<=0);
			vis_1 = (dx > 0) && (x > 0) && (localLevel[y][x-1]<=0);
			vis_2 = (dy < 0) && (y < levelHeight-1) && (localLevel[y+1][x]<=0);
			vis_3 = (dx < 0) && (x < levelWidth-1) && (localLevel[y][x+1]<=0);
		} else {
			vis_0 = (dy > 0) && (y > 0) && (localLevel[y-1][x]==0);
			vis_1 = (dx > 0) && (x > 0) && (localLevel[y][x-1]==0);
			vis_2 = (dy < 0) && (y < levelHeight-1) && (localLevel[y+1][x]==0);
			vis_3 = (dx < 0) && (x < levelWidth-1) && (localLevel[y][x+1]==0);
		}

		if (vis_0 && vis_1) {
			tToSide = 0; tFromSide = 1;
		} else if (vis_1 && vis_2) {
			tToSide = 1; tFromSide = 2;
		} else if (vis_2 && vis_3) {
			tToSide = 2; tFromSide = 3;
		} else if (vis_3 && vis_0) {
			tToSide = 3; tFromSide = 0;
		} else if (vis_0) {
			tToSide = 0; tFromSide = 0;
		} else if (vis_1) {
			tToSide = 1; tFromSide = 1;
		} else if (vis_2) {
			tToSide = 2; tFromSide = 2;
		} else if (vis_3) {
			tToSide = 3; tFromSide = 3;
		}

		if (tToSide >= 0 && localLevel[y][x] > 0) {
			for (int i = tFromSide; i != (tToSide + 3) % 4; i = (i + 3) % 4) {
				addWallToDraw(x, y, i, localLevel[y][x]);
			}
		}
	}

	protected void traceCell(int fromX, int fromY, float fromAngle, int toX, int toY, float toAngle) {
		boolean repeat = true;
		int[][] localLevel = level;
		boolean[][] localTouchedCellsMap = touchedCellsMap;
		TouchedCell[] localTouchedCells = touchedCells;

		do {
			float fromDx = FloatMath.cos(fromAngle);
			float fromDy = FloatMath.sin(fromAngle);
			float toDx = FloatMath.cos(toAngle);
			float toDy = FloatMath.sin(toAngle);

			if (fromAngle < 0.5f * GameMath.PI_F) {
				fromX++;

				if (fromX >= levelWidth) {
					return;
				}
			} else if (fromAngle >= 0.75f * GameMath.PI_F) {
				if (fromAngle < 1.5f * GameMath.PI_F) {
					fromX--;

					if (fromX < 0) {
						return;
					}
				} else if (fromAngle >= 1.75f * GameMath.PI_F) {
					fromX++;

					if (fromX >= levelWidth) {
						return;
					}
				}
			}

			if (fromAngle >= 0.25f * GameMath.PI_F) {
				if (fromAngle < GameMath.PI_F) {
					fromY--;

					if (fromY < 0) {
						return;
					}
				} else if (fromAngle >= 1.25f * GameMath.PI_F) {
					fromY++;

					if (fromY >= levelHeight) {
						return;
					}
				}
			}

			if (toAngle > 1.5f * GameMath.PI_F) {
				toX++;

				if (toX >= levelWidth) {
					return;
				}
			} else if (toAngle <= 1.25f * GameMath.PI_F) {
				if (toAngle > 0.5f * GameMath.PI_F) {
					toX--;

					if (toX < 0) {
						return;
					}
				} else if (toAngle <= 0.25f * GameMath.PI_F) {
					toX++;

					if (toX >= levelWidth) {
						return;
					}
				}
			}

			if (toAngle <= 1.75f * GameMath.PI_F) {
				if (toAngle > GameMath.PI_F) {
					toY++;

					if (toY >= levelHeight) {
						return;
					}
				} else if (toAngle <= 0.75f * GameMath.PI_F) {
					toY--;

					if (toY < 0) {
						return;
					}
				}
			}

			for (;;) {
				boolean visible = false;
				int oa = 0;
				int ob = 0;

				if (!localTouchedCellsMap[fromY][fromX]) {
					for (int i = 0; i < 4; i++) {
						float dx = (float)fromX + X_ADD[i] - heroX;
						float dy = (float)fromY + Y_ADD[i] - heroY;

						float tf = dx * fromDy + dy * fromDx;
						float tt = dx * toDy + dy * toDx;

						if (tf <= 0 && tt >= 0) {
							visible = true;
							break;
						} else if (tf >= 0) {
							oa++;
						} else if (tt <= 0) {
							ob++;
						}
					}
				}

				// if at least one point is between fromAngle and toAngle
				// or fromAngle and toAngle is between cell points
				if (visible || (oa > 0 && ob > 0)) {
					break;
				}

				if (fromX == toX && fromY == toY) {
					repeat = false;
					break;
				} else if (fromY > toY) {
					if (fromX >= toX) {
						fromY--;
					} else {
						fromX++;
					}
				} else if (fromY == toY) {
					if (fromX > toX) {
						fromX--;
					} else {
						fromX++;
					}
				} else {
					if (fromX <= toX) {
						fromY++;
					} else {
						fromX--;
					}
				}
			}

			for (;;) {
				boolean visible = false;
				int oa = 0;
				int ob = 0;

				if (!localTouchedCellsMap[toY][toX]) {
					for (int i = 0; i < 4; i++) {
						float dx = (float)toX + X_ADD[i] - heroX;
						float dy = (float)toY + Y_ADD[i] - heroY;

						float tf = dx * fromDy + dy * fromDx;
						float tt = dx * toDy + dy * toDx;

						if (tf <= 0 && tt >= 0) {
							visible = true;
							break;
						} else if (tf >= 0) {
							oa++;
						} else if (tt <= 0) {
							ob++;
						}
					}
				}

				// if at least one point is between fromAngle and toAngle
				// or fromAngle and toAngle is between cell points
				if (visible || (oa > 0 && ob > 0)) {
					break;
				}

				if (fromX == toX && fromY == toY) {
					repeat = false;
					break;
				} else if (fromY > toY) {
					if (fromX > toX) {
						toX++;
					} else {
						toY++;
					}
				} else if (fromY == toY) {
					if (fromX > toX) {
						toX++;
					} else {
						toX--;
					}
				} else {
					if (fromX < toX) {
						toX--;
					} else {
						toY--;
					}
				}
			}

			int x = fromX;
			int y = fromY;
			int prevX = fromX;
			int prevY = fromY;
			int lastX = fromX;
			int lastY = fromY;
			float portFromX = -1;
			float portFromY = -1;
			float portToX = -1;
			float portToY = -1;
			boolean wall = false;
			boolean portal = false;

			for (;;) {
				if (!localTouchedCellsMap[y][x]) {	// just for case
					localTouchedCellsMap[y][x] = true;

					if (/* [add even walls - required for extrude] (localLevel[y][x] <= 0) && */ (touchedCellsCount < MAX_TOUCHED_CELLS)) {
						localTouchedCells[touchedCellsCount].x = x;
						localTouchedCells[touchedCellsCount].y = y;
						touchedCellsCount++;
					}
				}

				if (localLevel[y][x] == 0) {
					if (wall) {
						if (portal) {
							float innerToAngle = (portToX >= 0 ? GameMath.getAngle(portToX - heroX, portToY - heroY) /* + ANG_CORRECT (leaved here just for case) */ : toAngle);

							if (angleDiff(fromAngle, innerToAngle) < GameMath.PI_F) {
								traceCell(fromX, fromY, fromAngle, lastX, lastY, innerToAngle);
							}
						}

						if (portFromX >= 0) {
							fromAngle = GameMath.getAngle(portFromX - heroX, portFromY - heroY) /* - ANG_CORRECT (leaved here just for case) */;
						}

						fromX = x;
						fromY = y;
						wall = false;
						portal = false;
					}
				} else {
					addWallBlock(x, y, localLevel[y][x] > 0); // -1 = closed door

					if (!wall) {
						lastX = prevX;
						lastY = prevY;
						wall = true;
						portal = (x != fromX || y != fromY);

						if (tToSide >= 0) {
							portToX = (float)x + X_ADD[(tFromSide + 1) % 4];
							portToY = (float)y + Y_ADD[(tFromSide + 1) % 4];
						}
					}

					if (tFromSide >= 0) {
						portFromX = (float)x + X_ADD[tToSide];
						portFromY = (float)y + Y_ADD[tToSide];
					}
				}

				if (x == toX && y == toY) {
					if (portal) {
						toX = lastX;
						toY = lastY;

						if (portToX >= 0) {
							toAngle = GameMath.getAngle(portToX - heroX, portToY - heroY) /* + ANG_CORRECT (leaved here just for case) */;

							if (angleDiff(fromAngle, toAngle) > GameMath.PI_F) {
								repeat = false;
							}
						}
					} else if (wall) {
						repeat = false;
					}

					break;
				}

				prevX = x;
				prevY = y;

				if (y > toY) {
					if (x >= toX) {
						y--;
					} else {
						x++;
					}
				} else if (y == toY) {
					if (x > toX) {
						x--;
					} else {
						x++;
					}
				} else {
					if (x <= toX) {
						y++;
					} else {
						x--;
					}
				}
			}
		} while (repeat);
	}

	// halfFov must be between (10 * PI / 180) and (45 * PI / 180)
	public void trace(float x, float y, float heroAngle, float halfFov) {
		level = state.wallsMap;
		levelWidth = state.levelWidth;
		levelHeight = state.levelHeight;

		float fromAngle = heroAngle - halfFov;
		float toAngle = heroAngle + halfFov;

		if (fromAngle < 0) {
			fromAngle += GameMath.PI_M2F;
		} else {
			fromAngle %= GameMath.PI_M2F;
		}

		if (toAngle < 0) {
			toAngle += GameMath.PI_M2F;
		} else {
			toAngle %= GameMath.PI_M2F;
		}

		boolean[][] localTouchedCellsMap = touchedCellsMap;
		int[][] localDrawedWalls = drawedWalls;
		TouchedCell[] localTouchedCells = touchedCells;

		for (int i = 0, lenI = levelHeight; i < lenI; i++) {
			for (int j = 0, lenJ = levelWidth; j < lenJ; j++) {
				localTouchedCellsMap[i][j] = false;
				localDrawedWalls[i][j] = 0;
			}
		}

		heroX = x;
		heroY = y;
		wallsCount = 0;
		int touchedCellsCountL = 0;

		localTouchedCellsMap[(int)y][(int)x] = true;
		localTouchedCells[touchedCellsCountL].x = (int)x;
		localTouchedCells[touchedCellsCountL].y = (int)y;
		touchedCellsCountL++;

		int tx = (int)x;
		int ty = (int)y;

		if (fromAngle < 0.25f * GameMath.PI_F) {
			tx++;
			ty++;
		} else if (fromAngle < 0.5f * GameMath.PI_F) {
			tx++;
		} else if (fromAngle < 0.75f * GameMath.PI_F) {
			tx++;
			ty--;
		} else if (fromAngle < GameMath.PI_F) {
			ty--;
		} else if (fromAngle < 1.25f * GameMath.PI_F) {
			tx--;
			ty--;
		} else if (fromAngle < 1.5f * GameMath.PI_F) {
			tx--;
		} else if (fromAngle < 1.75f * GameMath.PI_F) {
			tx--;
			ty++;
		} else {
			ty++;
		}

		if (level[ty][tx] > 0) {
			addWallBlock(tx, ty, true);
		} else {
			localTouchedCellsMap[ty][tx] = true;
			localTouchedCells[touchedCellsCountL].x = tx;
			localTouchedCells[touchedCellsCountL].y = ty;
			touchedCellsCountL++;
		}

		tx = (int)x;
		ty = (int)y;

		if (toAngle > 1.75f * GameMath.PI_F) {
			tx++;
			ty--;
		} else if (toAngle > 1.5f * GameMath.PI_F) {
			tx++;
		} else if (toAngle > 1.25f * GameMath.PI_F) {
			tx++;
			ty++;
		} else if (toAngle > GameMath.PI_F) {
			ty++;
		} else if (toAngle > 0.75f * GameMath.PI_F) {
			tx--;
			ty++;
		} else if (toAngle > 0.5f * GameMath.PI_F) {
			tx--;
		} else if (toAngle > 0.25f * GameMath.PI_F) {
			tx--;
			ty--;
		} else {
			ty--;
		}

		if (level[ty][tx] > 0) {
			addWallBlock(tx, ty, true);
		} else {
			localTouchedCellsMap[ty][tx] = true;
			localTouchedCells[touchedCellsCountL].x = tx;
			localTouchedCells[touchedCellsCountL].y = ty;
			touchedCellsCountL++;
		}

		touchedCellsCount = touchedCellsCountL;
		traceCell((int)x, (int)y, fromAngle, (int)x, (int)y, toAngle);
	}
}
