package zame.game.engine.controls;

import zame.game.engine.Engine;
import zame.game.engine.EngineObject;

public class JoystickController implements EngineObject {
	protected Engine engine;

	@Override
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	public void reload() {
	}

	public void setJoystickValues(float joyX, float joyY) {
	}

	public void joystickButtonPressed(int buttonId) {
	}

	public void joystickButtonReleased(int buttonId) {
	}

	public void updateHero() {
	}
}
