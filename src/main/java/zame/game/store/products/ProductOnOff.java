package zame.game.store.products;

import zame.game.MainActivity;
import zame.game.fragments.dialogs.OnOffStateDialogFragment;
import zame.game.store.Profile;

public class ProductOnOff extends Product {
	public ProductOnOff(
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
	protected void runView(Profile profile, MainActivity activity, int storeCategory, int position) {
		OnOffStateDialogFragment.newInstance(id, titleResourceId).show(activity.getSupportFragmentManager());
	}

	@Override
	public void setPurchased(Profile profile) {
		profile.products[id].value = 1;
		super.setPurchased(profile);
	}
}
