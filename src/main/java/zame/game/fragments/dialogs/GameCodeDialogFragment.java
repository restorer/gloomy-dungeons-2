package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.Engine;
import zame.game.managers.SoundManager;

public class GameCodeDialogFragment extends BaseDialogFragment {
	protected MainActivity activity;
	protected Engine engine;

	public static GameCodeDialogFragment newInstance() {
		return new GameCodeDialogFragment();
	}

	public GameCodeDialogFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = (MainActivity)activity;
		this.engine = this.activity.engine;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_game_code, null);

		return new AlertDialog.Builder(activity)
			.setIcon(R.drawable.ic_dialog_alert)
			.setTitle(R.string.dlg_enter_code)
			.setView(viewGroup)
			.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					EditText editText = (EditText)viewGroup.findViewById(R.id.code);
					engine.game.unprocessedGameCode = editText.getText().toString();
					activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "CodeEntered", engine.game.unprocessedGameCode, 0);
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
		return SoundManager.FOCUS_MASK_GAME_CODE_DIALOG;
	}
}
