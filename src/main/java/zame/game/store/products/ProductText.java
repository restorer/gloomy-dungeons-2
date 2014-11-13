package zame.game.store.products;

public class ProductText extends Product {
	public int purchasedTextResourceId;
	public int purchasedImageResourceId;

	public ProductText(
		int id,
		int dependsOnId,
		int price,
		int titleResourceId,
		int descriptionResourceId,
		int iconResourceId,
		int imageResourceId,
		int purchasedTextResourceId,
		int purchasedImageResourceId
	) {
		super(id, dependsOnId, price, titleResourceId, descriptionResourceId, iconResourceId, imageResourceId);
		this.purchasedTextResourceId = purchasedTextResourceId;
		this.purchasedImageResourceId = purchasedImageResourceId;
	}
}
