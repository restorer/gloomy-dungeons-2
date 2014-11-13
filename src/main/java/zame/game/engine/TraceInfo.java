package zame.game.engine;

import zame.game.engine.data.DataListItem;

public class TraceInfo extends DataListItem {
	public float sx;
	public float sy;
	public float ex;
	public float ey;
	public int hit;
	public int ticks;

	public static void addInfo(LevelRenderer levelRenderer, float sx, float sy, float ex, float ey, int hit) {
		TraceInfo traceInfo = levelRenderer.tracesInfo.take();

		if (traceInfo == null) {
			levelRenderer.tracesInfo.release(levelRenderer.tracesInfo.first());
			traceInfo = levelRenderer.tracesInfo.take();
		}

		traceInfo.sx = sx;
		traceInfo.sy = sy;
		traceInfo.ex = ex;
		traceInfo.ey = ey;
		traceInfo.hit = hit;
		traceInfo.ticks = 0;
	}
}
