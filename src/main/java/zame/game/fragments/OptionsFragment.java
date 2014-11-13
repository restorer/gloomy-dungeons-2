package zame.game.fragments;

import android.content.Intent;
import android.os.Bundle;
import java.util.Locale;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.PreferenceFragment;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.fragments.dialogs.DeleteProfileDialogFragment;
import zame.game.fragments.dialogs.RestartWarnDialogFragment;
import zame.game.misc.GeneralWebActivity;

public class OptionsFragment extends PreferenceFragment {
	protected MainActivity activity;
	protected RestartWarnDialogFragment restartWarnDialogFragment;
	protected DeleteProfileDialogFragment deleteProfileDialogFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		restartWarnDialogFragment = RestartWarnDialogFragment.newInstance();
		deleteProfileDialogFragment = DeleteProfileDialogFragment.newInstance();

		findPreference("About").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(activity, GeneralWebActivity.class);
				intent.putExtra(
					GeneralWebActivity.EXTRA_URL,
					Common.getLocalizedAssetPath(activity.getAssets(), "web/about%s.html") + "?ver=" + MyApplication.self.getVersionName()
				);

				activity.startActivity(intent);
				return true;
			}
		});

		findPreference("Help").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String lang = Locale.getDefault().getLanguage().toLowerCase();
				activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Help", String.valueOf(lang), 0);

				Intent intent = new Intent(activity, GeneralWebActivity.class);
				intent.putExtra(GeneralWebActivity.EXTRA_URL, Common.HELP_LINK + lang);
				activity.startActivity(intent);

				return true;
			}
		});

		findPreference("Restart").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				restartWarnDialogFragment.show(getFragmentManager());
				return true;
			}
		});

		findPreference("DeleteProfile").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				deleteProfileDialogFragment.show(getFragmentManager());
				return true;
			}
		});

		findPreference("EnableSound").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				activity.soundManager.setSoundEnabledSetting((Boolean)newValue);
				activity.soundManager.onSettingsUpdated();
				return true;
			}
		});

		findPreference("MusicVolume").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				activity.soundManager.setMusicVolumeSetting((Integer)newValue);
				return true;
			}
		});

		findPreference("EffectsVolume").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				activity.soundManager.setEffectsVolumeSetting((Integer)newValue);
				return true;
			}
		});

		findPreference("RotateScreen").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				((zame.game.libs.FrameLayout)activity.findViewById(R.id.fragment_container)).updateRotateScreen((Boolean)newValue);
				return true;
			}
		});
	}
}
