package zame.game.engine.controls;

import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.State;

public class AccelerometerController implements EngineObject {
	protected Engine engine;
	protected Config config;
	protected Game game;
	protected State state;
	protected float accelerometerX = 0.0f;
	protected float accelerometerY = 0.0f;

	@Override
	public void setEngine(Engine engine) {
		this.engine = engine;
		this.config = engine.config;
		this.game = engine.game;
		this.state = engine.state;
	}

	public void reload() {
		accelerometerX = 0.0f;
		accelerometerY = 0.0f;
	}

	public void setAccelerometerValues(float accelerometerX, float accelerometerY) {
		this.accelerometerX = accelerometerX;
		this.accelerometerY = accelerometerY;
	}

	public void updateHero() {
		if (Math.abs(accelerometerX) >= 0.1f) {
			state.setHeroA(state.heroA + accelerometerX * config.accelerometerAcceleration * config.horizontalLookMult * config.rotateSpeed);
			engine.interracted = true;
		}
	}
}
