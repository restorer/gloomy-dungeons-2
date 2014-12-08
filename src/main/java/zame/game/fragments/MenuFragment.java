package zame.game.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.fragments.dialogs.LoadDialogFragment;
import zame.game.fragments.dialogs.SaveDialogFragment;
import zame.game.fragments.dialogs.ShareDialogFragment;
import zame.game.managers.SoundManager;
import zame.game.providers.UpdateLatestVersionCodeProvider;
import zame.game.store.Profile;
import zame.game.store.Store;

public class MenuFragment extends BaseFragment {
	protected static final long DISCOUNT_OFFER_HOURS = 48;

	protected ViewGroup viewGroup;
	protected LoadDialogFragment loadDialogFragment;
	protected SaveDialogFragment saveDialogFragment;
	protected ShareDialogFragment shareDialogFragment;
	protected Profile profile;
	protected ViewGroup discountOfferWrapView;
	protected TextView discountOfferTimeView = null;
	protected Timer discountOfferTimer = null;
	protected TimerTask discountOfferTimerTask = null;
	protected final Handler handler = new Handler();
	protected ViewGroup updateAvailableWrap;
	protected MenuFragmentGPlayHelper gPlayHelper = new MenuFragmentGPlayHelper();

	protected final Runnable updateDiscountOfferRunnable = new Runnable() {
		public void run() {
			updateDiscountOffer();
		}
	};

	protected final BroadcastReceiver latestVersionCodeUpdatedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateNewVersionBar();
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.profile = MyApplication.self.profile;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadDialogFragment = LoadDialogFragment.newInstance();
		saveDialogFragment = SaveDialogFragment.newInstance();
		shareDialogFragment = ShareDialogFragment.newInstance();

		gPlayHelper.onCreate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_menu, container, false);

		((ImageButton)viewGroup.findViewById(R.id.play)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				if (!activity.tryAndLoadInstantState() || activity.engine.state.showEpisodeSelector) {
					activity.showFragment(activity.selectEpisodeFragment);
				} else {
					activity.continueGame();
				}
			}
		});

		((ImageButton)viewGroup.findViewById(R.id.load)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				loadDialogFragment.show(getFragmentManager());
			}
		});

		((ImageButton)viewGroup.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				saveDialogFragment.show(getFragmentManager());
			}
		});

		((ImageButton)viewGroup.findViewById(R.id.options)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.optionsFragment);
			}
		});

		((ImageButton)viewGroup.findViewById(R.id.store)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.storeFragment);
			}
		});

		((ImageButton)viewGroup.findViewById(R.id.achievements)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MyApplication.self.leaderboard = null;
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.achievementsFragment);
			}
		});

		((ImageButton)viewGroup.findViewById(R.id.share)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				shareDialogFragment.show(getFragmentManager());
			}
		});

		discountOfferWrapView = (ViewGroup)viewGroup.findViewById(R.id.discount_offer_wrap);

		if (profile.discountOfferTime < 0) {
			discountOfferWrapView.setVisibility(View.GONE);
		} else {
			if (profile.discountOfferTime == 0) {
				profile.discountOfferTime = System.currentTimeMillis() / 1000L;
				profile.discountOfferSuccess = false;
				profile.update();
			}

			discountOfferTimeView = (TextView)viewGroup.findViewById(R.id.discount_offer_time);
			updateDiscountOffer();

			discountOfferWrapView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
					activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "BarLine.Discount", "", 0);

					activity.storeFragment.storeCategory = Store.CATEGORY_EARN;
					activity.showFragment(activity.storeFragment);
				}
			});

			if (profile.discountOfferSuccess) {
				discountOfferWrapView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.alpha));
			}
		}

		updateAvailableWrap = (ViewGroup)viewGroup.findViewById(R.id.update_available_wrap);

		updateAvailableWrap.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "BarLine.Update", "", 0);
				Common.openBrowser(activity, MyApplication.self.getSharedPreferences().getString("NewVersionDownloadUrl", ""));
			}
		});

		gPlayHelper.createFragmentView(viewGroup, activity);
		updateButtons();

		return viewGroup;
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
		startTask();

		updateNewVersionBar();

		MyApplication.self.getLocalBroadcastManager().registerReceiver(
			latestVersionCodeUpdatedReceiver,
			new IntentFilter(UpdateLatestVersionCodeProvider.BROADCAST_ACTION)
		);

		UpdateLatestVersionCodeProvider.updateLatestVersionCode();
	}

	@Override
	public void onPause() {
		stopTask();
		MyApplication.self.getLocalBroadcastManager().unregisterReceiver(latestVersionCodeUpdatedReceiver);

		super.onPause();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (hasWindowFocus) {
			updateButtons();
			gPlayHelper.updateRateWrapVisibility();
			startTask();
		} else {
			stopTask();
		}
	}

	protected void updateNewVersionBar() {
		if (MyApplication.self.getSharedPreferences().getInt("LatestVersionCode", 0) > MyApplication.self.getVersionCode()
			&& MyApplication.self.getSharedPreferences().getString("NewVersionDownloadUrl", "").length() != 0
		) {
			updateAvailableWrap.setVisibility(View.VISIBLE);
			gPlayHelper.hideRateWrap();
		} else {
			updateAvailableWrap.setVisibility(View.GONE);
			gPlayHelper.updateRateWrapVisibility();
		}
	}

	protected void updateButtons() {
		((ImageButton)viewGroup.findViewById(R.id.save)).setEnabled(activity.engine.hasInstantSave());
		((ImageButton)viewGroup.findViewById(R.id.load)).setEnabled(Common.fillSlots(activity, null, null, true) > 0);
	}

	protected synchronized void startTask() {
		if (profile.discountOfferTime <= 0) {
			return;
		}

		if (discountOfferTimerTask == null) {
			discountOfferTimerTask = new TimerTask() {
				public void run() {
					handler.post(updateDiscountOfferRunnable);
				}
			};

			if (discountOfferTimer != null) {
				discountOfferTimer.cancel();
			}

			discountOfferTimer = new Timer();
			discountOfferTimer.schedule(discountOfferTimerTask, 250, 250);
		}
	}

	protected synchronized void stopTask() {
		if (discountOfferTimerTask != null) {
			discountOfferTimerTask.cancel();
			discountOfferTimerTask = null;
		}

		if (discountOfferTimer != null) {
			discountOfferTimer.cancel();
			discountOfferTimer = null;
		}
	}

	protected void updateDiscountOffer() {
		if (profile.discountOfferTime <= 0 || discountOfferTimeView == null) {
			return;
		}

		int timeLeft = (int)(profile.discountOfferTime + DISCOUNT_OFFER_HOURS * 60L * 60L - (System.currentTimeMillis() / 1000));

		if (timeLeft <= 0) {
			discountOfferWrapView.clearAnimation();
			discountOfferWrapView.setVisibility(View.GONE);
			profile.discountOfferTime = -1;
			profile.update();
			return;
		}

		int hours = timeLeft / (60 * 60);
		int minutes = (timeLeft / 60) % 60;
		int seconds = timeLeft % 60;

		discountOfferTimeView.setText(String.format(Locale.US, "%1$02d:%2$02d:%3$02d", hours, minutes, seconds));
	}
}
