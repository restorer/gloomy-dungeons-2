package zame.game.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;
import zame.game.store.Store;
import zame.game.store.StoreAdapter;

public class StoreFragment extends BaseFragment implements ActionBarFragment, ActionBar.TabListener {
	protected ViewGroup viewGroup;
	protected Profile profile = null;
	protected boolean isActive = false;
	protected ListView itemsList;
	protected TextView creditsText = null;
	protected StoreAdapter adapter = null;

	public int storeCategory = Store.CATEGORY_LEVELS;

	protected final BroadcastReceiver profileUpdatedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			profileUpdated();
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.profile = MyApplication.self.profile;
	}

	@Override
	public void setupTabs(ActionBar actionBar) {
		addTab(actionBar, R.string.store_levels, Store.CATEGORY_LEVELS);
		addTab(actionBar, R.string.store_upgrade, Store.CATEGORY_UPGRADE);
		addTab(actionBar, R.string.store_additional, Store.CATEGORY_ADDITIONAL);

		if (Store.CATEGORIES[Store.CATEGORY_EARN].length != 0) {
			addTab(actionBar, R.string.store_earn, Store.CATEGORY_EARN);
		}
	}

	protected void addTab(ActionBar actionBar, int titleResourceId, int storeCategory) {
		actionBar.addTab(actionBar.newTab()
			.setText(getString(titleResourceId))
			.setTag(Integer.valueOf(storeCategory))
			.setTabListener(this)
		);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_store, container, false);

		itemsList = (ListView)viewGroup.findViewById(R.id.items);
		adapter = new StoreAdapter(activity, profile, storeCategory);
		itemsList.setAdapter(adapter);

		creditsText = (TextView)viewGroup.findViewById(R.id.credits);
		updateCredits();

		itemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Store.CATEGORIES[storeCategory][position].run(profile, activity, storeCategory, position);
			}
		});

		((Button)viewGroup.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.menuFragment);
			}
		});

		ensureSelectedTab();
		isActive = true;

		MyApplication.self.getLocalBroadcastManager().registerReceiver(
			profileUpdatedReceiver,
			new IntentFilter(Profile.BROADCAST_ACTION_UPDATED)
		);

		return viewGroup;
	}

	@Override
	public void onDestroyView() {
		isActive = false;
		MyApplication.self.getLocalBroadcastManager().unregisterReceiver(profileUpdatedReceiver);
		super.onDestroyView();
	}

	protected void updateCredits() {
		creditsText.setText(getString(R.string.store_credits, profile.credits));
	}

	protected void ensureSelectedTab() {
		ActionBar actionBar = activity.getSupportActionBar();

		if (actionBar == null || actionBar.getSelectedTab() == null || actionBar.getSelectedTab().getTag() == null) {
			return;
		}

		if ((Integer)actionBar.getSelectedTab().getTag() != storeCategory) {
			for (int i = 0, len = actionBar.getTabCount(); i < len; i++) {
				ActionBar.Tab tab = actionBar.getTabAt(i);

				if (tab != null && tab.getTag() != null && (Integer)tab.getTag() == storeCategory) {
					actionBar.selectTab(tab);
					break;
				}
			}
		}
	}

	public void changeCategory(int newStoreCategory) {
		if (storeCategory != newStoreCategory) {
			storeCategory = newStoreCategory;

			if (isActive) {
				adapter = new StoreAdapter(activity, profile, storeCategory);
				itemsList.setAdapter(adapter);

				// onTabSelected will not change adapter, because storeCategory was changed earlier
				ensureSelectedTab();
			}
		}
	}

	protected void profileUpdated() {
		if (profile != null) {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}

			if (creditsText != null) {
				updateCredits();
			}
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction) {
		if (isActive && ((Integer)tab.getTag() != storeCategory)) {
			storeCategory = (Integer)tab.getTag();
			adapter = new StoreAdapter(activity, profile, storeCategory);
			itemsList.setAdapter(adapter);
		}
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction) {
	}
}
