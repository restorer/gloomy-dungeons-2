package zame.game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;
import zame.game.engine.GameMath;
import zame.game.providers.CachedTexturesProvider;

// http://stackoverflow.com/questions/6086040/get-number-of-home-screens-in-android
// http://stackoverflow.com/questions/5208203/android-live-wallpaper-rescaling

public class MyWallpaperService extends GLWallpaperService {
	public class WallpaperEngine extends GLEngine implements Renderer {
		protected zame.game.engine.Engine engine;

		public WallpaperEngine() {
			super();
			// TODO: handle prefs, other initialization

			if (CachedTexturesProvider.needToUpdateCache()) {
				CachedTexturesProvider.updateCache();
			}

			engine = new zame.game.engine.Engine(null);
			engine.game.savedGameParam = (engine.hasInstantSave() ? engine.instantName : "");
			engine.init();

			setRenderer(this);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
		}

		@Override
		public void onResume() {
			super.onResume();

			if (engine != null) {
				engine.onResume();
			}
		}

		@Override
		public void onPause() {
			super.onPause();

			if (engine != null) {
				engine.onPause();
			}
		}

		@Override
		public void onDestroy() {
			super.onDestroy();

			if (engine != null) {
				engine.onPause();
				engine = null;
			}
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			if (engine != null) {
				engine.onSurfaceCreated(gl);
			}
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			if (engine != null) {
				engine.onSurfaceChanged(gl, width, height);
			}
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			if (engine != null) {
				engine.onDrawFrame(gl);
			}
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);

			engine.xOffset = xOffset;
			engine.xOffsetEnabled = (xOffsetStep > GameMath.INFINITY);
		}
	}

	public MyWallpaperService() {
		super();
	}

	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
}
