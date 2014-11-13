package zame.game.store.products;

import zame.game.MainActivity;
import zame.game.store.Profile;

public class ProductEarnDebug extends Product {
	public ProductEarnDebug(
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
		earnCredits(profile, false);
	}
}
