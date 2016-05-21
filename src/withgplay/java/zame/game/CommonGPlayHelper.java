package zame.game;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CommonGPlayHelper {
    public static final String FACEBOOK_PICTURE_LINK = "http://mobile.zame-dev.org/gloomy-ii/themes/default/images/facebook-icon.jpg";
    public static final String RATE_DISLIKE_LINK = "http://mobile.zame-dev.org/gloomy-ii/index.php?action=dislike&utm_medium=referral&utm_source=ingame&utm_campaign=ingame&hl=";

    private CommonGPlayHelper() {
    }

    public static boolean openMarket(Context context, String packageName) {
        try {
            context.startActivity((
                new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName))
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));

            return true;
        } catch (Exception ex) {
            Common.log(ex);
            Common.showToast("Could not launch the market application.");
        }

        return false;
    }
}
