package zame.game.providers;

import android.content.Intent;
import android.os.AsyncTask;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.store.Profile;

public class UpdateLeaderboardProvider {
	public static final String BROADCAST_ACTION = "local:UpdateLeaderboardProvider";

	private UpdateLeaderboardProvider() {
	}

	public interface IOnComplete {
		void onComplete();
	}

	public static class Task extends AsyncTask<Void,Void,Boolean> {
		protected IOnComplete onComplete;
		protected boolean success = false;

		public Task(IOnComplete onComplete) {
			this.onComplete = onComplete;
		}

		protected Boolean doInBackground(Void... params) {
			Profile profile = MyApplication.self.profile;

			Object result = Api.update(
				profile.playerUid,
				profile.exp,
				profile.playerName,
				profile.getAchievementsIdsString()
			);

			if (!(result instanceof Api.HttpStatusCode)) {
				String playerUid = Common.asString(result, "uid");
				String playerName = Common.asString(result, "name");

				if (!profile.playerUid.equals(playerUid) || !profile.playerName.equals(playerName)) {
					profile.playerUid = playerUid;
					profile.playerName = playerName;
					profile.update();
				}

				success = true;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (success) {
				MyApplication.self.leaderboard = null;
			}

			onComplete.onComplete();
		}
	}

	private UpdateLeaderboardProvider() {
	}

	public static void updateLeaderboard() {
		MyApplication.self.handler.post(new Runnable() {
			public void run() {
				if (MyApplication.self.updateLeaderboardTask != null) {
					return;
				}

				MyApplication.self.updateLeaderboardTask = new Task(new IOnComplete() {
					public void onComplete() {
						MyApplication.self.updateLeaderboardTask = null;
						MyApplication.self.getLocalBroadcastManager().sendBroadcast(new Intent(BROADCAST_ACTION));
					}
				});

				MyApplication.self.updateLeaderboardTask.execute();
			}
		});
	}
}
