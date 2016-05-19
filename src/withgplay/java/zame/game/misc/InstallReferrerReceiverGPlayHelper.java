package zame.game.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import zame.game.Common;

public class InstallReferrerReceiverGPlayHelper {
    private InstallReferrerReceiverGPlayHelper() {
    }

    public static void onReceive(Context context, Intent intent) {
        try {
            BroadcastReceiver campaignReceiver = new com.google.analytics.tracking.android.CampaignTrackingReceiver();
            campaignReceiver.onReceive(context, intent);
        } catch (Exception ex) {
            Common.log(ex);
        }
    }
}
