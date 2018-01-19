package zame.game;

import com.zeemote.zc.Controller;
import com.zeemote.zc.event.BatteryEvent;
import com.zeemote.zc.event.ButtonEvent;
import com.zeemote.zc.event.ControllerEvent;
import com.zeemote.zc.event.DisconnectEvent;
import com.zeemote.zc.event.IButtonListener;
import com.zeemote.zc.event.IJoystickListener;
import com.zeemote.zc.event.IStatusListener;
import com.zeemote.zc.event.JoystickEvent;
import zame.game.fragments.GameFragment;

public class MyApplicationZeemoteHelper implements IStatusListener, IJoystickListener, IButtonListener {
    public Controller zeemoteController = null;

    public void onCreate() {
        try {
            zeemoteController = new Controller(1, Controller.TYPE_JS1);
            zeemoteController.addStatusListener(this);
            zeemoteController.addButtonListener(this);
            zeemoteController.addJoystickListener(this);
        } catch (Exception ex) {
            Common.log(ex);
        }
    }

    @Override
    public void batteryUpdate(BatteryEvent event) {
    }

    @Override
    public void connected(ControllerEvent event) {
        if (GameFragment.self != null) {
            GameFragment.self.zeemoteHelper.joystickConnectedOrDisconnected();
        }
    }

    @Override
    public void disconnected(DisconnectEvent event) {
        if (GameFragment.self != null) {
            GameFragment.self.zeemoteHelper.joystickConnectedOrDisconnected();
        }
    }

    @Override
    public void joystickMoved(JoystickEvent e) {
        if (GameFragment.self != null) {
            GameFragment.self.zeemoteHelper.joystickMoved(e);
        }
    }

    @Override
    public void buttonPressed(ButtonEvent e) {
        if (GameFragment.self != null) {
            GameFragment.self.zeemoteHelper.joystickButtonPressed(e);
        }
    }

    @Override
    public void buttonReleased(ButtonEvent e) {
        if (GameFragment.self != null) {
            GameFragment.self.zeemoteHelper.joystickButtonReleased(e);
        }
    }
}
