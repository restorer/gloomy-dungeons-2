package zame.game.store.products;

import zame.game.MainActivity;
import zame.game.fragments.dialogs.ChangeDifficultyDialogFragment;
import zame.game.store.Profile;

public class ProductDifficulty extends Product {
	public ProductDifficulty(
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
		ChangeDifficultyDialogFragment.newInstance().show(activity.getSupportFragmentManager());
	}
}
