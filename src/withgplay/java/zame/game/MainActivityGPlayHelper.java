package zame.game;

import android.content.Intent;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.fragments.dialogs.RateGameDialogFragment;

public class MainActivityGPlayHelper {
    public RateGameDialogFragment rateGameDialogFragment;
    public boolean rateGameDialogShown;

    public void onCreate() {
        rateGameDialogFragment = RateGameDialogFragment.newInstance(true);
        rateGameDialogShown = false;
    }

    public boolean onBackPressed(MainActivity activity) {
        SharedPreferences sp = MyApplication.self.getSharedPreferences();

        if (!rateGameDialogShown && sp.getInt("QuitCount", 0) == 3 - 1) {
            rateGameDialogShown = true;
            rateGameDialogFragment.show(activity.getSupportFragmentManager());
            return false;
        }

        return true;
    }

    public void quitGame() {
        SharedPreferences sp = MyApplication.self.getSharedPreferences();
        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.putInt("QuitCount", sp.getInt("QuitCount", 0) + 1);
        spEdit.commit();
    }

    public void onActivityResult(MainActivity activity, int requestCode, int resultCode, Intent data) {
    }
}
