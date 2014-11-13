package zame.game.libs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import zame.game.MyApplication;
import zame.game.R;

public class FrameLayout extends org.holoeverywhere.widget.FrameLayout {
	protected boolean rotateScreen;

	public FrameLayout(Context context) {
		super(context);
		rotateScreen = getRotateSetting();
	}

	public FrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		rotateScreen = getRotateSetting();
	}

	public FrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		rotateScreen = getRotateSetting();
	}

	protected boolean getRotateSetting() {
		return MyApplication.self.getSharedPreferences().getBoolean("RotateScreen", false);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		if (hasWindowFocus) {
			updateRotateScreen(getRotateSetting());
		}
	}

	public void updateRotateScreen(boolean rotation) {
		rotateScreen = rotation;

		setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(
			getContext(),
			rotateScreen ? R.anim.rotation : R.anim.no_rotation
		), 0));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (rotateScreen) {
			event.setLocation(
				(float)(getWidth() - 1) - event.getX(),
				(float)(getHeight() - 1) - event.getY()
			);
		}

		return super.dispatchTouchEvent(event);
	}
}
