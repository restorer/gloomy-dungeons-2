package zame.game;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zame.game.managers.Tracker;

// http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3_r1/android/os/FileUtils.java#FileUtils

public class Common {
	public static final String TAG = "GloomyDungeons2";
    public static final String GA_CATEGORY = "Stats01";
	public static final int MAX_SLOTS = 4;
	public static final String WEB_LINK = "http://mobile.zame-dev.org/gloomy-ii/index.php?utm_medium=referral&utm_source=ingame&utm_campaign=ingame&hl=";
	public static final String HELP_LINK = "http://mobile.zame-dev.org/gloomy-ii/index.php?action=help&utm_medium=referral&utm_source=ingame&utm_campaign=ingame&hl=";

	public static int dpToPx(Context context, int dp) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static int spToPx(Context context, int sp) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
	}

	/*
	public static InputStream openLocalizedAsset(AssetManager assetManager, String pathTemplate) throws IOException {
		String path = String.format(Locale.US, pathTemplate, "-" + Locale.getDefault().getLanguage().toLowerCase());
		InputStream res;

		try {
			res = assetManager.open(path);
		} catch (IOException ex) {
			path = String.format(Locale.US, pathTemplate, "");
			res = assetManager.open(path);
		}

		return res;
	}
	*/

	public static String getLocalizedAssetPath(AssetManager assetManager, String pathTemplate) {
		String path = String.format(Locale.US, pathTemplate, "-" + Locale.getDefault().getLanguage().toLowerCase());

		try {
			InputStream is = assetManager.open(path);
			is.close();
		} catch (Exception ex) {
			path = String.format(Locale.US, pathTemplate, "");
		}

		return "file:///android_asset/" + path;
	}

	public static boolean openBrowser(Context context, String uri) {
		final String[] appPackageNames = {
			"com.android.chrome",
			"com.android.browser",
		};

		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

			outer: for (String appPackage : appPackageNames) {
				for (ResolveInfo resolveInfo : list) {
					String packageName = resolveInfo.activityInfo.packageName;

					if (packageName != null && packageName.startsWith(appPackage)) {
						intent.setPackage(packageName);
						Tracker.getInstance(false).sendEventAndFlush(GA_CATEGORY, "Detect", "Browser: " + packageName, 0);
						break outer;
					}
				}
			}

			context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
			return true;
		} catch (Exception ex) {
			log(ex);
			showToast("Could not launch the browser application.");
		}

		return false;
	}

	public static boolean openExternalIntent(Context context, Intent intent) {
		return openExternalIntent(context, intent, true);
	}

	public static boolean openExternalIntent(Context context, Intent intent, boolean logExceptions) {
		try {
			context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
			return true;
		} catch (Exception ex) {
			if (logExceptions) {
				log(ex);
			}

			showToast("Could not start external intent.");
		}

		return false;
	}

	public static void safeRename(String tmpName, String fileName) {
		String oldName = fileName + ".old";

		if (new File(oldName).exists()) {
			new File(oldName).delete();
		}

		if (new File(fileName).exists()) {
			new File(fileName).renameTo(new File(oldName));
		}

		new File(tmpName).renameTo(new File(fileName));

		if (new File(oldName).exists()) {
			new File(oldName).delete();
		}
	}

	public static boolean copyFile(String srcFileName, String destFileName, int errorMessageResId) {
		return copyFile(srcFileName, destFileName, errorMessageResId, true);
	}

	public static boolean copyFile(String srcFileName, String destFileName, int errorMessageResId, boolean logExceptions) {
		boolean success = true;
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(srcFileName);
			out = new FileOutputStream(destFileName);

			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception ex) {
			if (logExceptions) {
				log(ex);
			}

			success = false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception ex) {
					if (logExceptions) {
						log(ex);
					}

					success = false;
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (Exception ex) {
					if (logExceptions) {
						log(ex);
					}

					// do not unset success flag,
					// because error while closing InputStream is safe in our case
				}
			}
		}

		if (!success && errorMessageResId != 0) {
			showToast(errorMessageResId);
		}

		return success;
	}

	public static void log(String message) {
		Log.e(TAG, message);
	}

	public static void log(Throwable tr) {
		Log.e(TAG, "Exception: " + tr, tr);
	}

	public static void log(String message, Throwable tr) {
		Log.e(TAG, message, tr);
	}

	public static boolean canUseKey(int keyCode) {
		return (
			(keyCode != KeyEvent.KEYCODE_BACK) &&
			(keyCode != KeyEvent.KEYCODE_HOME) &&
			(keyCode != KeyEvent.KEYCODE_MENU) &&
			(keyCode != KeyEvent.KEYCODE_ENDCALL)
		);
	}

	public static int fillSlots(Context context, ArrayList<String> slotStrings, ArrayList<String> slotFileNames, boolean hideUnused) {
		if (slotStrings != null) {
			slotStrings.clear();
		}

		if (slotFileNames != null) {
			slotFileNames.clear();
		}

		String[] files = new File(MyApplication.self.SAVES_FOLDER).list();

		if (files == null) {
			return 0;
		}

		Pattern pat = Pattern.compile("^slot\\-(\\d)\\.(\\d{4}\\-\\d{2}\\-\\d{2})\\-(\\d{2})\\-(\\d{2})\\.save$");
		SparseArray<Pair<String, String>> saves = new SparseArray<Pair<String, String>>();

		for (int i = 0; i < files.length; i++) {
			Matcher mt = pat.matcher(files[i]);

			if (mt.find()) {
				int slotNum = Integer.valueOf(mt.group(1)) - 1;

				if (slotNum >= 0 && slotNum < MAX_SLOTS) {
					saves.put(Integer.valueOf(slotNum), new Pair<String, String>(
						String.format("Slot %d: %s %s:%s", slotNum + 1, mt.group(2), mt.group(3), mt.group(4)),
						files[i].substring(0, files[i].length() - 5)
					));
				}
			}
		}

		for (int i = 0; i < MAX_SLOTS; i++) {
			Pair<String, String> pair = saves.get(Integer.valueOf(i));

			if (pair != null) {
				if (slotStrings != null) {
					slotStrings.add(pair.first);
				}

				if (slotFileNames != null) {
					slotFileNames.add(pair.second);
				}
			} else if (!hideUnused) {
				if (slotStrings != null) {
					try {
						slotStrings.add(String.format("Slot %d: <%s>", i + 1, context.getString(R.string.val_empty)));
					} catch (Exception ex) {
						slotStrings.add(String.format("Slot %d: <Empty>", i + 1));
					}
				}

				if (slotFileNames != null) {
					slotFileNames.add("");
				}
			}
		}

		return saves.size();
	}

	public static byte[] readBytes(InputStream is) throws IOException {
		byte[] buffer;

		buffer = new byte[is.available()];
		is.read(buffer);
		is.close();

		return buffer;
	}

	public static String getTimeString(int seconds) {
		StringBuilder sb = new StringBuilder();
		int hrs = seconds / 3600;
		int mins = (seconds / 60) % 60;
		int secs = seconds % 60;

		if (hrs > 0) {
			sb.append(hrs);
			sb.append(":");

			if (mins < 10) {
				sb.append("0");
			}

			sb.append(mins);
		} else {
			sb.append(mins);
		}

		sb.append(":");

		if (secs < 10) {
			sb.append("0");
		}

		sb.append(secs);
		return sb.toString();
	}

	public static void appendSpannable(SpannableStringBuilder builder, String text, Object[] styles) {
		final int from = builder.length();
		builder.append(text);
		final int to = builder.length();

		for (Object style : styles) {
			builder.setSpan(style, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	public static String urlEncode(String text) {
		try {
			text = URLEncoder.encode(text, "UTF-8");
		} catch (Exception ex) {
			log(ex);
		}

		return text;
	}

	public static Integer asInteger(Object value) {
		return asInteger(value, 0);
	}

	public static Integer asInteger(Object value, int def) {
		if (value instanceof Number) {
			return Integer.valueOf(((Number)value).intValue());
		} else {
			try {
				return Integer.valueOf(value.toString());
			} catch (NumberFormatException ex) {
				return Integer.valueOf(def);
			}
		}
	}

	public static float asFloat(Object value) {
		return asFloat(value, 0f);
	}

	public static float asFloat(Object value, float def) {
		if (value instanceof Number) {
			return ((Number)value).floatValue();
		} else {
			try {
				return Float.valueOf(value.toString());
			} catch (NumberFormatException ex) {
				return def;
			}
		}
	}

	public static int asInt(Object node, String key) {
		return asInt(node, key, 0);
	}

	public static int asInt(Object node, String key, int def) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			if (value instanceof Number) {
				return ((Number)value).intValue();
			} else {
				try {
					return Integer.valueOf(value.toString());
				} catch (NumberFormatException ex) {
					return def;
				}
			}
		} else {
			return def;
		}
	}

	public static long asLong(Object node, String key) {
		return asLong(node, key, 0);
	}

	public static long asLong(Object node, String key, long def) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			if (value instanceof Number) {
				return ((Number)value).longValue();
			} else {
				try {
					return Long.valueOf(value.toString());
				} catch (NumberFormatException ex) {
					return def;
				}
			}
		} else {
			return def;
		}
	}

	public static float asFloat(Object node, String key) {
		return asFloat(node, key, 0f);
	}

	public static float asFloat(Object node, String key, float def) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			if (value instanceof Number) {
				return ((Number)value).floatValue();
			} else {
				try {
					return Float.valueOf(value.toString());
				} catch (NumberFormatException ex) {
					return def;
				}
			}
		} else {
			return def;
		}
	}

	public static boolean asBoolean(Object node, String key) {
		return asBoolean(node, key, false);
	}

	public static boolean asBoolean(Object node, String key, boolean def) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			if (value instanceof Boolean) {
				return ((Boolean)value).booleanValue();
			} else {
				return Boolean.valueOf(value.toString());
			}
		} else {
			return def;
		}
	}

	public static String asString(Object node, String key) {
		return asString(node, key, "");
	}

	public static String asString(Object node, String key, String def) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			return String.valueOf(value);
		} else {
			return def;
		}
	}

	public static HashMap<?, ?> asMap(Object node, String key) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			if (value instanceof HashMap) {
				return (HashMap<?, ?>)value;
			} else {
				return new HashMap<String, Object>();
			}
		} else {
			return new HashMap<String, Object>();
		}
	}

	public static List<?> asList(Object node) {
		if (node instanceof List<?>) {
			return (List<?>)node;
		} else {
			return new ArrayList<Object>();
		}
	}

	public static List<?> asList(Object node, String key) {
		Object value;

		if ((node instanceof HashMap<?, ?>) && ((value = ((HashMap<?, ?>)node).get(key)) != null)) {
			if (value instanceof List<?>) {
				return (List<?>)value;
			} else {
				return new ArrayList<Object>();
			}
		} else {
			return new ArrayList<Object>();
		}
	}

	public static Object asListItem(Object node, String key, int idx) {
		List<?> list = asList(node, key);
		return (idx >= 0 && idx < list.size() ? list.get(idx) : null);
	}

	public static boolean hasKey(Object node, String key) {
		return ((node instanceof HashMap<?, ?>) && ((HashMap<?, ?>)node).containsKey(key));
	}

	public static Object defaultize(Object obj, Object def) {
		return (obj == null ? def : obj);
	}

	public static Typeface loadTypeface() {
		if (MyApplication.self.cachedTypeface == null) {
			try {
				MyApplication.self.cachedTypeface = Typeface.createFromAsset(
					MyApplication.self.getAssets(),
					"fonts/" + MyApplication.self.getString(R.string.typeface)
				);
			} catch (Exception ex) {
				log(ex);
				MyApplication.self.cachedTypeface = Typeface.DEFAULT;
			}
		}

		return MyApplication.self.cachedTypeface;
	}

	public static boolean isPackageExists(Context context, String targetPackage){
		try {
			context.getPackageManager().getPackageInfo(targetPackage, PackageManager.GET_ACTIVITIES);
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	public static String md5(String s) {
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(s.getBytes("UTF-8"));
			byte[] a = digester.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);

			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			return "";
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	public static void showToast(final String message) {
		try {
			MyApplication.self.handler.post(new Runnable() {
				public void run() {
					try {
						Toast.makeText(MyApplication.self, message, Toast.LENGTH_LONG).show();
					} catch (Throwable innerFatality) {
					}
				}
			});
		} catch (Throwable fatality) {
		}
	}

	public static void showToast(final int resourceId) {
		try {
			MyApplication.self.handler.post(new Runnable() {
				public void run() {
					try {
						Toast.makeText(MyApplication.self, resourceId, Toast.LENGTH_LONG).show();
					} catch (Throwable innerFatality) {
					}
				}
			});
		} catch (Throwable fatality) {
		}
	}

	public static String readToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[8192];
		int read;

		while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
			sb.append(buffer, 0, read);
		}

		return sb.toString();
	}

	public static Bitmap createBitmap(int width, int height, String oomErrorMessage) {
		return createBitmap(width, height, 0, 0, oomErrorMessage);
	}

	public static Bitmap createBitmap(int hqWidth, int hqHeight, int lqWidth, int lqHeight, String oomErrorMessage) {
		Bitmap result = null;

		try {
			result = Bitmap.createBitmap(hqWidth, hqHeight, Bitmap.Config.ARGB_8888);
		} catch (OutOfMemoryError oom) {
			// do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
		}

		if (result == null) {
			try {
				result = Bitmap.createBitmap(hqWidth, hqHeight, Bitmap.Config.ARGB_4444);
			} catch (OutOfMemoryError oom) {
				// do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
			}
		}

		// ok, there is chinese phone / tablet
		if (result == null && lqWidth > 0 && lqHeight > 0) {
			try {
				result = Bitmap.createBitmap(lqWidth, lqHeight, Bitmap.Config.ARGB_8888);
			} catch (OutOfMemoryError oom) {
				// do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
			}

			if (result == null) {
				try {
					result = Bitmap.createBitmap(lqWidth, lqHeight, Bitmap.Config.ARGB_4444);
				} catch (OutOfMemoryError oom) {
					// do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
				}
			}
		}

		// we did everything we could...
		if (result == null) {
			showToast(oomErrorMessage);
			throw new OutOfMemoryError(oomErrorMessage);
		}

		return result;
	}
}
