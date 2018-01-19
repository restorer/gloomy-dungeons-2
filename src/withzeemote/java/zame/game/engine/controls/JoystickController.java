package zame.game.engine.controls;

import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.State;

public class JoystickController implements EngineObject {
    protected Engine engine;
    protected Config config;
    protected Game game;
    protected State state;
    protected float joyX = 0.0f;
    protected float joyY = 0.0f;
    protected int joyButtonsMask = 0;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.config = engine.config;
        this.game = engine.game;
        this.state = engine.state;
    }

    public void reload() {
        joyX = 0.0f;
        joyY = 0.0f;
        joyButtonsMask = 0;
    }

    public void setJoystickValues(float joyX, float joyY) {
        this.joyX = joyX;
        this.joyY = joyY;
    }

    public void joystickButtonPressed(int buttonId) {
        int mapping = config.zeemoteHelper.zeemoteButtonMappings[buttonId];

        if (mapping == Controls.NEXT_WEAPON) {
            game.actionNextWeapon = true;
        } else if (mapping == Controls.TOGGLE_MAP) {
            game.actionToggleMap = true;
        }

        joyButtonsMask |= mapping;
    }

    public void joystickButtonReleased(int buttonId) {
        joyButtonsMask &= ~(config.zeemoteHelper.zeemoteButtonMappings[buttonId]);
    }

    public void updateHero() {
        if (Math.abs(joyY) >= 0.01f) {
            game.updateHeroPosition(-engine.heroCs, engine.heroSn, joyY * 0.2f * config.moveSpeed);
            engine.interracted = true;
        }

        if (Math.abs(joyX) >= 0.01f) {
            if ((joyButtonsMask & Controls.STRAFE_MODE) != 0) {
                game.updateHeroPosition(engine.heroSn, engine.heroCs, joyX * 0.2f * config.strafeSpeed);
            } else {
                state.setHeroA(state.heroA - joyX * 2.0f * config.rotateSpeed);
            }

            engine.interracted = true;
        }

        if ((joyButtonsMask & Controls.FIRE) != 0) {
            game.actionFire |= Controls.ACTION_FIRE_JOYSTICK;
            engine.interracted = true;
        } else {
            game.actionFire &= ~Controls.ACTION_FIRE_JOYSTICK;
        }
    }
}
