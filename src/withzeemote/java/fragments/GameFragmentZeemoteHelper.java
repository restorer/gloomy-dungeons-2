package zame.game.fragments;

import com.zeemote.zc.event.ButtonEvent;
import com.zeemote.zc.event.JoystickEvent;
import com.zeemote.zc.ui.android.ControllerAndroidUi;
import zame.game.MyApplication;
import zame.game.engine.controls.ControlsZeemoteHelper;

public class GameFragmentZeemoteHelper {
    protected GameFragment gameFragment;
    protected ControllerAndroidUi zeemoteControllerUi = null;

    public GameFragmentZeemoteHelper(GameFragment gameFragment) {
        this.gameFragment = gameFragment;
    }

    public void onResume() {
        if (gameFragment.config.controlScheme == ControlsZeemoteHelper.SCHEME_ZEEMOTE) {
            if (zeemoteControllerUi == null) {
                zeemoteControllerUi = new ControllerAndroidUi(gameFragment.activity, MyApplication.self.zeemoteHelper.zeemoteController);
            }

            if (!MyApplication.self.zeemoteHelper.zeemoteController.isConnected()) {
                zeemoteControllerUi.startConnectionProcess();
            }
        }
    }

    public void showZeemoteControllerUi() {
        if (zeemoteControllerUi != null) {
            zeemoteControllerUi.showControllerMenu();
        }
    }

    public void joystickConnectedOrDisconnected() {
        gameFragment.heroController.initJoystickVars();
    }

    public void joystickMoved(JoystickEvent e) {
        if (gameFragment.config.controlScheme == ControlsZeemoteHelper.SCHEME_ZEEMOTE) {
            gameFragment.heroController.setJoystickValues(
                (float)(e.getScaledX(-100, 100)) * 0.01f,
                (float)(e.getScaledY(-100, 100)) * 0.01f
            );
        }
    }

    public void joystickButtonPressed(ButtonEvent e) {
        if (gameFragment.config.controlScheme == ControlsZeemoteHelper.SCHEME_ZEEMOTE) {
            int buttonId = e.getButtonGameAction();

            if ((buttonId >= 0) && (buttonId < gameFragment.config.zeemoteHelper.zeemoteButtonMappings.length)) {
                gameFragment.heroController.joystickButtonPressed(buttonId);
            }
        }
    }

    public void joystickButtonReleased(ButtonEvent e) {
        if (gameFragment.config.controlScheme == ControlsZeemoteHelper.SCHEME_ZEEMOTE) {
            int buttonId = e.getButtonGameAction();

            if ((buttonId >= 0) && (buttonId < gameFragment.config.zeemoteHelper.zeemoteButtonMappings.length)) {
                gameFragment.heroController.joystickButtonReleased(buttonId);
            }
        }
    }
}
