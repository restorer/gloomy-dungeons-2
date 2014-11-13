package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import java.util.Locale;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.Common;
import zame.game.CommonGPlayHelper;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;

public class RateGameDialogFragment extends BaseDialogFragment {
	protected static final String KEY_SHOWN_ON_QUIT = "shownOnQuit";

	protected MainActivity activity;

	public static RateGameDialogFragment newInstance(boolean shownOnQuit) {
		RateGameDialogFragment fragment = new RateGameDialogFragment();

		Bundle args = new Bundle();
		args.putBoolean(KEY_SHOWN_ON_QUIT, shownOnQuit);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final boolean shownOnQuit = getArguments().getBoolean(KEY_SHOWN_ON_QUIT);
		final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_rate_game, null);

		viewGroup.findViewById(R.id.like).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				shouldSoundPauseInstantlyOnDismiss = true;

				SharedPreferences.Editor spEditor = MyApplication.self.getSharedPreferences().edit();
				spEditor.putBoolean("RateAtLeastOnce", true);
				spEditor.commit();

				activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Rate", "Like", 0);
				CommonGPlayHelper.openMarket(activity, MyApplication.self.getPackageName());

				if (shownOnQuit) {
					activity.quitGame();
				} else {
					RateGameDialogFragment.this.dismiss();
				}
			}
		});

		viewGroup.findViewById(R.id.dislike).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				shouldSoundPauseInstantlyOnDismiss = true;

				SharedPreferences.Editor spEditor = MyApplication.self.getSharedPreferences().edit();
				spEditor.putBoolean("RateAtLeastOnce", true);
				spEditor.commit();

				activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Rate", "Dislike", 0);
				Common.openBrowser(activity, CommonGPlayHelper.RATE_DISLIKE_LINK + Locale.getDefault().getLanguage().toLowerCase());

				if (shownOnQuit) {
					activity.quitGame();
				} else {
					RateGameDialogFragment.this.dismiss();
				}
			}
		});

		activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Rate", "Show", 0);

		return new AlertDialog.Builder(activity)
			.setIcon(R.drawable.ic_dialog_alert)
			.setTitle(R.string.dlg_rate_title)
			.setView(viewGroup)
			.setPositiveButton(shownOnQuit ? R.string.dlg_rate_quit : R.string.dlg_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					if (shownOnQuit) {
						shouldSoundPauseInstantlyOnDismiss = true;
						activity.quitGame();
					}
				}
			})
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);

		// set QuitWithoutRate on show, because user can hide dialog using "Back" button
		SharedPreferences.Editor spEditor = MyApplication.self.getSharedPreferences().edit();
		spEditor.putBoolean("QuitWithoutRate", true);
		spEditor.commit();
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_RATE_GAME_DIALOG;
	}
}
