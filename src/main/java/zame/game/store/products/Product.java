package zame.game.store.products;

import zame.game.Common;
import zame.game.MainActivity;
import zame.game.fragments.dialogs.StoreBuyDialogFragment;
import zame.game.fragments.dialogs.StoreViewDialogFragment;
import zame.game.store.Profile;

public class Product {
	public int id;
	public int titleResourceId;
	public int descriptionResourceId;
	public int iconResourceId;
	public int imageResourceId;
	public boolean locked = false;

	protected int price;
	protected int dependsOnId;

	public Product(
		int id,
		int dependsOnId,
		int price,
		int titleResourceId,
		int descriptionResourceId,
		int iconResourceId,
		int imageResourceId
	) {
		this.id = id;
		this.dependsOnId = dependsOnId;
		this.price = price;
		this.titleResourceId = titleResourceId;
		this.descriptionResourceId = descriptionResourceId;
		this.iconResourceId = iconResourceId;
		this.imageResourceId = imageResourceId;
	}

	public int getPrice(Profile profile) {
		return price;
	}

	public boolean isLocked(Profile profile) {
		return (locked || ((dependsOnId >= 0) && !profile.isPurchased(dependsOnId)));
	}

	public void run(Profile profile, MainActivity activity, int storeCategory, int position) {
		if (isLocked(profile)) {
			return;
		}

		if (getPrice(profile) > 0) {
			if (id < 0) {
				// shouldn't be
				return;
			}

			if (profile.isPurchased(id)) {
				activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "Product.ViewBought", "Id." + (id < 10 ? "0" : "") + String.valueOf(id), 0);
				runView(profile, activity, storeCategory, position);
			} else {
				StoreBuyDialogFragment.newInstance(storeCategory, position).show(activity.getSupportFragmentManager());
			}
		} else {
			if (id >= 0 && profile.isPurchased(id)) {
				return;
			} else {
				runEarn(profile, activity, storeCategory, position);
			}
		}
	}

	protected void runView(Profile profile, MainActivity activity, int storeCategory, int position) {
		StoreViewDialogFragment.newInstance(storeCategory, position).show(activity.getSupportFragmentManager());
	}

	protected void runEarn(Profile profile, MainActivity activity, int storeCategory, int position) {
	}

	public boolean isPurchased(Profile profile) {
		return profile.isPurchased(id);
	}

	public void earnCredits(Profile profile, boolean makePurchased) {
		if (price >= 0) {
			return;
		}

		profile.earnCredits(-price);

		if (makePurchased) {
			setPurchased(profile);
		} else {
			profile.update();
		}
	}

	public void setPurchased(Profile profile) {
		profile.products[id]._purchased = true;
		profile.update();
	}
}
