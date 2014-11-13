package zame.game.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ProgressBar;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.providers.LeaderboardAdapter;
import zame.game.providers.LoadLeaderboardProvider;
import zame.game.providers.UpdateLeaderboardProvider;
import zame.game.store.AchievementsAdapter;
import zame.game.store.Profile;

public class AchievementsFragment extends BaseFragment implements ActionBarFragment, ActionBar.TabListener {
	protected final static int TAG_ACHIEVEMENTS = 1;
	protected final static int TAG_LEADERBOARD = 2;

	protected ViewGroup viewGroup;
	protected Profile profile = null;
	protected boolean isActive = false;
	protected ListView itemsList;
	protected ProgressBar progressBar;
	protected BaseAdapter adapter = null;
	protected int currentTag = TAG_ACHIEVEMENTS;
	protected AchievementsFragmentGPlayHelper gPlayHelper = new AchievementsFragmentGPlayHelper();

	protected final BroadcastReceiver leaderboardLoadedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshAdapter();
			updatePlayerName();
		}
	};

	protected final BroadcastReceiver leaderboardUpdatedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshAdapter();
			updatePlayerName();
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.profile = MyApplication.self.profile;
	}

	@Override
	public void setupTabs(ActionBar actionBar) {
		actionBar.addTab(actionBar.newTab()
			.setText(getString(R.string.achievements))
			.setTag(TAG_ACHIEVEMENTS)
			.setTabListener(this)
		);

		actionBar.addTab(actionBar.newTab()
			.setText(getString(R.string.leaderboard))
			.setTag(TAG_LEADERBOARD)
			.setTabListener(this)
		);
	}

	@Override
	public View createFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_achievements, container, false);

		itemsList = (ListView)viewGroup.findViewById(R.id.items);
		progressBar = (ProgressBar)viewGroup.findViewById(R.id.progress);

		refreshAdapter();

		((Button)viewGroup.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.menuFragment);
			}
		});

		((TextView)viewGroup.findViewById(R.id.exp)).setText(getString(R.string.achievements_exp, profile.exp));

		gPlayHelper.createFragmentView(this, viewGroup, activity);
		updatePlayerName();
		ensureSelectedTab();
		isActive = true;

		MyApplication.self.getLocalBroadcastManager().registerReceiver(
			leaderboardLoadedReceiver,
			new IntentFilter(LoadLeaderboardProvider.BROADCAST_ACTION)
		);

		MyApplication.self.getLocalBroadcastManager().registerReceiver(
			leaderboardUpdatedReceiver,
			new IntentFilter(UpdateLeaderboardProvider.BROADCAST_ACTION)
		);

		return viewGroup;
	}

	protected void refreshAdapter() {
		if (currentTag == TAG_LEADERBOARD) {
			adapter = new LeaderboardAdapter(activity, profile);
			itemsList.setAdapter(adapter);

			if (MyApplication.self.leaderboard == null) {
				progressBar.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE); // fix bug
				itemsList.setVisibility(View.GONE);
				LoadLeaderboardProvider.loadLeaderboard();
			} else {
				progressBar.setVisibility(View.GONE);
				itemsList.setVisibility(View.VISIBLE);
			}
		} else {
			adapter = new AchievementsAdapter(activity, profile);
			itemsList.setAdapter(adapter);

			progressBar.setVisibility(View.GONE);
			itemsList.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		isActive = false;
		MyApplication.self.getLocalBroadcastManager().unregisterReceiver(leaderboardLoadedReceiver);
		MyApplication.self.getLocalBroadcastManager().unregisterReceiver(leaderboardUpdatedReceiver);
		super.onDestroyView();
	}

	public void updatePlayerName() {
		String playerName = MyApplication.self.profile.playerName;
		((TextView)viewGroup.findViewById(R.id.player_name)).setText(playerName);
		gPlayHelper.updatePlayerName(viewGroup, playerName);
	}

	protected void ensureSelectedTab() {
		ActionBar actionBar = activity.getSupportActionBar();

		if (actionBar == null || actionBar.getSelectedTab() == null || actionBar.getSelectedTab().getTag() == null) {
			return;
		}

		if ((Integer)actionBar.getSelectedTab().getTag() != currentTag) {
			for (int i = 0, len = actionBar.getTabCount(); i < len; i++) {
				ActionBar.Tab tab = actionBar.getTabAt(i);

				if (tab != null && tab.getTag() != null && (Integer)tab.getTag() == currentTag) {
					actionBar.selectTab(tab);
					break;
				}
			}
		}
	}

	public void changeTag(int newTag) {
		if (currentTag != newTag) {
			currentTag = newTag;

			if (isActive) {
				refreshAdapter();
				ensureSelectedTab(); // onTabSelected will not change adapter, because currentTag was changed earlier
			}
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction) {
		if (isActive && ((Integer)tab.getTag() != currentTag)) {
			currentTag = (Integer)tab.getTag();
			refreshAdapter();
		}
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction) {
	}
}
