package zame.game.engine;

import android.view.KeyEvent;
import com.zeemote.zc.event.ButtonEvent;
import javax.microedition.khronos.opengles.GL10;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.MyApplication;
import zame.game.engine.controls.Controls;
import zame.game.engine.controls.ControlsZeemoteHelper;

public class ConfigZeemoteHelper {
    public int[] zeemoteButtonMappings;

    public boolean setControlScheme(Config config, String controlSchemeStr) {
        if (controlSchemeStr.equals("Zeemote")) {
            config.controlScheme = ControlsZeemoteHelper.SCHEME_ZEEMOTE;
            return true;
        }

        return false;
    }

    public void reload(SharedPreferences sp) {
        zeemoteButtonMappings = new int[Math.max(
            Math.max(
                Math.max(ButtonEvent.BUTTON_A, ButtonEvent.BUTTON_B),
                ButtonEvent.BUTTON_C
            ),
            ButtonEvent.BUTTON_D
        ) + 1];

        zeemoteButtonMappings[ButtonEvent.BUTTON_A] = getControlMaskByName(sp.getString("ZeemoteMappingFire", "None"));
        zeemoteButtonMappings[ButtonEvent.BUTTON_B] = getControlMaskByName(sp.getString("ZeemoteMappingA", "None"));
        zeemoteButtonMappings[ButtonEvent.BUTTON_C] = getControlMaskByName(sp.getString("ZeemoteMappingB", "None"));
        zeemoteButtonMappings[ButtonEvent.BUTTON_D] = getControlMaskByName(sp.getString("ZeemoteMappingC", "None"));
    }

    protected int getControlMaskByName(String name) {
        if (name.equals("Fire")) {
            return Controls.FIRE;
        } else if (name.equals("NextWeapon")) {
            return Controls.NEXT_WEAPON;
        } else if (name.equals("ToggleMap")) {
            return Controls.TOGGLE_MAP;
        } else if (name.equals("Strafe")) {
            return Controls.STRAFE_MODE;
        } else {
            return 0;
        }
    }
}
