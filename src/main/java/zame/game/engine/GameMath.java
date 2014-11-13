package zame.game.engine;

import android.util.FloatMath;

public class GameMath {
	public static final float PI_F = (float)Math.PI;
	public static final float PI_M2F = (float)(Math.PI * 2.0);
	public static final float PI_D2F = (float)(Math.PI / 2.0);
	public static final float G2RAD_F = (float)(Math.PI / 180.0);
	public static final float RAD2G_F = (float)(180.0 / Math.PI);
	public static final float INFINITY = 0.000000001f;
	public static final float ONE_MINUS_LITTLE = 1.0f - 0.0001f;

	public static float getAngle(float dx, float dy) {
		float l = FloatMath.sqrt(dx*dx + dy*dy);
		float a = (float)Math.acos(dx / (l < INFINITY ? INFINITY : l));

		return (dy < 0 ? a : (PI_M2F - a));
	}

	public static float getAngle(float dx, float dy, float l) {
		float a = (float)Math.acos(dx / (l < INFINITY ? INFINITY : l));
		return (dy < 0 ? a : (PI_M2F - a));
	}
}
