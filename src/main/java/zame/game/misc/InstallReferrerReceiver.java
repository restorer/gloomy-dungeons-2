package zame.game.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReferrerReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		InstallReferrerReceiverGPlayHelper.onReceive(context, intent);
	}
}
