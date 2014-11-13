/*
 * The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * http://android.hlidskialf.com/blog/code/android-seekbar-preference
 * http://www.bryandenny.com/index.php/2010/05/25/what-i-learned-from-writing-my-first-android-application/
 *
 * Modified by restorer (added/fixed min parameter, fixed "Cancel" behaviour)
 *
 */

package zame.game.libs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;
import org.holoeverywhere.preference.DialogPreference;
import zame.game.R;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String holons = "http://schemas.android.com/apk/res-auto";

	private Context mContext;
	private SeekBar mSeekBar;
	private TextView mSplashText;
	private TextView mValueText;

	private String mDialogMessage;
	private int mMin;
	private int mMax;
	private int mValue = 0;
	private String mSummary = "%s/%s";

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		mDialogMessage = attrs.getAttributeValue(holons, "dialogMessage");

		TypedArray app = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
		mMin = app.getInt(R.styleable.SeekBarPreference_xmin, 0);

		mMax = attrs.getAttributeIntValue(holons, "max", 100);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);

		mSplashText = new TextView(mContext);

		if (mDialogMessage != null) {
			mSplashText.setText(mDialogMessage);
		}

		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);

		params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		);

		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setProgress(mValue - mMin);
		mValueText.setText(String.valueOf(mValue));

		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setProgress(mValue - mMin);
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

		setSummary(String.format(Locale.US, mSummary, String.valueOf(mValue), String.valueOf(mMax)));
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		value += mMin;

		mValueText.setText(String.valueOf(value));
		callChangeListener(Integer.valueOf(value));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult && (mSeekBar != null)) {
			mValue = mSeekBar.getProgress() + mMin;
			setSummary(String.format(Locale.US, mSummary, String.valueOf(mValue), String.valueOf(mMax)));

			if (shouldPersist()) {
				persistInt(mValue);
			}
		}
	}

	public void onStartTrackingTouch(SeekBar seek) {}
	public void onStopTrackingTouch(SeekBar seek) {}

	public void setMin(int min) {
		mMin = min;
	}

	public int getMin() {
		return mMin;
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		mValue = progress;

		if (mSeekBar != null) {
			mSeekBar.setProgress(progress - mMin);
		}
	}

	public int getProgress() {
		return mValue;
	}
}
