package zame.game;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import org.holoeverywhere.FontLoader;
import org.holoeverywhere.app.Application;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.managers.SoundManager;
import zame.game.managers.Tracker;
import zame.game.misc.GloomyFont;
import zame.game.providers.CachedTexturesProvider;
import zame.game.providers.DlcProvider;
import zame.game.providers.LeaderboardItem;
import zame.game.providers.LoadLeaderboardProvider;
import zame.game.providers.UpdateLatestVersionCodeProvider;
import zame.game.providers.UpdateLeaderboardProvider;
import zame.game.store.Profile;

public class MyApplication extends Application {
	// public static String CACHE_ROOT;
	public static MyApplication self;

	public String SAVES_FOLDER;
	public String SAVES_ROOT;
	public String INTERNAL_ROOT;
	public GloomyFont gloomyFont;

	public final Handler handler = new Handler();
	public Tracker trackerInst = null;
	public SoundManager soundManagerInst = null;
	public Profile profile = new Profile();
	public boolean isLargeDevice = false;
	public UpdateLeaderboardProvider.Task updateLeaderboardTask = null;
	public LoadLeaderboardProvider.Task loadLeaderboardTask = null;
	public CachedTexturesProvider.Task cachedTexturesTask = null;
	public DlcProvider.Task dlcTask = null;
	public ArrayList<LeaderboardItem> leaderboard = null;
	public Typeface cachedTypeface = null;
	public volatile boolean cachedTexturesReady = false;
	public UpdateLatestVersionCodeProvider.Task updateLatestVersionCodeTask = null;
	public MyApplicationZeemoteHelper zeemoteHelper = new MyApplicationZeemoteHelper();
	public MyApplicationGPlayHelper gPlayHelper = new MyApplicationGPlayHelper();

	protected String cachedVersionName = null;
	protected int cachedVersionCode = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		self = this;
		Tracker.getInstance(false); // initialize GA
		isLargeDevice = getResources().getBoolean(R.bool.gloomy_device_large);

		initFonts();
		initPreferences();
		initPaths();

		if (getSharedPreferences().getBoolean("FirstRun", true)) {
			profile.load(false);
			profile.save();

			SharedPreferences.Editor spEditor = getSharedPreferences().edit();
			spEditor.putBoolean("FirstRun", false);
			spEditor.commit();

			Tracker.getInstance(false).sendEventAndFlush(Common.GA_CATEGORY, "Detect", (isLargeDevice ? "LargeDevice" : "NotLargeDevice"), 0);
		} else {
			profile.load();
		}

		zeemoteHelper.onCreate();
	}

	public SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	public LocalBroadcastManager getLocalBroadcastManager() {
		return LocalBroadcastManager.getInstance(getApplicationContext());
	}

	protected String getInternalStoragePath() {
		String result = "";
		final String errorMessage = "Can't open internal storage";

		if (getFilesDir() == null) {
			Common.log("MyApplication.getInternalStoragePath : getFilesDir() == null");
		} else {
			try {
				result = getFilesDir().getCanonicalPath();
			} catch (IOException ex) {
				Common.log(errorMessage, ex);
			}
		}

		if (result.length() == 0) {
			Toast.makeText(this, "Critical error!\n" + errorMessage + ".", Toast.LENGTH_LONG).show();
			throw new RuntimeException(errorMessage);
		}

		return result;
	}

	// protected String getInternalCachePath() {
	//	String result = "";
	//	final String errorMessage = "Can't open internal cache";
	//
	//	if (getCacheDir() == null) {
	//		Common.log("MyApplication.getCachePath : getCacheDir() == null");
	//	} else {
	//		try {
	//			result = getCacheDir().getCanonicalPath();
	//		} catch (IOException ex) {
	//			Common.log(errorMessage, ex);
	//		}
	//	}
	//
	//	if (result.length() == 0) {
	//		Toast.makeText(this, "Critical error!\n" + errorMessage + ".", Toast.LENGTH_LONG).show();
	//		throw new RuntimeException(errorMessage);
	//	}
	//
	//	return result;
	// }

	@SuppressLint("SdCardPath")
	protected String getExternalStoragePath() {
		try {
			if (Environment.getExternalStorageDirectory() == null) {
				// mystical error? return default value
				return "/sdcard";
			} else {
				return Environment.getExternalStorageDirectory().getCanonicalPath();
			}
		} catch (IOException ex) {
			// sdcard missing or mounted. it is not essential for the game, so let's assume it is sdcard
			return "/sdcard";
		}
	}

	protected void initFonts() {
		gloomyFont = new GloomyFont();
		FontLoader.setDefaultFont(gloomyFont);
	}

	protected void initPreferences() {
		SharedPreferences sp = getSharedPreferences();

		if (sp.getInt("ControlsScale", 0) == 0) {
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putInt("ControlsScale", isLargeDevice ? 1 : 2);
			spEditor.commit();
		}

		if (sp.getInt("RotateSpeed", 0) == 0) {
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putInt("RotateSpeed", isLargeDevice ? 8 : 4);
			spEditor.commit();
		}

		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
	}

	protected void initPaths() {
		SAVES_FOLDER = String.format(
			Locale.US,
			"%1$s%2$sAndroid%2$sdata%2$s%3$s",
			getExternalStoragePath(),
			File.separator,
			getPackageName()
		);

		File externalStorageFile = new File(SAVES_FOLDER);

		if (!externalStorageFile.exists()) {
			externalStorageFile.mkdirs();
		}

		String noMediaPath = String.format(Locale.US, "%1$s%2$s.nomedia", SAVES_FOLDER, File.separator);

		if (!(new File(noMediaPath)).exists()) {
			try {
				FileOutputStream out = new FileOutputStream(noMediaPath);
				out.close();
			} catch (Exception ex) {
			}
		}

		SAVES_ROOT = SAVES_FOLDER + File.separator;
		INTERNAL_ROOT = getInternalStoragePath() + File.separator;
		// CACHE_ROOT = getInternalCachePath() + File.separator;
	}

	public String getVersionName() {
		if (cachedVersionName == null) {
			cachedVersionName = "xxxx.xx.xx.xxxx";

			try {
				cachedVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (Exception ex) {
				Common.log(ex);
			}
		}

		return cachedVersionName;
	}

	public int getVersionCode() {
		if (cachedVersionCode == 0) {
			try {
				cachedVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (Exception ex) {
				Common.log(ex);
			}
		}

		return cachedVersionCode;
	}
}
