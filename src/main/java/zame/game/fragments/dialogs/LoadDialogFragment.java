package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.managers.SoundManager;

public class LoadDialogFragment extends BaseDialogFragment {
	protected MainActivity activity;
	protected ArrayAdapter<String> slotsAdapter;
	protected ArrayList<String> slotStrings = new ArrayList<String>();
	protected ArrayList<String> slotFileNames = new ArrayList<String>();

	public static LoadDialogFragment newInstance() {
		return new LoadDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;

		slotsAdapter = new ArrayAdapter<String>(
			activity,
			R.layout.select_dialog_item_holo,
			slotStrings
		);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(activity)
			.setTitle(R.string.dlg_select_slot_load)
			.setAdapter(slotsAdapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && which < slotFileNames.size()) {
						String saveName = slotFileNames.get(which);
						boolean instantStateLoaded = activity.tryAndLoadInstantState();

						if (instantStateLoaded && !activity.engine.state.savedOrNew) {
							LoadWarnDialogFragment.newInstance(saveName).show(getFragmentManager());
						} else {
							if (instantStateLoaded) {
								activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Loaded", activity.engine.state.levelName, 0);
							}

							activity.startGame(saveName);
						}
					}
				}
			})
			.setNegativeButton(R.string.dlg_cancel, null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
		Common.fillSlots(activity, slotStrings, slotFileNames, true);
		slotsAdapter.notifyDataSetChanged();
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_LOAD_DIALOG;
	}
}
