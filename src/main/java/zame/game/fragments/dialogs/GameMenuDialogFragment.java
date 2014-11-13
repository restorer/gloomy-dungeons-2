package zame.game.fragments.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.Engine;
import zame.game.engine.Weapons;
import zame.game.managers.SoundManager;

// http://stackoverflow.com/questions/12239886/how-to-migrate-from-gallery-to-horizontalscrollview-viewpager
// https://gist.github.com/8cbe094bb7a783e37ad1

@SuppressWarnings({"deprecation"})
public class GameMenuDialogFragment extends BaseDialogFragment {
	public static class ImageAdapter extends BaseAdapter implements AdapterView.OnItemSelectedListener {
		@SuppressWarnings({"rawtypes", "unchecked"})
		protected static final Pair<Integer, Integer>[] mapping = new Pair[] {
			new Pair<Integer, Integer>(Weapons.WEAPON_HAND, R.drawable.weapon_hand),
			new Pair<Integer, Integer>(Weapons.WEAPON_PISTOL, R.drawable.weapon_pist),
			new Pair<Integer, Integer>(Weapons.WEAPON_SHOTGUN, R.drawable.weapon_shtg),
			new Pair<Integer, Integer>(Weapons.WEAPON_CHAINGUN, R.drawable.weapon_chgn),
			new Pair<Integer, Integer>(Weapons.WEAPON_DBLSHOTGUN, R.drawable.weapon_dblshtg),
			new Pair<Integer, Integer>(Weapons.WEAPON_RLAUNCHER, R.drawable.weapon_rocket),
			new Pair<Integer, Integer>(Weapons.WEAPON_DBLCHAINGUN, R.drawable.weapon_dblchgn),
			new Pair<Integer, Integer>(Weapons.WEAPON_CHAINSAW, R.drawable.weapon_saw),
			new Pair<Integer, Integer>(Weapons.WEAPON_DBLPISTOL, R.drawable.weapon_dblpist),
			new Pair<Integer, Integer>(Weapons.WEAPON_PDBLSHOTGUN, R.drawable.weapon_pdblshtg),
		};

		protected Context context;
		protected android.widget.AbsSpinner gallery;
		protected Weapons weapons;
		protected int sizeInPx;
		protected Drawable normalBackground;
		protected Drawable selectedBackground;
		protected ViewGroup.LayoutParams layoutParams;
		protected ArrayList<Pair<Integer, Drawable>> imagesList;
		protected TextView infoView;

		@SuppressWarnings({"deprecation"})
		public ImageAdapter(Context context, android.widget.AbsSpinner gallery, TextView infoView, Engine engine, int sizeInPx) {
			this.context = context;
			this.gallery = gallery;
			this.infoView = infoView;
			this.weapons = engine.weapons;
			this.sizeInPx = sizeInPx;

			Resources resources = context.getResources();
			imagesList = new ArrayList<Pair<Integer, Drawable>>();

			ColorMatrix colorMatrix = new ColorMatrix();
			colorMatrix.setSaturation(0.0f);
			colorMatrix.setScale(0.25f, 0.25f, 0.25f, 1.0f);

			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

			for (int i = 0; i < mapping.length; i++) {
				int weaponId = mapping[i].first;
				Drawable drawable = resources.getDrawable(mapping[i].second);

				if (weapons.canSwitch(weaponId)) {
					drawable.setColorFilter(null);
				} else {
					drawable.setColorFilter(filter);
				}

				imagesList.add(new Pair<Integer, Drawable>(weaponId, drawable));
			}

			layoutParams = new android.widget.Gallery.LayoutParams(sizeInPx, sizeInPx);
			normalBackground = resources.getDrawable(R.drawable.weapon_normal);
			selectedBackground = resources.getDrawable(R.drawable.weapon_selected);

			gallery.setOnItemSelectedListener(this);
		}

		public void setSelectedWeapon(int weaponId) {
			for (int i = 0, len = imagesList.size(); i < len; i++) {
				if (imagesList.get(i).first == weaponId) {
					gallery.setSelection(i);
					break;
				}
			}
		}

		public int getCount() {
			return imagesList.size();
		}

		public Object getItem(int position) {
			return imagesList.get(position).first;
		}

		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings({"deprecation"})
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;

			if (convertView != null && (convertView instanceof ImageView)) {
				imageView = (ImageView)convertView;
			} else {
				imageView = new ImageView(context);
			}

			imageView.setImageDrawable(imagesList.get(position).second);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setLayoutParams(layoutParams);
			setSelectedState(imageView, false);

			return imageView;
		}

		@SuppressWarnings({"deprecation"})
		public void setSelectedState(ImageView imageView, boolean selected) {
			imageView.setBackgroundDrawable(selected ? selectedBackground : normalBackground);
			imageView.setPadding(0, 0, 0, 0);
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			View selectedView = gallery.getSelectedView();
			int selectedIndex = (Integer)gallery.getSelectedItem();
			boolean canSwitch = weapons.canSwitch(selectedIndex);

			for (int i = 0, len = gallery.getChildCount(); i < len; i++) {
				ImageView imageView = (ImageView)gallery.getChildAt(i);

				if (imageView != null) {
					setSelectedState(imageView, (imageView == selectedView) && canSwitch);
				}
			}

			infoView.setText(Weapons.WEAPONS[selectedIndex].description);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	protected MainActivity activity;
	protected Engine engine;
	protected Weapons weapons;
	protected ViewGroup viewGroup;
	protected android.widget.AbsSpinner gallery;
	protected boolean ignoreDismissHandlerOnce = false;
	protected int minItemSizeInPx;
	protected int maxItemSizeInPx;
	protected int paddingInPx;

	public static GameMenuDialogFragment newInstance() {
		return new GameMenuDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = (MainActivity)activity;
		this.engine = this.activity.engine;
		this.weapons = this.engine.weapons;

		minItemSizeInPx = Common.dpToPx(activity, 80);
		maxItemSizeInPx = Common.dpToPx(activity, 160);
		paddingInPx = Common.dpToPx(activity, (20 + 10) * 2);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	@SuppressWarnings("deprecation")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_game_menu, null);

		int maxGalleryWidth = engine.width - paddingInPx;
		int itemSize = Math.max(minItemSizeInPx, Math.min(maxItemSizeInPx, Math.min(engine.height / 3, maxGalleryWidth / 5)));

		gallery = (android.widget.Gallery)viewGroup.findViewById(R.id.gallery);
		gallery.setMinimumHeight(itemSize);
		gallery.setMinimumWidth(Math.min(maxGalleryWidth, itemSize * 5));

		ImageAdapter adapter = new ImageAdapter(activity, gallery, (TextView)viewGroup.findViewById(R.id.info), engine, itemSize);
		gallery.setAdapter(adapter);
		adapter.setSelectedWeapon(engine.state.heroWeapon);

		gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int weaponId = (Integer)gallery.getItemAtPosition(position);

				if (weapons.canSwitch(weaponId)) {
					if (engine.state.heroWeapon != weaponId) {
						weapons.switchWeapon(weaponId);
						engine.state.autoSelectWeapon = false;
						engine.interracted = true;
					}

					ignoreDismissHandlerOnce = true;
					GameMenuDialogFragment.this.dismiss();
				}
			}
		});

		GameMenuDialogFragmentZeemoteHelper.onCreateDialog(viewGroup, engine, activity, this);

		return new AlertDialog.Builder(activity)
			.setTitle(R.string.dlg_change_weapon)
			.setView(viewGroup)
			.setPositiveButton(R.string.dlg_exit_to_menu, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					GameMenuDialogFragment.this.dismiss();
					engine.gameViewActive = false;
					engine.renderBlackScreen = true;
					activity.showFragment(activity.menuFragment);
				}
			})
			.setNeutralButton(R.string.dlg_game_code, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					GameMenuDialogFragment.this.dismiss();
					activity.gameFragment.showGameCodeDialog();
				}
			})
			.setNegativeButton(R.string.dlg_upgrade, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Upgrade.Menu", engine.state.levelName, 0);
					GameMenuDialogFragment.this.dismiss();
					engine.changeView(Engine.VIEW_TYPE_UPGRADE);
				}
			})
			.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (!ignoreDismissHandlerOnce) {
			int weaponId = (Integer)gallery.getSelectedItem();

			if ((engine.state.heroWeapon != weaponId) && weapons.canSwitch(weaponId)) {
				weapons.switchWeapon(weaponId);
				engine.state.autoSelectWeapon = false;
			}
		} else {
			ignoreDismissHandlerOnce = false;
		}

		super.onDismiss(dialog);
	}

	@Override
	public int getFocusMask() {
		return SoundManager.FOCUS_MASK_GAME_MENU_DIALOG;
	}
}
