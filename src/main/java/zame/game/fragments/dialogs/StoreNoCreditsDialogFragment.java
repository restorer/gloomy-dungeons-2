package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;
import zame.game.store.Store;
import zame.game.store.products.Product;

public class StoreNoCreditsDialogFragment extends BaseDialogFragment {
	protected static final String KEY_STORE_CATEGORY = "storeCategory";
	protected static final String KEY_POSITION = "position";

	protected MainActivity activity;
	protected Profile profile;

	public static StoreNoCreditsDialogFragment newInstance(int storeCategory, int position) {
		StoreNoCreditsDialogFragment fragment = new StoreNoCreditsDialogFragment();

		Bundle args = new Bundle();
		args.putInt(KEY_STORE_CATEGORY, storeCategory);
		args.putInt(KEY_POSITION, position);
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
		final int storeCategory = getArguments().getInt(KEY_STORE_CATEGORY);
		final int position = getArguments().getInt(KEY_POSITION);
		final Product item = Store.CATEGORIES[storeCategory][position];

		return new AlertDialog.Builder(activity)
			.setTitle(R.string.store_nocred_title)
			.setMessage(Html.fromHtml(String.format(
				getString(R.string.store_nocred_content),
				getString(item.titleResourceId),
				getString(item.descriptionResourceId),
				item.getPrice(profile),
				profile.credits
			)))
			.setPositiveButton(Html.fromHtml(getString(R.string.store_nocred_earn)), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					activity.storeFragment.changeCategory(Store.CATEGORY_EARN);
				}
			})
			.setNegativeButton(Html.fromHtml(getString(R.string.store_nocred_cancel)), null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_STORE_NO_CREDITS_DIALOG;
	}
}
