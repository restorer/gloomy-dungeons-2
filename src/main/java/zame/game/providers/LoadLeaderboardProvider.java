package zame.game.providers;

import android.content.Intent;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.store.Profile;

public class LoadLeaderboardProvider {
	public static final String BROADCAST_ACTION = "local:LoadLeaderboardProvider";

	public interface IOnComplete {
		void onComplete(boolean success);
	}

	public static class Task extends AsyncTask<Void,Void,Boolean> {
		protected IOnComplete onComplete;
		protected ArrayList<LeaderboardItem> resultItems = new ArrayList<LeaderboardItem>();
		protected boolean success = false;

		public Task(IOnComplete onComplete) {
			this.onComplete = onComplete;
		}

		protected Boolean doInBackground(Void... params) {
			Profile profile = MyApplication.self.profile;

			Object result = Api.leaderboard(
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

				List<?> list = Common.asList(result, "leaderboard");

				for (int i = 0, len = list.size(); i < len; i++) {
					Object item = list.get(i);

					resultItems.add(new LeaderboardItem(
						Common.asString(item, "uid"),
						Common.asInt(item, "exp"),
						Common.asString(item, "name"),
						Common.asInt(item, "achievementsCount")
					));
				}

				success = true;
			} else {
				success = false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (success) {
				MyApplication.self.leaderboard = resultItems;
			}

			onComplete.onComplete(success);
		}
	}

	public static void loadLeaderboard() {
		MyApplication.self.handler.post(new Runnable() {
			public void run() {
				if (MyApplication.self.loadLeaderboardTask != null) {
					return;
				}

				MyApplication.self.loadLeaderboardTask = new Task(new IOnComplete() {
					public void onComplete(boolean success) {
						MyApplication.self.loadLeaderboardTask = null;

						if (!success) {
							Common.showToast("Could not update leaderboard.");
						}

						MyApplication.self.getLocalBroadcastManager().sendBroadcast(new Intent(BROADCAST_ACTION));
					}
				});

				MyApplication.self.loadLeaderboardTask.execute();
			}
		});
	}
}
