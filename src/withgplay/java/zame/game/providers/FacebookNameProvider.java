package zame.game.providers;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import zame.game.BuildConfig;
import zame.game.Common;
import zame.game.MyApplication;

/*
    Info about permissions: https://developers.facebook.com/docs/authentication/permissions/
    When updating Facebook SDK, add following method inside Session class:

    // http://stackoverflow.com/questions/13897056/when-to-request-permissions-with-facebooks-new-android-sdk-3-0/14048644#14048644
    public static Session openActiveSession(Activity activity, boolean allowLoginUI, StatusCallback callback, List<String> permissions) {
        OpenRequest openRequest = new OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Builder(activity).build();

        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }

        return null;
    }
*/

public class FacebookNameProvider {
    private FacebookNameProvider() {
    }

    public interface IOnComplete {
        void onComplete(boolean updated);
    }

    public static void updateFacebookName(final Session session, final IOnComplete onComplete) {
        MyApplication.self.handler.post(new Runnable() {
            public void run() {
                if (MyApplication.self.gPlayHelper.facebookNameTaskActive) {
                    if (onComplete != null) {
                        try {
                            onComplete.onComplete(false);
                        } catch (Exception ex) {
                            Common.log(ex);
                        }
                    }

                    return;
                }

                MyApplication.self.gPlayHelper.facebookNameTaskActive = true;

                try {
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            MyApplication.self.gPlayHelper.facebookNameTaskActive = false;
                            boolean updated = false;

                            if (user != null && user.getName() != null && user.getName().length() != 0) { // length check just for case
                                if (!MyApplication.self.profile.playerName.equals(user.getName())) {
                                    MyApplication.self.profile.playerName = user.getName();
                                    MyApplication.self.profile.update();

                                    UpdateLeaderboardProvider.updateLeaderboard();
                                    updated = true;
                                }
                            }

                            if (onComplete != null) {
                                try {
                                    onComplete.onComplete(updated);
                                } catch (Exception ex) {
                                    Common.log(ex);
                                }
                            }
                        }
                    });
                } catch (Exception ex) {
                    Common.log(ex);
                    MyApplication.self.gPlayHelper.facebookNameTaskActive = false;

                    if (onComplete != null) {
                        try {
                            onComplete.onComplete(false);
                        } catch (Exception exi) {
                            Common.log(exi);
                        }
                    }
                }
            }
        });
    }
}
