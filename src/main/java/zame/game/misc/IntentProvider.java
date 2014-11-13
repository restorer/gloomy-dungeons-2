package zame.game.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import java.util.List;
import java.util.Locale;
import zame.game.Common;
import zame.game.R;

public class IntentProvider {
	public static Intent getTwitterIntent(Context context, String title, String url) {
		Intent intent = null;
		String text = url + " - " + title;

		final String[] twitterApps = {
			"com.twitter.android",
			"com.twidroid",
			"com.handmark.tweetcaster",
			"com.thedeck.android",
		};

		Intent checkIntent = new Intent();
		checkIntent.setType("text/plain");
		final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(checkIntent, PackageManager.MATCH_DEFAULT_ONLY);

		outer: for (String appPackage : twitterApps) {
			for (ResolveInfo resolveInfo : list) {
				String packageName = resolveInfo.activityInfo.packageName;

				if (packageName != null && packageName.startsWith(appPackage)) {
					checkIntent.setPackage(packageName);
					checkIntent.putExtra(Intent.EXTRA_TEXT, text);
					intent = checkIntent;
					break outer;
				}
			}
		}

		if (intent == null) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
				"https://twitter.com/intent/tweet?text=" + Common.urlEncode(text)
			));
		}

		return intent;
	}

	// https://m.facebook.com/dialog/feed?app_id=458358780877780&link=http://example.com&picture=http://fbrell.com/f8.jpg&name=TestName&caption=TestCaption&description=TestDescription&redirect_uri=https://mighty-lowlands-6381.herokuapp.com/

	// http://stackoverflow.com/questions/4191492/launch-facebook-app-from-other-app
	// facebook://feed
	// fb://feed

	// I'm on map <map_name> - try to beat me! Total

	public static Intent getEndlFacebookIntent(Context context, String link, String name, String caption, String description) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(
			Locale.US,
			"https://m.facebook.com/dialog/feed?app_id=%1$s&link=%2$s&picture=%3$s&name=%4$s&caption=%5$s&description=%6$s&redirect_uri=%7$s",
			"458358780877780",
			Common.urlEncode(link),
			"http://fbrell.com/f8.jpg",
			Common.urlEncode(name),
			Common.urlEncode(caption),
			Common.urlEncode(description),
			"https://mighty-lowlands-6381.herokuapp.com/"
		)));

		return intent;
	}

	public static Intent getFacebookIntent(Context context, String title, String url) {
		Intent intent = null;

		final String[] facebookApps = {
			"com.facebook.katana",
		};

		Intent checkIntent = new Intent();
		checkIntent.setType("text/plain");
		final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(checkIntent, PackageManager.MATCH_DEFAULT_ONLY);

		outer: for (String appPackage : facebookApps) {
			for (ResolveInfo resolveInfo : list) {
				String packageName = resolveInfo.activityInfo.packageName;

				if (packageName != null && packageName.startsWith(appPackage)) {
					checkIntent.setPackage(packageName);
					checkIntent.putExtra(Intent.EXTRA_TEXT, url);
					checkIntent.putExtra(Intent.EXTRA_SUBJECT, title);
					intent = checkIntent;
					break outer;
				}
			}
		}

		if (intent == null) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
				"http://m.facebook.com/sharer.php?u=" + Common.urlEncode(url)
			));
		}

		return intent;
	}

	public static Intent getGooglePlusIntent(Context context, String title, String url) {
		Intent intent = null;

		final String[] plusApps = {
			"com.google.android.apps.plus",
		};

		Intent checkIntent = new Intent(Intent.ACTION_SEND);
		checkIntent.setType("text/plain");
		final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(checkIntent, PackageManager.MATCH_DEFAULT_ONLY);

		outer: for (String appPackage : plusApps) {
			for (ResolveInfo resolveInfo : list) {
				String packageName = resolveInfo.activityInfo.packageName;

				if (packageName != null && packageName.startsWith(appPackage)) {
					checkIntent.setPackage(packageName);
					checkIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + url);
					intent = checkIntent;
					break outer;
				}
			}
		}

		if (intent == null) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
				"https://plus.google.com/share?url=" + Common.urlEncode(url)
			));
		}

		return intent;
	}

	public static Intent getVkIntent(Context context, String title, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
			"http://vk.com/share.php?url=" + Common.urlEncode(url)
		));

		return intent;
	}

	public static Intent getEmailIntent(Context context, String email) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
			.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));

		return Intent.createChooser(intent, context.getString(R.string.send_email_using));
	}

	public static Intent getEmailIntent(Context context, String email, String subject, String body) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
			.putExtra(Intent.EXTRA_SUBJECT, subject)
			.putExtra(Intent.EXTRA_TEXT, body);

		return Intent.createChooser(intent, context.getString(R.string.send_email_using));
	}
}
