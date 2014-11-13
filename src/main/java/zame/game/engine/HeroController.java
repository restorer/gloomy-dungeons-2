package zame.game.engine;

import android.view.KeyEvent;
import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;

public abstract class HeroController implements EngineObject {
	public static HeroController newInstance(boolean isWallpaper) {
		if (isWallpaper) {
			return new HeroControllerAuto();
		} else {
			return new HeroControllerHuman();
		}
	}

	public abstract void updateHero();

	public void reload() {
	}

	public void surfaceSizeChanged() {
	}

	public void onDrawFrame() {
	}

	public void updateAfterLoadOrCreate() {
	}

	public void renderControls(GL10 gl, boolean renderHelpOverlay, boolean helpOverlayFullMode, long firstTouchTime) {
	}

	public boolean onKeyUp(int keyCode) {
		return false;
	}

	public boolean onKeyDown(int keyCode) {
		return false;
	}

	public void onTouchEvent(MotionEvent event) {
	}

	public void onTrackballEvent(MotionEvent event) {
	}

	public void setAccelerometerValues(float accelerometerX, float accelerometerY) {
	}

	public void initJoystickVars() {
	}

	public void setJoystickValues(float joyX, float joyY) {
	}

	public void joystickButtonPressed(int buttonId) {
	}

	public void joystickButtonReleased(int buttonId) {
	}
}
