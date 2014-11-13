package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;
import zame.game.store.Store;
import zame.game.store.products.Product;

public class StoreBuyDialogFragment extends BaseDialogFragment {
	protected static final String KEY_STORE_CATEGORY = "storeCategory";
	protected static final String KEY_POSITION = "position";

	protected MainActivity activity;
	protected Profile profile;

	public static StoreBuyDialogFragment newInstance(int storeCategory, int position) {
		StoreBuyDialogFragment fragment = new StoreBuyDialogFragment();

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
		final int price = item.getPrice(profile);
		final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_store_buy, null);

		((TextView)viewGroup.findViewById(R.id.text)).setText(getString(item.descriptionResourceId));

		if (item.imageResourceId != 0) {
			((ImageView)viewGroup.findViewById(R.id.image)).setImageResource(item.imageResourceId);
		} else {
			((ImageView)viewGroup.findViewById(R.id.image)).setVisibility(View.GONE);
		}

		((TextView)viewGroup.findViewById(R.id.price)).setText(String.format(getString(R.string.store_buy_price), price));

		activity.tracker.sendEventAndFlush(
			Common.GA_CATEGORY,
			"Product.View",
			"Id." + (item.id < 10 ? "0" : "") + String.valueOf(item.id),
			0
		);

		return new AlertDialog.Builder(activity)
			.setTitle(String.format(getString(R.string.store_buy_title), getString(item.titleResourceId)))
			.setView(viewGroup)
			.setPositiveButton(Html.fromHtml(getString(R.string.store_buy_accept)), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					if (item.id >= 0 && !item.isLocked(profile) && !profile.isPurchased(item.id)) {
						if (profile.credits >= price) {
							activity.tracker.sendEventAndFlush(
								Common.GA_CATEGORY,
								"Product.Purchase",
								"Id." + (item.id < 10 ? "0" : "") + String.valueOf(item.id),
								0
							);

							profile.credits -= price;
							Store.CATEGORIES[storeCategory][position].setPurchased(profile);
							Store.CATEGORIES[storeCategory][position].run(profile, activity, storeCategory, position);
						} else {
							StoreNoCreditsDialogFragment.newInstance(storeCategory, position).show(getFragmentManager());
						}
					}
				}
			})
			.setNegativeButton(Html.fromHtml(getString(R.string.store_buy_cancel)), null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_STORE_BUY_DIALOG;
	}
}
