package zame.game.providers;

import android.content.Intent;
import android.os.AsyncTask;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.Common;
import zame.game.MyApplication;

public class UpdateLatestVersionCodeProvider {
	public static final String BROADCAST_ACTION = "local:UpdateLatestVersionCodeProvider";

	private UpdateLatestVersionCodeProvider() {
	}

	public interface IOnComplete {
		void onComplete(int versionCode, String downloadUrl);
	}

	public static class Task extends AsyncTask<Void,Void,Boolean> {
		protected IOnComplete onComplete;
		protected int versionCode;
		protected String downloadUrl = "";

		public Task(IOnComplete onComplete) {
			this.onComplete = onComplete;
		}

		protected Boolean doInBackground(Void... params) {
			Object result = Api.version();

			if (result instanceof Api.HttpStatusCode) {
				versionCode = 0;
				downloadUrl = "";
			} else {
				versionCode = Common.asInt(result, "versionCode");
				downloadUrl = Common.asString(result, "downloadUrl");
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			onComplete.onComplete(versionCode, downloadUrl);
		}
	}

	public static void updateLatestVersionCode() {
		MyApplication.self.handler.post(new Runnable() {
			public void run() {
				if ((MyApplication.self.updateLatestVersionCodeTask != null) ||
					(MyApplication.self.getSharedPreferences().getLong("LatestVersionCodeNextCheck", 0) > System.currentTimeMillis())
				) {
					return;
				}

				MyApplication.self.updateLatestVersionCodeTask = new Task(new IOnComplete() {
					public void onComplete(int versionCode, String downloadUrl) {
						MyApplication.self.updateLatestVersionCodeTask = null;

						if (versionCode > 0) {
							SharedPreferences.Editor spEditor = MyApplication.self.getSharedPreferences().edit();
							spEditor.putInt("LatestVersionCode", versionCode);
							spEditor.putString("NewVersionDownloadUrl", downloadUrl);
							spEditor.putLong("LatestVersionCodeNextCheck", System.currentTimeMillis() + 12*60*60*1000);
							spEditor.commit();

							MyApplication.self.getLocalBroadcastManager().sendBroadcast(new Intent(BROADCAST_ACTION));
						}
					}
				});

				MyApplication.self.updateLatestVersionCodeTask.execute();
			}
		});
	}
}
