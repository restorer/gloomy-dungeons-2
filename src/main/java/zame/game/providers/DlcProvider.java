package zame.game.providers;

import android.content.Intent;
import android.os.AsyncTask;
import java.io.File;
import java.util.ArrayList;
import zame.game.BuildConfig;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;

public class DlcProvider {
	public static final String BROADCAST_ACTION = "local:DlcProvider";
	public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";

	public interface IOnComplete {
		void onComplete();
	}

	public static class Task extends AsyncTask<Void,Integer,Void> implements Api.IDownloadManager {
		protected IOnComplete onComplete;
		protected int progress = 0;
		protected long downloadedSize = 0L;
		protected long prevDownloadedSize = 0L;
		protected long totalSize = 1L;

		public Task(IOnComplete onComplete) {
			this.onComplete = onComplete;
		}

		protected Void doInBackground(Void... params) {
			ArrayList<String> downloadList = new ArrayList<String>();

			for (int i = 0, len = SoundManager.LIST_MAIN.list.length; i < len; i++) {
				String name = SoundManager.LIST_MAIN.list[i];

				if (name.length() > 4 && "dlc_".equals(name.substring(0, 4))) {
					downloadList.add(name.substring(4));
				}
			}

			Object result = Api.dlcTotalSize(downloadList);

			if (result instanceof Api.HttpStatusCode) {
				Common.showToast(R.string.prep_download_error);
				return null;
			}

			totalSize = Common.asLong(result, "total");

			if (totalSize <= 0) {
				Common.showToast(R.string.prep_download_error);
				return null;
			}

			for (String name : downloadList) {
				if (isCancelled()) {
					if (BuildConfig.DEBUG) {
						Common.log("DlcProvider.Task.doInBackground: cancelled");
					}

					break;
				}

				String filePath = MyApplication.self.INTERNAL_ROOT + "dlc_" + name;
				File file = new File(filePath);

				if (file.exists()) {
					if (BuildConfig.DEBUG) {
						Common.log("DlcProvider.Task.doInBackground: [" + name + "] already downloaded");
					}

					onPartDownloaded(file.length());
					prevDownloadedSize = downloadedSize;
					continue;
				}

				String partPath = filePath + ".part";

				if (Api.downloadFile(partPath, name, this) == Api.STATUS_CODE_OK) {
					if (BuildConfig.DEBUG) {
						Common.log("DlcProvider.Task.doInBackground: [" + name + "] successfully downloaded");
					}

					Common.safeRename(partPath, filePath);
					prevDownloadedSize = downloadedSize;
				} else if (!isCancelled()) {
					Common.showToast(R.string.prep_download_error);
					break;
				}
			}

			if (BuildConfig.DEBUG) {
				Common.log("DlcProvider.Task.doInBackground: done");
			}

			return null;
		}

		@Override
		public void onDownloadStarted() {
			downloadedSize = prevDownloadedSize;
			progress = (int)((float)downloadedSize / (float)totalSize * 100.0f);
			publishProgress(progress);
		}

		@Override
		public void onTotalSizeRetrieved(long totalSize) {
			// do nothing, because we already know total size
		}

		@Override
		public void onPartDownloaded(long partSize) {
			downloadedSize += partSize;
			int newProgress = (int)((float)downloadedSize / (float)totalSize * 100.0f);

			if (newProgress > progress) {
				progress = newProgress;
				publishProgress(progress);
			}
		}

		protected void onProgressUpdate(Integer... progress) {
			MyApplication.self.getLocalBroadcastManager().sendBroadcast(
				(new Intent(BROADCAST_ACTION))
				.putExtra(EXTRA_PROGRESS, progress[0])
			);
		}

		protected void onPostExecute(Void result) {
			onComplete.onComplete();
		}

		protected void onCancelled(Void result) {
			onComplete.onComplete();
		}

		// support for android 2.x
		protected void onCancelled() {
			onComplete.onComplete();
		}
	}

	private DlcProvider() {
	}

	public static boolean needToDownloadMusic() {
		for (int i = 0, len = SoundManager.LIST_MAIN.list.length; i < len; i++) {
			String name = SoundManager.LIST_MAIN.list[i];

			if (name.length() > 4 && "dlc_".equals(name.substring(0, 4))) {
				if (!(new File(MyApplication.self.INTERNAL_ROOT + name)).exists()) {
					return true;
				}
			}
		}

		return false;
	}

	public static void downloadMusic() {
		MyApplication.self.handler.post(new Runnable() {
			public void run() {
				if (MyApplication.self.dlcTask != null) {
					return;
				}

				MyApplication.self.dlcTask = new Task(new IOnComplete() {
					public void onComplete() {
						if (BuildConfig.DEBUG) {
							Common.log("DlcProvider.downloadMusic: complete");
						}

						MyApplication.self.dlcTask = null;

						MyApplication.self.getLocalBroadcastManager().sendBroadcast(
							(new Intent(BROADCAST_ACTION))
							.putExtra(EXTRA_PROGRESS, 101)
						);
					}
				});

				if (BuildConfig.DEBUG) {
					Common.log("DlcProvider.downloadMusic: executing");
				}

				MyApplication.self.dlcTask.execute();
			}
		});
	}
}
