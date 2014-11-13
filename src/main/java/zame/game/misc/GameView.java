package zame.game.misc;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.engine.Engine;
import zame.game.engine.HeroController;

public class GameView extends zame.game.libs.GLSurfaceView21 implements zame.game.libs.GLSurfaceView21.Renderer {
	protected MainActivity activity;
	protected Engine engine;
	protected HeroController heroController;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.activity = (MainActivity)context;
		this.engine = activity.engine;
		this.heroController = engine.heroController;

		setFocusable(true);
		requestFocus();
		setFocusableInTouchMode(true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		engine.init();
		setRenderer(this);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		engine.onSurfaceCreated(gl);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		engine.onSurfaceChanged(gl, width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		engine.onDrawFrame(gl);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Common.canUseKey(keyCode) && heroController.onKeyDown(keyCode)) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (Common.canUseKey(keyCode) && heroController.onKeyUp(keyCode)) {
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		heroController.onTouchEvent(event);

		try {
			Thread.sleep((Build.VERSION.SDK_INT < 8) ? 16 : 1);
		} catch (InterruptedException e) {
		}

		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		heroController.onTrackballEvent(event);
		return true;
	}
}
