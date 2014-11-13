package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.Engine;
import zame.game.managers.SoundManager;

// TODO: сделать какой-нибудь setCheckedItemsPosition

public class SaveDialogFragment extends BaseDialogFragment {
	protected MainActivity activity;
	protected Engine engine;
	protected int currentIndex = -1;
	protected ArrayAdapter<String> slotsAdapter;
	protected ArrayList<String> slotStrings = new ArrayList<String>();
	protected ArrayList<String> slotFileNames = new ArrayList<String>();

	public static SaveDialogFragment newInstance() {
		return new SaveDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
		this.engine = this.activity.engine;

		slotsAdapter = new ArrayAdapter<String>(
			getActivity(),
			R.layout.select_dialog_singlechoice_holo,
			slotStrings
		);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.dlg_select_slot_save)
			.setSingleChoiceItems(slotsAdapter, 0, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					currentIndex = which;
				}
			})
			.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					if (currentIndex < 0 || currentIndex >= slotFileNames.size() || !activity.tryAndLoadInstantState()) {
						return;
					}

					engine.state.savedOrNew = true;
					activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Saved", engine.state.levelName, 0);

					if (!engine.state.save(engine.instantName)) {
						return;
					}

					String newSaveName = engine.getSavePathBySaveName(String.format(
						Locale.US,
						"slot-%d.%s",
						currentIndex + 1,
						(new SimpleDateFormat("yyyy-MM-dd-HH-mm")).format(Calendar.getInstance().getTime())
					));

					if (Common.copyFile(engine.getSavePathBySaveName(engine.instantName), newSaveName + ".new", R.string.msg_cant_copy_state)) {
						if (slotFileNames.get(currentIndex).length() != 0) {
							(new File(engine.getSavePathBySaveName(slotFileNames.get(currentIndex)))).delete();
						}

						(new File(newSaveName + ".new")).renameTo(new File(newSaveName));
						Common.showToast(R.string.msg_game_saved);
					}
				}
			})
			.setNegativeButton(R.string.dlg_cancel, null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getDialog() != null) {
			getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		soundManager.setPlaylist(SoundManager.LIST_MAIN);
		Common.fillSlots(getActivity(), slotStrings, slotFileNames, false);
		slotsAdapter.notifyDataSetChanged();

		currentIndex = (
			(getDialog() == null || !(getDialog() instanceof AlertDialog) || ((AlertDialog)getDialog()).getListView() == null) ?
			0 :
			((AlertDialog)getDialog()).getListView().getCheckedItemPosition()
		);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_SAVE_DIALOG;
	}
}
