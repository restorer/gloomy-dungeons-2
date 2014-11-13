package zame.game.managers;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class WindowCallbackManager {
	public static void attachWindowCallback(final Window window, final SoundManager soundManager, final int focusMask) {
		final Window.Callback windowCallback = window.getCallback();

		window.setCallback(new Window.Callback() {
			@Override
			@TargetApi(12)
			public boolean dispatchGenericMotionEvent(MotionEvent event) {
				if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1 && windowCallback != null) {
					return windowCallback.dispatchGenericMotionEvent(event);
				} else {
					return false;
				}
			}

			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
				if (windowCallback != null) {
					return windowCallback.dispatchKeyEvent(event);
				} else {
					return false;
				}
			}

			@Override
			@TargetApi(11)
			public boolean dispatchKeyShortcutEvent(KeyEvent event) {
				if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && windowCallback != null) {
					return windowCallback.dispatchKeyShortcutEvent(event);
				} else {
					return false;
				}
			}

			@Override
			public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
				if (windowCallback != null) {
					return windowCallback.dispatchPopulateAccessibilityEvent(event);
				} else {
					return false;
				}
			}

			@Override
			public boolean dispatchTouchEvent(MotionEvent event) {
				if (windowCallback != null) {
					return windowCallback.dispatchTouchEvent(event);
				} else {
					return false;
				}
			}

			@Override
			public boolean dispatchTrackballEvent(MotionEvent event) {
				if (windowCallback != null) {
					return windowCallback.dispatchTrackballEvent(event);
				} else {
					return false;
				}
			}

			@Override
			@TargetApi(11)
			public void onActionModeFinished(ActionMode mode) {
				if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && windowCallback != null) {
					windowCallback.onActionModeFinished(mode);
				}
			}

			@Override
			@TargetApi(11)
			public void onActionModeStarted(ActionMode mode) {
				if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && windowCallback != null) {
					windowCallback.onActionModeStarted(mode);
				}
			}

			@Override
			public void onAttachedToWindow() {
				if (windowCallback != null) {
					windowCallback.onAttachedToWindow();
				}
			}

			@Override
			public void onContentChanged() {
				if (windowCallback != null) {
					windowCallback.onContentChanged();
				}
			}

			@Override
			public boolean onCreatePanelMenu(int featureId, Menu menu) {
				if (windowCallback != null) {
					return windowCallback.onCreatePanelMenu(featureId, menu);
				} else {
					return false;
				}
			}

			@Override
			public View onCreatePanelView(int featureId) {
				if (windowCallback != null) {
					return windowCallback.onCreatePanelView(featureId);
				} else {
					return null;
				}
			}

			@Override
			public void onDetachedFromWindow() {
				if (windowCallback != null) {
					windowCallback.onDetachedFromWindow();
				}
			}

			@Override
			public boolean onMenuItemSelected(int featureId, MenuItem item) {
				if (windowCallback != null) {
					return windowCallback.onMenuItemSelected(featureId, item);
				} else {
					return false;
				}
			}

			@Override
			public boolean onMenuOpened(int featureId, Menu menu) {
				if (windowCallback != null) {
					return windowCallback.onMenuOpened(featureId, menu);
				} else {
					return false;
				}
			}

			@Override
			public void onPanelClosed(int featureId, Menu menu) {
				if (windowCallback != null) {
					windowCallback.onPanelClosed(featureId, menu);
				}
			}

			@Override
			public boolean onPreparePanel(int featureId, View view, Menu menu) {
				if (windowCallback != null) {
					return windowCallback.onPreparePanel(featureId, view, menu);
				} else {
					return false;
				}
			}

			@Override
			public boolean onSearchRequested() {
				if (windowCallback != null) {
					return windowCallback.onSearchRequested();
				} else {
					return false;
				}
			}

			@Override
			public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
				if (windowCallback != null) {
					windowCallback.onWindowAttributesChanged(attrs);
				}
			}

			@Override
			public void onWindowFocusChanged(boolean hasFocus) {
				soundManager.onWindowFocusChanged(hasFocus, focusMask);

				if (windowCallback != null) {
					windowCallback.onWindowFocusChanged(hasFocus);
				}
			}

			@Override
			@TargetApi(12)
			public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
				if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1 && windowCallback != null) {
					return windowCallback.onWindowStartingActionMode(callback);
				} else {
					return null;
				}
			}
		});
	}
}
