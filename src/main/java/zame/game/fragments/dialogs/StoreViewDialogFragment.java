package zame.game.fragments.dialogs;

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
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;
import zame.game.store.Store;
import zame.game.store.products.Product;
import zame.game.store.products.ProductText;

public class StoreViewDialogFragment extends BaseDialogFragment {
	protected static final String KEY_STORE_CATEGORY = "storeCategory";
	protected static final String KEY_POSITION = "position";

	protected MainActivity activity;
	protected Profile profile;

	public static StoreViewDialogFragment newInstance(int storeCategory, int position) {
		StoreViewDialogFragment fragment = new StoreViewDialogFragment();

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
		int storeCategory = getArguments().getInt(KEY_STORE_CATEGORY);
		int position = getArguments().getInt(KEY_POSITION);
		Product item = Store.CATEGORIES[storeCategory][position];

		ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_store_view, null);
		TextView textView = (TextView)viewGroup.findViewById(R.id.text);
		ImageView imageView = (ImageView)viewGroup.findViewById(R.id.image);

		if (item instanceof ProductText) {
			ProductText textItem = (ProductText)item;
			textView.setText(Html.fromHtml(getString(textItem.purchasedTextResourceId)));

			if (textItem.purchasedImageResourceId != 0) {
				imageView.setImageResource(textItem.purchasedImageResourceId);
			} else {
				imageView.setVisibility(View.GONE);
			}
		} else {
			textView.setText(getString(item.descriptionResourceId));

			if (item.imageResourceId != 0) {
				imageView.setImageResource(item.imageResourceId);
			} else {
				imageView.setVisibility(View.GONE);
			}
		}

		return new AlertDialog.Builder(activity)
			.setTitle(item.titleResourceId)
			.setView(viewGroup)
			.setPositiveButton(R.string.dlg_ok, null)
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_STORE_VIEW_DIALOG;
	}
}
