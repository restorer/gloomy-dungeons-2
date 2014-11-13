package zame.game.engine;

import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.controls.AccelerometerController;
import zame.game.engine.controls.Controls;
import zame.game.engine.controls.JoystickController;
import zame.game.engine.controls.KeysController;

public class HeroControllerHuman extends HeroController {
	protected Engine engine;
	protected Game game;
	protected State state;
	protected Config config;
	protected Controls controls = new Controls();
	protected JoystickController joystickController = new JoystickController();
	protected AccelerometerController accelerometerController = new AccelerometerController();
	protected KeysController keysController = new KeysController();

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.game = engine.game;
		this.state = engine.state;
		this.config = engine.config;

		controls.setEngine(engine);
		joystickController.setEngine(engine);
		accelerometerController.setEngine(engine);
		keysController.setEngine(engine);
	}

	@Override
	public void onDrawFrame() {
		keysController.onDrawFrame();
	}

	@Override
	public void updateHero() {
		controls.updateHero();
		joystickController.updateHero();
		accelerometerController.updateHero();
		keysController.updateHero();
	}

	@Override
	public void reload() {
		controls.reload();
		joystickController.reload();
		accelerometerController.reload();
		keysController.reload();
	}

	public void surfaceSizeChanged() {
		controls.surfaceSizeChanged();
	}

	@Override
	public void renderControls(GL10 gl, boolean renderHelpOverlay, boolean helpOverlayFullMode, long firstTouchTime) {
		controls.render(gl, engine.elapsedTime, renderHelpOverlay, helpOverlayFullMode, firstTouchTime);
	}

	@Override
	public boolean onKeyUp(int keyCode) {
		return keysController.onKeyUp(keyCode);
	}

	@Override
	public boolean onKeyDown(int keyCode) {
		return keysController.onKeyDown(keyCode);
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		controls.touchEvent(event);
	}

	@Override
	public void onTrackballEvent(MotionEvent event) {
		keysController.onTrackballEvent(event);
	}

	@Override
	public void setAccelerometerValues(float accelerometerX, float accelerometerY) {
		accelerometerController.setAccelerometerValues(accelerometerX, accelerometerY);
	}

	@Override
	public void initJoystickVars() {
		joystickController.reload();
	}

	@Override
	public void setJoystickValues(float joyX, float joyY) {
		joystickController.setJoystickValues(joyX, joyY);
	}

	@Override
	public void joystickButtonPressed(int buttonId) {
		joystickController.joystickButtonPressed(buttonId);
	}

	@Override
	public void joystickButtonReleased(int buttonId) {
		joystickController.joystickButtonReleased(buttonId);
	}
}
