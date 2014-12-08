package zame.game.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.holoeverywhere.LayoutInflater;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.store.Profile;
import zame.game.store.Store;
import zame.game.store.products.Product;

public class EodBlockerFragment extends BaseFragment {
	protected ViewGroup viewGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_eod_blocker, container, false);

		((Button)viewGroup.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.menuFragment);
			}
		});

		((Button)viewGroup.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.storeFragment.storeCategory = Store.CATEGORY_LEVELS;
				activity.showFragment(activity.storeFragment);
			}
		});

		setInfoText();
		return viewGroup;
	}

	protected void setInfoText() {
		if (activity.tryAndLoadInstantState()) {
			Profile profile = MyApplication.self.profile;
			Product product = Store.findProductById(profile.getLevel(activity.engine.state.levelName).storeEpisodeId);

			if (product != null && product.isLocked(profile)) {
				((TextView)viewGroup.findViewById(R.id.info)).setText(getString(R.string.eod_info_locked));
				((Button)viewGroup.findViewById(R.id.ok)).setEnabled(false);
				return;
			}
		}

		((TextView)viewGroup.findViewById(R.id.info)).setText(getString(R.string.eod_info));
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
	}
}
