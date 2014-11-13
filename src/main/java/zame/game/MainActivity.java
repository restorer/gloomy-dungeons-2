package zame.game;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import org.holoeverywhere.app.Activity;
import zame.game.engine.Engine;
import zame.game.engine.State;
import zame.game.fragments.AchievementsFragment;
import zame.game.fragments.ActionBarFragment;
import zame.game.fragments.BaseFragment;
import zame.game.fragments.EodBlockerFragment;
import zame.game.fragments.GameFragment;
import zame.game.fragments.MenuFragment;
import zame.game.fragments.OptionsFragment;
import zame.game.fragments.PrepareFragment;
import zame.game.fragments.SelectEpisodeFragment;
import zame.game.fragments.StoreFragment;
import zame.game.fragments.dialogs.QuitWarnDialogFragment;
import zame.game.managers.SoundManager;
import zame.game.managers.Tracker;
import zame.game.misc.ViewServer;
import zame.game.providers.CachedTexturesProvider;
import zame.game.providers.DlcProvider;
import zame.game.store.Profile;

public class MainActivity extends Activity {
	public static MainActivity self = null;

	public SoundManager soundManager;
	public Tracker tracker;
	public Engine engine;
	public MenuFragment menuFragment;
	public SelectEpisodeFragment selectEpisodeFragment;
	public GameFragment gameFragment;
	public OptionsFragment optionsFragment;
	public EodBlockerFragment eodBlockerFragment;
	public StoreFragment storeFragment;
	public AchievementsFragment achievementsFragment;
	public PrepareFragment prepareFragment;
	public QuitWarnDialogFragment quitWarnDialogFragment;

	protected Profile profile;
	protected Fragment currentFragment = null;
	protected Fragment prevFragment = null;
	protected ActionBarFragment prevActionBarFragment = null;
	protected final Handler handler = new Handler();

	protected MainActivityGPlayHelper gPlayHelper = new MainActivityGPlayHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		soundManager = SoundManager.getInstance(false);
		soundManager.initialize();

		tracker = Tracker.getInstance(false);
		engine = new Engine(this);

		menuFragment = new MenuFragment();
		selectEpisodeFragment = new SelectEpisodeFragment();
		gameFragment = new GameFragment();
		optionsFragment = new OptionsFragment();
		eodBlockerFragment = new EodBlockerFragment();
		storeFragment = new StoreFragment();
		achievementsFragment = new AchievementsFragment();
		prepareFragment = new PrepareFragment();

		quitWarnDialogFragment = QuitWarnDialogFragment.newInstance();
		gPlayHelper.onCreate();

		/*
		android.content.res.Configuration config = getBaseContext().getResources().getConfiguration();
		java.util.Locale locale = new java.util.Locale("be");
		java.util.Locale.setDefault(locale);
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		*/

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		profile = MyApplication.self.profile;
		self = this;

		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();

		// ViewServer.get(this).addWindow(this);

		if (MyApplication.self.cachedTexturesTask != null
			|| CachedTexturesProvider.needToUpdateCache()
			|| MyApplication.self.dlcTask != null
			|| DlcProvider.needToDownloadMusic()
		) {
			showFragment(prepareFragment);
		} else {
			showFragment(menuFragment);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ViewServer.get(this).setFocusedWindow(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		soundManager.onWindowFocusChanged(hasFocus, SoundManager.FOCUS_MASK_MAIN_ACTIVITY);

		if ((currentFragment != null) && (currentFragment instanceof BaseFragment) && currentFragment.isVisible()) {
			((BaseFragment)currentFragment).onWindowFocusChanged(hasFocus);
		}
	}

	@Override
	protected void onPause() {
		soundManager.onWindowFocusChanged(false, SoundManager.FOCUS_MASK_MAIN_ACTIVITY);

		// if home screen button pressed, lock screen button pressed, call received or something similar - return to menu screen
		if (currentFragment == gameFragment) {
			gameFragment.hideDialogs();
			showFragment(menuFragment);
		}

		super.onPause();
	}

	public synchronized void showFragment(final Fragment fragment) {
		handler.post(new Runnable() {
			public void run() {
				showFragmentInternal(fragment);
			}
		});
	}

	protected void showFragmentInternal(Fragment fragment) {
		if (fragment == currentFragment) {
			return;
		}

		if (currentFragment == optionsFragment) {
			engine.config.reload();
		}

		prevFragment = currentFragment;
		currentFragment = fragment;

		try {
			getSupportFragmentManager().executePendingTransactions(); // fix for black-screen issue
		} catch (Exception ex) {
			Common.log(ex.toString());
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, fragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE); // fix for cyanogenmod

		try {
			transaction.commit();
		} catch (Exception ex) {
			try {
				transaction.commitAllowingStateLoss();
			} catch (Exception exi) {
				Common.log(exi.toString());
			}
		}
	}

	public void setupTabs() {
		ActionBar actionBar = getSupportActionBar();

		if (currentFragment instanceof ActionBarFragment) {
			if (currentFragment != prevActionBarFragment) {
				actionBar.removeAllTabs();

				try {
					((ActionBarFragment)currentFragment).setupTabs(actionBar);
				} catch (Exception ex) {
					// java.lang.IllegalStateException: Fragment ao{40866688} not attached to Activity
					Common.log(ex);
				}
			}

			prevActionBarFragment = (ActionBarFragment)currentFragment;

			actionBar.setDisplayOptions(0);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.show();
		} else {
			actionBar.hide();
		}
	}

	public void showPrevFragment() {
		handler.post(new Runnable() {
			public void run() {
				if (prevFragment == null || prevFragment == currentFragment) {
					showFragmentInternal(menuFragment);
				} else {
					showFragmentInternal(prevFragment);
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (currentFragment == prepareFragment) {
			return;
		}

		if ((currentFragment != null) && (currentFragment instanceof BaseFragment) && currentFragment.isVisible()) {
			((BaseFragment)currentFragment).onBackPressed();
		}

		if (currentFragment != null && currentFragment != menuFragment) {
			showFragment(menuFragment);
			return;
		}

		if (!gPlayHelper.onBackPressed(this)) {
			return;
		}

		quitWarnDialogFragment.show(getSupportFragmentManager());
		// super.onBackPressed();
	}

	public void quitGame() {
		gPlayHelper.quitGame();
		finish();
	}

	@Override
	protected void onDestroy() {
		self = null;
		soundManager.shutdown();

		super.onDestroy();
		// ViewServer.get(this).removeWindow(this);
	}

	public void startGame(String saveName) {
		engine.game.savedGameParam = saveName;
		showFragment(gameFragment);
	}

	public boolean tryAndLoadInstantState() {
		if (!engine.hasInstantSave()) {
			engine.state.init();
			return false;
		}

		engine.state.init();
		return (engine.state.load(engine.instantName) == State.LOAD_RESULT_SUCCESS);
	}

	public void continueGame() {
		engine.game.savedGameParam = (engine.hasInstantSave() ? engine.instantName : "");
		showFragment(gameFragment);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		gPlayHelper.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
