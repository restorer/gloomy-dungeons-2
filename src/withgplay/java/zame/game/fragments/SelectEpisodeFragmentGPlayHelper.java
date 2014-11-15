package zame.game.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import zame.game.Common;
import zame.game.CommonGPlayHelper;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.State;
import zame.game.managers.SoundManager;
import zame.game.providers.Api;
import zame.game.providers.FacebookNameProvider;
import zame.game.store.Profile;
import zame.game.store.ProfileLevel;

public class SelectEpisodeFragmentGPlayHelper {
    public void createFragmentView(ViewGroup viewGroup, final MainActivity activity, final State state, final Profile profile) {
        ((ImageView)viewGroup.findViewById(R.id.share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Share.Facebook", state.levelName, 0);
                postToFacebook(activity, state, profile);
            }
        });
    }

    public void updateImages(ViewGroup viewGroup, ProfileLevel level) {
        if (level.storeEpisodeId == -1) {
            ((ImageView)viewGroup.findViewById(R.id.share)).setVisibility(View.GONE);
        }
    }

    protected void postToFacebook(final MainActivity activity, final State state, final Profile profile) {
        Session session = new Session(activity);
        Session.setActiveSession(session);

        try {
            session.openForRead(new Session.OpenRequest(activity).setCallback(new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState facebookState, Exception exception) {
                    if (session.isClosed()) {
                        session.removeCallback(this);
                        return;
                    }

                    if (!session.isOpened()) {
                        return;
                    }

                    session.removeCallback(this);

                    FacebookNameProvider.updateFacebookName(session, null);
                    Bundle params = new Bundle();

                    params.putString("name", activity.getString(R.string.share_fb_name));
                    params.putString("caption", activity.getString(R.string.share_fb_caption));
                    params.putString("link", Api.getUserinfoUrl());
                    params.putString("picture", CommonGPlayHelper.FACEBOOK_PICTURE_LINK);

                    params.putString("description", String.format(
                        activity.getString(R.string.share_fb_description),
                        profile.exp, // 1
                        state.levelName, // 2
                        state.overallMonsters, // 3
                        state.overallItems, // 4
                        state.overallSecrets, // 5
                        Common.getTimeString(state.overallSeconds) // 6
                    ));

                    try {
                        WebDialog feedDialog =
                            (new WebDialog.FeedDialogBuilder(activity, Session.getActiveSession(), params))
                            .build();

                        feedDialog.show();
                    } catch (Exception ex) {
                        Common.log(ex);
                    }
                }
            }));
        } catch (Exception ex) {
            Common.log(ex);
        }
    }
}
