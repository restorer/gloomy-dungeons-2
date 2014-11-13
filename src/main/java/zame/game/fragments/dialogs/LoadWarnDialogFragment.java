package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.managers.SoundManager;

public class LoadWarnDialogFragment extends BaseDialogFragment {
	public static final String SAVE_NAME = "save_name";

	protected MainActivity activity;
	protected String saveName = "";

	public static LoadWarnDialogFragment newInstance(String saveName) {
		LoadWarnDialogFragment frag = new LoadWarnDialogFragment();

		Bundle args = new Bundle();
		args.putString(SAVE_NAME, saveName);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		saveName = getArguments().getString(SAVE_NAME);

		return new AlertDialog.Builder(activity)
			.setIcon(R.drawable.ic_dialog_alert)
			.setTitle(R.string.dlg_new_game)
			.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					if (saveName != null && activity.tryAndLoadInstantState()) {
						activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Loaded", activity.engine.state.levelName, 0);
					}

					activity.startGame(saveName == null ? "" : saveName);
				}
			})
			.setNegativeButton(R.string.dlg_cancel, null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_LOAD_WARN_DIALOG;
	}
}
