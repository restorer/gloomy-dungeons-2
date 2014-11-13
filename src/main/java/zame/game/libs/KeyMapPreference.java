package zame.game.libs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.DialogPreference;
import zame.game.Common;
import zame.game.R;

public class KeyMapPreference extends DialogPreference {
	private Context mContext;
	private int mValue = 0;

	public KeyMapPreference(Context context) {
		this(context, null);
	}

	public KeyMapPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		setPositiveButtonText(R.string.lib_clear);
		updateSummary();
	}

	@Override
	@SuppressWarnings("deprecation")
	protected View onCreateDialogView() {
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);

		TextView text = new TextView(mContext);
		text.setGravity(Gravity.CENTER_HORIZONTAL);
		text.setTextSize(16);
		text.setText(R.string.lib_press_key);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		);

		layout.addView(text, params);
		return layout;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);

		mValue = (restore ? getPersistedInt(mValue) : ((defaultValue == null) ? mValue : (Integer)defaultValue));

		if (!restore && shouldPersist()) {
			persistInt(mValue);
		}

		updateSummary();
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (Common.canUseKey(keyCode)) {
					mValue = keyCode;
					updateSummary();

					if (shouldPersist()) {
						persistInt(mValue);
					}

					getDialog().dismiss();
					return true;
				}

				return false;
			}
		});
	}

	protected void updateSummary() {
		if (mValue == 0) {
			setSummary(R.string.lib_none);
		} else {
			setSummary(getKeyNameByCode(mValue));
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			mValue = 0;
			updateSummary();

			if (shouldPersist()) {
				persistInt(mValue);
			}
		}
	}

	// I belive that android framework has some better way to do this, but spent almost 2 hours on searching, and didn't find anything
	protected String getKeyNameByCode(int keyCode) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_0: return "0";
			case KeyEvent.KEYCODE_1: return "1";
			case KeyEvent.KEYCODE_2: return "2";
			case KeyEvent.KEYCODE_3: return "3";
			case KeyEvent.KEYCODE_4: return "4";
			case KeyEvent.KEYCODE_5: return "5";
			case KeyEvent.KEYCODE_6: return "6";
			case KeyEvent.KEYCODE_7: return "7";
			case KeyEvent.KEYCODE_8: return "8";
			case KeyEvent.KEYCODE_9: return "9";

			case KeyEvent.KEYCODE_A: return "A";
			case KeyEvent.KEYCODE_B: return "B";
			case KeyEvent.KEYCODE_C: return "C";
			case KeyEvent.KEYCODE_D: return "D";
			case KeyEvent.KEYCODE_E: return "E";
			case KeyEvent.KEYCODE_F: return "F";
			case KeyEvent.KEYCODE_G: return "G";
			case KeyEvent.KEYCODE_H: return "H";
			case KeyEvent.KEYCODE_I: return "I";
			case KeyEvent.KEYCODE_J: return "J";
			case KeyEvent.KEYCODE_K: return "K";
			case KeyEvent.KEYCODE_L: return "L";
			case KeyEvent.KEYCODE_M: return "M";
			case KeyEvent.KEYCODE_N: return "N";
			case KeyEvent.KEYCODE_O: return "O";
			case KeyEvent.KEYCODE_P: return "P";
			case KeyEvent.KEYCODE_Q: return "Q";
			case KeyEvent.KEYCODE_R: return "R";
			case KeyEvent.KEYCODE_S: return "S";
			case KeyEvent.KEYCODE_T: return "T";
			case KeyEvent.KEYCODE_U: return "U";
			case KeyEvent.KEYCODE_V: return "V";
			case KeyEvent.KEYCODE_W: return "W";
			case KeyEvent.KEYCODE_X: return "X";
			case KeyEvent.KEYCODE_Y: return "Y";
			case KeyEvent.KEYCODE_Z: return "Z";

			case KeyEvent.KEYCODE_DPAD_CENTER: return "DPAD_CENTER";
			case KeyEvent.KEYCODE_DPAD_DOWN: return "DPAD_DOWN";
			case KeyEvent.KEYCODE_DPAD_LEFT: return "DPAD_LEFT";
			case KeyEvent.KEYCODE_DPAD_RIGHT: return "DPAD_RIGHT";
			case KeyEvent.KEYCODE_DPAD_UP: return "DPAD_UP";

			case KeyEvent.KEYCODE_ALT_LEFT: return "ALT_LEFT";
			case KeyEvent.KEYCODE_ALT_RIGHT: return "ALT_RIGHT";
			case KeyEvent.KEYCODE_APOSTROPHE: return "APOSTROPHE";
			case KeyEvent.KEYCODE_AT: return "AT";
			case KeyEvent.KEYCODE_BACKSLASH: return "BACKSLASH";
			case KeyEvent.KEYCODE_CALL: return "CALL";
			case KeyEvent.KEYCODE_CAMERA: return "CAMERA";
			case KeyEvent.KEYCODE_CLEAR: return "CLEAR";
			case KeyEvent.KEYCODE_COMMA: return "COMMA";
			case KeyEvent.KEYCODE_DEL: return "DEL";
			case KeyEvent.KEYCODE_ENTER: return "ENTER";
			case KeyEvent.KEYCODE_ENVELOPE: return "ENVELOPE";
			case KeyEvent.KEYCODE_EQUALS: return "EQUALS";
			case KeyEvent.KEYCODE_EXPLORER: return "EXPLORER";
			case KeyEvent.KEYCODE_FOCUS: return "FOCUS";
			case KeyEvent.KEYCODE_GRAVE: return "GRAVE";
			case KeyEvent.KEYCODE_HEADSETHOOK: return "HEADSETHOOK";
			case KeyEvent.KEYCODE_LEFT_BRACKET: return "LEFT_BRACKET";
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: return "MEDIA_FAST_FORWARD";
			case KeyEvent.KEYCODE_MEDIA_NEXT: return "MEDIA_NEXT";
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: return "MEDIA_PLAY_PAUSE";
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS: return "MEDIA_PREVIOUS";
			case KeyEvent.KEYCODE_MEDIA_REWIND: return "MEDIA_REWIND";
			case KeyEvent.KEYCODE_MEDIA_STOP: return "MEDIA_STOP";
			case KeyEvent.KEYCODE_MINUS: return "MINUS";
			case KeyEvent.KEYCODE_MUTE: return "MUTE";
			case KeyEvent.KEYCODE_NOTIFICATION: return "NOTIFICATION";
			case KeyEvent.KEYCODE_NUM: return "NUM";
			case KeyEvent.KEYCODE_PERIOD: return "PERIOD";
			case KeyEvent.KEYCODE_PLUS: return "PLUS";
			case KeyEvent.KEYCODE_POUND: return "POUND";
			case KeyEvent.KEYCODE_POWER: return "POWER";
			case KeyEvent.KEYCODE_RIGHT_BRACKET: return "RIGHT_BRACKET";
			case KeyEvent.KEYCODE_SEARCH: return "SEARCH";
			case KeyEvent.KEYCODE_SEMICOLON: return "SEMICOLON";
			case KeyEvent.KEYCODE_SHIFT_LEFT: return "SHIFT_LEFT";
			case KeyEvent.KEYCODE_SHIFT_RIGHT: return "SHIFT_RIGHT";
			case KeyEvent.KEYCODE_SLASH: return "SLASH";
			case KeyEvent.KEYCODE_SOFT_LEFT: return "SOFT_LEFT";
			case KeyEvent.KEYCODE_SOFT_RIGHT: return "SOFT_RIGHT";
			case KeyEvent.KEYCODE_SPACE: return "SPACE";
			case KeyEvent.KEYCODE_STAR: return "STAR";
			case KeyEvent.KEYCODE_SYM: return "SYM";
			case KeyEvent.KEYCODE_TAB: return "TAB";
			case KeyEvent.KEYCODE_VOLUME_DOWN: return "VOLUME_DOWN";
			case KeyEvent.KEYCODE_VOLUME_UP: return "VOLUME_UP";

			case KeyEvent.KEYCODE_BACK: return "BACK";
			case KeyEvent.KEYCODE_ENDCALL: return "ENDCALL";
			case KeyEvent.KEYCODE_HOME: return "HOME";
			case KeyEvent.KEYCODE_MENU: return "MENU";
		}

		return "<UNKNOWN>";
	}
}
