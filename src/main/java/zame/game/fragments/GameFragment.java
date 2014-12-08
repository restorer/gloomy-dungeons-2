package zame.game.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.holoeverywhere.LayoutInflater;
import zame.game.R;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.Game;
import zame.game.engine.HeroController;
import zame.game.fragments.dialogs.GameCodeDialogFragment;
import zame.game.fragments.dialogs.GameMenuDialogFragment;
import zame.game.managers.SoundManager;
import zame.game.misc.GameView;

public class GameFragment extends BaseFragment implements SensorEventListener {
	public static GameFragment self;

	protected ViewGroup viewGroup;
	protected GameView gameView;
	protected Engine engine;
	protected Game game;
	protected SensorManager sensorManager;
	protected Sensor accelerometer;
	protected int deviceRotation = 0;
	protected GameMenuDialogFragment gameMenuDialogFragment;
	protected GameCodeDialogFragment gameCodeDialogFragment;

	public Config config;
	public HeroController heroController;
	public GameFragmentZeemoteHelper zeemoteHelper = new GameFragmentZeemoteHelper(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.engine = activity.engine;
		this.config = engine.config;
		this.heroController = engine.heroController;
		this.game = engine.game;

		gameMenuDialogFragment = GameMenuDialogFragment.newInstance();
		gameCodeDialogFragment = GameCodeDialogFragment.newInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_game, container, false);
		self = this;

		return viewGroup;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onResume() {
		super.onResume();

		if (gameView != null) {
			viewGroup.removeView(gameView);
		}

		gameView = new GameView(activity);
		viewGroup.addView(gameView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		gameView.setRenderer(gameView);
		engine.init();
		gameView.onResume();

		zeemoteHelper.onResume();

		if (config.accelerometerEnabled) {
			sensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			// documentation says that getOrientation() is deprecated, and we must use getRotation instead()
			// but getRotation() available only for >= 2.2
			// if we look for getRotation() into android sources, we found nice piece of code:
			// public int getRotation() { return getOrientation(); }
			// so it should be safe to use getOrientation() instead of getRotation()
			deviceRotation = activity.getWindowManager().getDefaultDisplay().getOrientation();
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			sensorManager = null;
			accelerometer = null;
		}

		if (game.renderMode == Game.RENDER_MODE_END_LEVEL) {
			activity.soundManager.setPlaylist(SoundManager.LIST_ENDL);
		} else if (game.renderMode == Game.RENDER_MODE_GAME_OVER) {
			activity.soundManager.setPlaylist(SoundManager.LIST_GAMEOVER);
		} else {
			activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		zeemoteHelper.onPause();

		if (gameView != null) {
			gameView.onPause();
			viewGroup.removeView(gameView);
			gameView = null;
		}

		engine.onPause();

		if (config.accelerometerEnabled && (sensorManager != null)) {
			sensorManager.unregisterListener(this);
		}

		engine.tracker.flushEvents();
	}

	@Override
	public void onDestroyView() {
		self = null;
		super.onDestroyView();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		if (hasWindowFocus) {
			engine.onResume();
		} else {
			engine.onPause();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent e) {
		float sensorX;
		float sensorY;

		if (config.accelerometerEnabled && (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
			switch (deviceRotation) {
				case Surface.ROTATION_90:
					sensorX = -e.values[1];
					sensorY = e.values[0];
					break;

				case Surface.ROTATION_180:
					sensorX = -e.values[0];
					sensorY = -e.values[1];
					break;

				case Surface.ROTATION_270:
					sensorX = e.values[1];
					sensorY = -e.values[0];
					break;

				default:
					sensorX = e.values[0];
					sensorY = e.values[1];
					break;
			}

			float accelerometerX = sensorX / SensorManager.GRAVITY_EARTH;
			float accelerometerY = sensorY / SensorManager.GRAVITY_EARTH;

			if (config.rotateScreen) {
				heroController.setAccelerometerValues(-accelerometerX, -accelerometerY);
			} else {
				heroController.setAccelerometerValues(accelerometerX, accelerometerY);
			}
		}
	}

	public void showGameMenu() {
		gameMenuDialogFragment.show(getFragmentManager());
	}

	public void showGameCodeDialog() {
		gameCodeDialogFragment.show(getFragmentManager());
	}

	public void hideDialogs() {
		if (gameCodeDialogFragment.isVisible()) {
			gameCodeDialogFragment.dismiss();
		}

		if (gameMenuDialogFragment.isVisible()) {
			gameMenuDialogFragment.dismiss();
		}
	}
}
