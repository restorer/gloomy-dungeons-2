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

public class OnOffStateDialogFragment extends BaseDialogFragment {
	protected static final String KEY_STORE_PRODUCT_ID = "storeProductId";
	protected static final String KEY_TITLE_RESOURCE_ID = "titleResourceId";

	protected MainActivity activity;
	protected Profile profile;

	public static OnOffStateDialogFragment newInstance(int storeProductId, int titleResourceId) {
		OnOffStateDialogFragment fragment = new OnOffStateDialogFragment();

		Bundle args = new Bundle();
		args.putInt(KEY_STORE_PRODUCT_ID, storeProductId);
		args.putInt(KEY_TITLE_RESOURCE_ID, titleResourceId);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = (MainActivity)activity;
		this.profile = MyApplication.self.profile;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int storeProductId = getArguments().getInt(KEY_STORE_PRODUCT_ID);
		final int titleResourceId = getArguments().getInt(KEY_TITLE_RESOURCE_ID);

		final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_onoff_state, null);
		final RadioButton radioBtnEnabled = (RadioButton)viewGroup.findViewById(R.id.state_enabled);
		final RadioButton radioBtnDisabled = (RadioButton)viewGroup.findViewById(R.id.state_disabled);

		if (profile.products[storeProductId].value != 0) {
			radioBtnEnabled.setChecked(true);
		} else {
			radioBtnDisabled.setChecked(true);
		}

		return new AlertDialog.Builder(activity)
			.setTitle(titleResourceId)
			.setView(viewGroup)
			.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// additional check
					if (profile.isPurchased(storeProductId)) {
						if (radioBtnEnabled.isChecked()) {
							profile.products[storeProductId].value = 1;
						} else {
							profile.products[storeProductId].value = 0;
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
		return SoundManager.FOCUS_MASK_ONOFF_STATE_DIALOG;
	}
}
