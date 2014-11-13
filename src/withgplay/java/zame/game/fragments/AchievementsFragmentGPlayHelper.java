package zame.game.fragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.facebook.Session;
import com.facebook.SessionState;
import zame.game.BuildConfig;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.providers.FacebookNameProvider;

public class AchievementsFragmentGPlayHelper {
    public void createFragmentView(final AchievementsFragment achievementsFragment, ViewGroup viewGroup, final MainActivity activity) {
        ((Button)viewGroup.findViewById(R.id.use_facebook_name)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "FacebookName.Pressed", "", 0);
                useNameFromFacebook(achievementsFragment, activity);
            }
        });
    }

    public void updatePlayerName(ViewGroup viewGroup, String playerName) {
        ((Button)viewGroup.findViewById(R.id.use_facebook_name)).setVisibility(playerName.matches(".+ #[0-9]+") ? View.VISIBLE : View.GONE);
    }

    // http://stackoverflow.com/questions/16176468/my-android-application-for-a-facebook-login-wont-open-a-session-if-i-have-facebo
    // http://stackoverflow.com/questions/13894006/android-facebook-sdk-3-0-gives-remote-app-id-does-not-match-stored-id-while-lo

    protected void useNameFromFacebook(final AchievementsFragment achievementsFragment, final MainActivity activity) {
        Session.openActiveSession(activity, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState facebookState, Exception exception) {
                if (BuildConfig.DEBUG) {
                    Common.log("AchievementsFragment.useNameFromFacebook [1]");
                }

                if (session.isOpened()) {
                    if (BuildConfig.DEBUG) {
                        Common.log("AchievementsFragment.useNameFromFacebook [2]");
                    }

                    FacebookNameProvider.updateFacebookName(session, new FacebookNameProvider.IOnComplete() {
                        public void onComplete(boolean updated) {
                            activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "FacebookName.Updated", "", 0);
                            achievementsFragment.updatePlayerName();
                        }
                    });
                }
            }
        });
    }
}
