package zame.game.engine;

import android.view.KeyEvent;
import javax.microedition.khronos.opengles.GL10;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.MyApplication;
import zame.game.engine.controls.Controls;

public class Config implements EngineObject {
	protected Engine engine;

	public int controlScheme;
	public float controlsAlpha;
	public float controlsScale;
	public boolean accelerometerEnabled;
	public float accelerometerAcceleration;
	public float trackballAcceleration;
	public float verticalLookMult;
	public float horizontalLookMult;
	public boolean leftHandAim;
	public boolean fireButtonAtTop;
	public float moveSpeed;
	public float strafeSpeed;
	public float rotateSpeed;
	public int[] keyMappings;
	public float gamma;
	public int levelTextureFilter;
	public int weaponsTextureFilter;
	public float mapPosition;
	public boolean showCrosshair;
	public boolean rotateScreen;
	public float wpDim = 0.5f;
	public ConfigZeemoteHelper zeemoteHelper = new ConfigZeemoteHelper();

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	protected void updateKeyMap(SharedPreferences sp, String key, int type) {
		int keyCode = sp.getInt(key, 0);

		if (keyCode > 0 && keyCode < keyMappings.length) {
			keyMappings[keyCode] = type;
		}
	}

	protected float getAccel(int value, int valueMin, int valueMid, int valueMax, float accelMin, float accelMid, float accelMax) {
		if (value <= valueMin) {
			return accelMin;
		} else if (value < valueMid) {
			return (float)(value - valueMin) / (float)(valueMid - valueMin) * (accelMid - accelMin) + accelMin;
		} else if (value == valueMid) {
			return accelMid;
		} else if (value < valueMax) {
			return (float)(value - valueMid) / (float)(valueMax - valueMid) * (accelMax - accelMid) + accelMid;
		} else {
			return accelMax;
		}
	}

	public void reload() {
		SharedPreferences sp = MyApplication.self.getSharedPreferences();
		String controlSchemeStr = sp.getString("ControlsScheme", "StaticMovePad");

		if (!zeemoteHelper.setControlScheme(this, controlSchemeStr)) {
			if (controlSchemeStr.equals("FreeMovePad")) {
				controlScheme = Controls.SCHEME_FREE_MOVE_PAD;
			} else {
				controlScheme = Controls.SCHEME_STATIC_MOVE_PAD;
			}
		}

		moveSpeed = getAccel(sp.getInt("MoveSpeed", 8), 1, 8, 15, 0.25f, 0.5f, 1.0f);
		strafeSpeed = getAccel(sp.getInt("StrafeSpeed", 8), 1, 8, 15, 0.25f, 0.5f, 1.0f) * 0.5f;
		rotateSpeed = getAccel(sp.getInt("RotateSpeed", 8), 1, 8, 15, 0.5f, 1.0f, 2.0f);
		verticalLookMult = (sp.getBoolean("InvertVerticalLook", false) ? -1.0f : 1.0f);
		horizontalLookMult = (sp.getBoolean("InvertHorizontalLook", false) ? -1.0f : 1.0f);
		leftHandAim = sp.getBoolean("LeftHandAim", false);
		fireButtonAtTop = sp.getBoolean("FireButtonAtTop", false);
		controlsAlpha = 0.1f * (float)sp.getInt("ControlsAlpha", 5);
		controlsScale = 0.5f + 0.25f * (float)sp.getInt("ControlsScale", 2); // from 0.5f up to 1.25f
		accelerometerEnabled = sp.getBoolean("AccelerometerEnabled", false);
		accelerometerAcceleration = (float)sp.getInt("AccelerometerAcceleration", 5);
		trackballAcceleration = getAccel(sp.getInt("TrackballAcceleration", 5), 1, 5, 9, 0.1f, 1.0f, 10.0f);

		zeemoteHelper.reload(sp);
		keyMappings = new int[KeyEvent.getMaxKeyCode()];

		for (int i = 0; i < keyMappings.length; i++) {
			keyMappings[i] = 0;
		}

		updateKeyMap(sp, "KeyForward", Controls.FORWARD);
		updateKeyMap(sp, "KeyBackward", Controls.BACKWARD);
		updateKeyMap(sp, "KeyRotateLeft", Controls.ROTATE_LEFT);
		updateKeyMap(sp, "KeyRotateRight", Controls.ROTATE_RIGHT);
		updateKeyMap(sp, "KeyStrafeLeft", Controls.STRAFE_LEFT);
		updateKeyMap(sp, "KeyStrafeRight", Controls.STRAFE_RIGHT);
		updateKeyMap(sp, "KeyFire", Controls.FIRE);
		updateKeyMap(sp, "KeyNextWeapon", Controls.NEXT_WEAPON);
		updateKeyMap(sp, "KeyToggleMap", Controls.TOGGLE_MAP);
		updateKeyMap(sp, "KeyStrafeMode", Controls.STRAFE_MODE);

		gamma = (float)sp.getInt("Gamma", 1) * 0.04f;
		levelTextureFilter = ((sp.getInt("SmoothingLevel", 2) >= 3) ? GL10.GL_LINEAR : GL10.GL_NEAREST);
		weaponsTextureFilter = ((sp.getInt("SmoothingLevel", 2) >= 2) ? GL10.GL_LINEAR : GL10.GL_NEAREST);
		mapPosition = (float)(sp.getInt("MapPosition", 5) - 5) * 0.2f;
		showCrosshair = sp.getBoolean("ShowCrosshair", true);
		rotateScreen = sp.getBoolean("RotateScreen", false);
	}
}
