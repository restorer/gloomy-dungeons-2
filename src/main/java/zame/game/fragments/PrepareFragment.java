package zame.game.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.holoeverywhere.widget.ProgressBar;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.fragments.BaseFragment;
import zame.game.providers.CachedTexturesProvider;
import zame.game.providers.DlcProvider;

public class PrepareFragment extends BaseFragment {
	protected ViewGroup viewGroup;
	protected ProgressBar progressView;
	protected TextView infoView;
	protected Button cancelBtn;

	protected final BroadcastReceiver cacheUpdateProgressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int progress = intent.getIntExtra(CachedTexturesProvider.EXTRA_PROGRESS, 0);

			if (progress > 100) {
				if (MyApplication.self.dlcTask == null && !DlcProvider.needToDownloadMusic()) {
					activity.showFragment(activity.menuFragment);
				} else {
					startDownload();
				}
			} else {
				progressView.setProgress(progress);
			}
		}
	};

	protected final BroadcastReceiver musicDownloadProgressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int progress = intent.getIntExtra(DlcProvider.EXTRA_PROGRESS, 0);

			if (progress > 100) {
				activity.showFragment(activity.menuFragment);
			} else {
				progressView.setProgress(progress);
			}
		}
	};

	@Override
	public View createFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_prepare, container, false);

		progressView = (ProgressBar)viewGroup.findViewById(R.id.progress);
		infoView = (TextView)viewGroup.findViewById(R.id.info);
		cancelBtn = (Button)viewGroup.findViewById(R.id.cancel);

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (MyApplication.self.dlcTask != null) {
					MyApplication.self.dlcTask.cancel(false);
				}

				activity.showFragment(activity.menuFragment);
			}
		});

		boolean needToUpdateCache = (MyApplication.self.cachedTexturesTask != null || CachedTexturesProvider.needToUpdateCache());
		boolean needToDownloadMusic = (MyApplication.self.dlcTask != null || DlcProvider.needToDownloadMusic());

		if (!needToUpdateCache && !needToDownloadMusic) {
			activity.showFragment(activity.menuFragment);
		} else {
			MyApplication.self.getLocalBroadcastManager().registerReceiver(
				cacheUpdateProgressReceiver,
				new IntentFilter(CachedTexturesProvider.BROADCAST_ACTION)
			);

			MyApplication.self.getLocalBroadcastManager().registerReceiver(
				musicDownloadProgressReceiver,
				new IntentFilter(DlcProvider.BROADCAST_ACTION)
			);

			if (MyApplication.self.cachedTexturesTask == null && needToUpdateCache) {
				CachedTexturesProvider.updateCache();
			}

			if (!needToUpdateCache) {
				startDownload();
			}
		}

		return viewGroup;
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.soundManager.setPlaylist(null);
	}

	@Override
	public void onDestroyView() {
		MyApplication.self.getLocalBroadcastManager().unregisterReceiver(cacheUpdateProgressReceiver);
		MyApplication.self.getLocalBroadcastManager().unregisterReceiver(musicDownloadProgressReceiver);
		super.onDestroyView();
	}

	protected void startDownload() {
		progressView.setProgress(0);
		infoView.setText(activity.getString(R.string.prep_downloading_music));
		cancelBtn.setVisibility(View.VISIBLE);

		if (MyApplication.self.dlcTask == null) {
			DlcProvider.downloadMusic();
		}
	}
}
