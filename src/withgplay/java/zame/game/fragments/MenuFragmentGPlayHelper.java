package zame.game.fragments;

import android.view.View;
import android.view.ViewGroup;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.fragments.dialogs.RateGameDialogFragment;
import zame.game.managers.SoundManager;

public class MenuFragmentGPlayHelper {
    protected RateGameDialogFragment rateGameDialogFragment;
    protected ViewGroup rateGameWrap;

    public void onCreate() {
        rateGameDialogFragment = RateGameDialogFragment.newInstance(false);
    }

    public void createFragmentView(ViewGroup viewGroup, final MainActivity activity) {
        rateGameWrap = (ViewGroup)viewGroup.findViewById(R.id.rate_game_wrap);

        rateGameWrap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "BarLine.Rate", "", 0);
                rateGameDialogFragment.show(activity.getSupportFragmentManager());
            }
        });

        updateRateWrapVisibility();
    }

    public void updateRateWrapVisibility() {
        SharedPreferences sp = MyApplication.self.getSharedPreferences();

        if (sp.getBoolean("QuitWithoutRate", false) && !sp.getBoolean("RateAtLeastOnce", false)) {
            rateGameWrap.setVisibility(View.VISIBLE);
        } else {
            rateGameWrap.setVisibility(View.GONE);
        }
    }

    public void hideRateWrap() {
        rateGameWrap.setVisibility(View.GONE);
    }
}
