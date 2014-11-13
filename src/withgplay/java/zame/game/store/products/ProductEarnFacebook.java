package zame.game.store.products;

import zame.game.Common;
import zame.game.MainActivity;
import zame.game.managers.Tracker;
import zame.game.store.Profile;

public class ProductEarnFacebook extends Product {
	public ProductEarnFacebook(
		int id,
		int dependsOnId,
		int price,
		int titleResourceId,
		int descriptionResourceId,
		int iconResourceId,
		int imageResourceId
	) {
		super(id, dependsOnId, price, titleResourceId, descriptionResourceId, iconResourceId, imageResourceId);
	}

	@Override
	protected void runEarn(Profile profile, MainActivity activity, int storeCategory, int position) {
		if (!isPurchased(profile) && Common.openBrowser(activity, "http://m.facebook.com/gloomy.dungeons")) {
			Tracker.getInstance(false).sendEventAndFlush(Common.GA_CATEGORY, "EarnStart.Facebook", "", 0);
			setPurchased(profile);
		}
	}
}
