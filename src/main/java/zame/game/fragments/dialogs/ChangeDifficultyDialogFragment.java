package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RadioButton;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;
import zame.game.store.Store;

public class ChangeDifficultyDialogFragment extends BaseDialogFragment {
	protected MainActivity activity;
	protected Profile profile;

	public static ChangeDifficultyDialogFragment newInstance() {
		return new ChangeDifficultyDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = (MainActivity)activity;
		this.profile = MyApplication.self.profile;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_change_difficulty, null);
		final RadioButton radioBtnNewbie = (RadioButton)viewGroup.findViewById(R.id.difficulty_newbie);
		final RadioButton radioBtnEasy = (RadioButton)viewGroup.findViewById(R.id.difficulty_easy);
		final RadioButton radioBtnNormal = (RadioButton)viewGroup.findViewById(R.id.difficulty_normal);
		final RadioButton radioBtnHard = (RadioButton)viewGroup.findViewById(R.id.difficulty_hard);
		final RadioButton radioBtnUltimate = (RadioButton)viewGroup.findViewById(R.id.difficulty_ultimate);

		if (profile.products[Store.DIFFICULTY].value == Store.DIFFICULTY_NEWBIE) {
			radioBtnNewbie.setChecked(true);
		} else if (profile.products[Store.DIFFICULTY].value == Store.DIFFICULTY_EASY) {
			radioBtnEasy.setChecked(true);
		} else if (profile.products[Store.DIFFICULTY].value == Store.DIFFICULTY_HARD) {
			radioBtnHard.setChecked(true);
		} else if (profile.products[Store.DIFFICULTY].value == Store.DIFFICULTY_ULTIMATE) {
			radioBtnUltimate.setChecked(true);
		} else {
			radioBtnNormal.setChecked(true);
		}

		return new AlertDialog.Builder(activity)
			.setTitle(R.string.dlg_difficulty_title)
			.setView(viewGroup)
			.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// additional check
					if (profile.isPurchased(Store.DIFFICULTY)) {
						if (radioBtnNewbie.isChecked()) {
							profile.products[Store.DIFFICULTY].value = Store.DIFFICULTY_NEWBIE;
						} else if (radioBtnEasy.isChecked()) {
							profile.products[Store.DIFFICULTY].value = Store.DIFFICULTY_EASY;
						} else if (radioBtnHard.isChecked()) {
							profile.products[Store.DIFFICULTY].value = Store.DIFFICULTY_HARD;
						} else if (radioBtnUltimate.isChecked()) {
							profile.products[Store.DIFFICULTY].value = Store.DIFFICULTY_ULTIMATE;
						} else {
							profile.products[Store.DIFFICULTY].value = Store.DIFFICULTY_NORMAL;
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
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_CHANGE_DIFFICULTY_DIALOG;
	}
}
