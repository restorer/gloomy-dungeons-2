package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.managers.SoundManager;

public class QuitWarnDialogFragment extends BaseDialogFragment {
	protected MainActivity activity;

	public static QuitWarnDialogFragment newInstance() {
		return new QuitWarnDialogFragment();
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
			.setTitle(R.string.dlg_quit_game)
			.setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					shouldSoundPauseInstantlyOnDismiss = true;
					activity.quitGame();
				}
			})
			.setNegativeButton(R.string.dlg_no, null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_QUIT_WARN_DIALOG;
	}
}
