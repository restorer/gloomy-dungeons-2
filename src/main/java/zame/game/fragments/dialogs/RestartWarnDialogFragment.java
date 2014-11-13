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

public class RestartWarnDialogFragment extends BaseDialogFragment {
	protected MainActivity activity;

	public static RestartWarnDialogFragment newInstance() {
		return new RestartWarnDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(activity)
			.setIcon(R.drawable.ic_dialog_alert)
			.setTitle(R.string.dlg_new_game)
			.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "RestartPressed", "", 0);
					activity.engine.deleteInstantSave();
					activity.showFragment(activity.selectEpisodeFragment);
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
		return SoundManager.FOCUS_MASK_RESTART_WARN_DIALOG;
	}
}
