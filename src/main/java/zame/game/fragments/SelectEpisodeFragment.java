package zame.game.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.engine.State;
import zame.game.managers.SoundManager;
import zame.game.misc.MapImageGenerator;
import zame.game.store.Profile;
import zame.game.store.ProfileLevel;
import zame.game.store.Store;
import zame.game.store.products.Product;

public class SelectEpisodeFragment extends BaseFragment {
	protected ViewGroup viewGroup;
	protected ImageView[] images = new ImageView[2];
	protected int currentImageIdx;
	protected final Handler handler = new Handler();
	protected Timer switchImagesTimer = null;
	protected TimerTask switchImagesTimerTask = null;
	protected Profile profile;
	protected State state;
	protected MapImageGenerator.MapImageBitmaps mapImageBitmaps = null;
	protected HashMap<Integer,MapImageGenerator.MapPath> mapPathsHash = new HashMap<Integer,MapImageGenerator.MapPath>();
	protected SelectEpisodeFragmentGPlayHelper gPlayHelper = new SelectEpisodeFragmentGPlayHelper();

	protected final Runnable switchImages = new Runnable() {
		public void run() {
			images[currentImageIdx].setVisibility(View.INVISIBLE);
			currentImageIdx = (currentImageIdx + 1) % images.length;
			images[currentImageIdx].setVisibility(View.VISIBLE);
		}
	};

	protected final View.OnClickListener onContinueClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
			activity.continueGame();
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.state = this.activity.engine.state;
		this.profile = MyApplication.self.profile;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_select_episode, container, false);

		images[0] = (ImageView)viewGroup.findViewById(R.id.image_1);
		images[1] = (ImageView)viewGroup.findViewById(R.id.image_2);
		currentImageIdx = 0;

		((Button)viewGroup.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				activity.showFragment(activity.menuFragment);
			}
		});

		((Button)viewGroup.findViewById(R.id.kontinue)).setOnClickListener(onContinueClick);
		images[0].setOnClickListener(onContinueClick);
		images[1].setOnClickListener(onContinueClick);

		gPlayHelper.createFragmentView(viewGroup, activity, state, profile);
		updateImages();

		return viewGroup;
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
		startTask();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopTask();
	}

	public void updateImages() {
		if (mapImageBitmaps == null) {
			mapImageBitmaps = new MapImageGenerator.MapImageBitmaps(getResources());
		}

		ProfileLevel level;

		if (activity.tryAndLoadInstantState()) {
			level = profile.getLevel(state.levelName);

			((TextView)viewGroup.findViewById(R.id.info)).setText(String.format(
				getString(R.string.se_info),
				state.overallMonsters,
				state.overallItems,
				state.overallSecrets,
				Common.getTimeString(state.overallSeconds)
			));
		} else {
			level = profile.getLevel(State.LEVEL_INITIAL);

			((TextView)viewGroup.findViewById(R.id.info)).setText(
				String.format(getString(R.string.se_info),
				0, 0, 0, Common.getTimeString(0)
			));
		}

		String episodeName = (level.storeEpisodeId == -1 ? getString(R.string.pt_tutorial) : getString(R.string.app_name));
		gPlayHelper.updateImages(viewGroup, level);

		if (level.storeEpisodeId >= 0) {
			for (Product product : Store.CATEGORIES[Store.CATEGORY_LEVELS]) {
				if (product.id == level.storeEpisodeId) {
					episodeName = getString(product.titleResourceId);
					break;
				}
			}
		}

		((TextView)viewGroup.findViewById(R.id.episode)).setText(episodeName);
		MapImageGenerator.MapPath mapPath = mapPathsHash.get(level.storeEpisodeId);

		if (mapPath == null) {
			mapPath = MapImageGenerator.generateMapPath(level.storeEpisodeId, level.episodeLevelsCount);
			mapPathsHash.put(level.storeEpisodeId, mapPath);
		}

		images[0].setImageBitmap(MapImageGenerator.generateMapImage(mapPath, level.episodeIndex, mapImageBitmaps));
		images[1].setImageBitmap(MapImageGenerator.generateMapImage(mapPath, level.episodeIndex + 1, mapImageBitmaps));
	}

	protected synchronized void startTask() {
		if (switchImagesTimerTask == null) {
			switchImagesTimerTask = new TimerTask() {
				public void run() {
					handler.post(switchImages);
				}
			};

			if (switchImagesTimer != null) {
				switchImagesTimer.cancel();
			}

			switchImagesTimer = new Timer();
			switchImagesTimer.schedule(switchImagesTimerTask, 250, 250);
		}
	}

	protected synchronized void stopTask() {
		if (switchImagesTimerTask != null) {
			switchImagesTimerTask.cancel();
			switchImagesTimerTask = null;
		}

		if (switchImagesTimer != null) {
			switchImagesTimer.cancel();
			switchImagesTimer = null;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		if (hasWindowFocus) {
			startTask();
		} else {
			stopTask();
		}
	}
}
