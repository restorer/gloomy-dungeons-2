package zame.game.store;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.store.products.Product;

public class StoreAdapter extends BaseAdapter {
	public static class ItemViewHolder {
		public ImageView iconView;
		public TextView titleView;
		public TextView descriptionView;
		public TextView priceView;
		public TextView freeCreditsView;

		public ItemViewHolder(ViewGroup viewGroup) {
			iconView = (ImageView)viewGroup.findViewById(R.id.icon);
			titleView = (TextView)viewGroup.findViewById(R.id.title);
			descriptionView = (TextView)viewGroup.findViewById(R.id.description);
			priceView = (TextView)viewGroup.findViewById(R.id.price);
			freeCreditsView = (TextView)viewGroup.findViewById(R.id.free_credits);
		}
	}

	protected MainActivity activity;
	protected Profile profile;
	protected LayoutInflater layoutInflater;
	protected Product[] items;
	protected int colorNormal;
	protected int colorLocked;
	protected int colorPrice;
	protected int colorPurchased;
	protected int colorEarn;
	protected String textLocked;
	protected String textPurchased;
	protected String textPrice;
	protected String textEarn;
	protected String textEarnFree;

	public StoreAdapter(MainActivity activity, Profile profile, int storeCategory) {
		this.activity = activity;
		this.profile = profile;
		this.layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		items = Store.CATEGORIES[storeCategory];
		Resources resources = activity.getResources();

		colorNormal = resources.getColor(R.color.gloomy_store_normal);
		colorLocked = resources.getColor(R.color.gloomy_store_locked);
		colorPrice = resources.getColor(R.color.gloomy_store_price);
		colorPurchased = resources.getColor(R.color.gloomy_store_purchased);
		colorEarn = resources.getColor(R.color.gloomy_store_earn);

		textLocked = resources.getString(R.string.store_item_locked);
		textPurchased = resources.getString(R.string.store_item_purchased);
		textPrice = resources.getString(R.string.store_item_price);
		textEarn = resources.getString(R.string.store_item_earn);
		textEarnFree = resources.getString(R.string.store_item_earn_free);
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public Object getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return items[position].id;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		Product item = items[position];

		int price = item.getPrice(profile);
		boolean isLocked = item.isLocked(profile);

		return (!isLocked && !(price < 0 && item.id >= 0 && profile.isPurchased(item.id)));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup viewGroup;
		ItemViewHolder holder;

		if (convertView != null) {
			viewGroup = (ViewGroup)convertView;
			holder = (ItemViewHolder)viewGroup.getTag();
		} else {
			viewGroup = (ViewGroup)layoutInflater.inflate(R.layout.list_store, null);
			holder = new ItemViewHolder(viewGroup);
			viewGroup.setTag(holder);
		}

		Product item = items[position];
		int price = item.getPrice(profile);
		boolean isLocked = item.isLocked(profile);
		boolean useLockedColod = ((price < 0 && item.id >= 0 && profile.isPurchased(item.id)) || isLocked);

		if (item.iconResourceId != 0) {
			holder.iconView.setImageResource(item.iconResourceId);
			holder.iconView.setVisibility(View.VISIBLE);
		} else {
			holder.iconView.setVisibility(View.GONE);
		}

		if (!useLockedColod) {
			holder.titleView.setTextColor(colorNormal);
			holder.descriptionView.setTextColor(colorNormal);
		}

		holder.titleView.setText(Html.fromHtml(activity.getString(item.titleResourceId).toUpperCase()));
		holder.descriptionView.setText(Html.fromHtml(activity.getString(item.descriptionResourceId)));

		if (useLockedColod) {
			holder.titleView.setTextColor(colorLocked);
			holder.descriptionView.setTextColor(colorLocked);
		}

		if (price == 0) {
			holder.priceView.setText("");
			holder.freeCreditsView.setVisibility(View.GONE);
		} else if (price > 0) {
			if (isLocked) {
				holder.priceView.setText(textLocked);
				holder.priceView.setTextColor(colorLocked);
			} else if (item.id >= 0 && profile.isPurchased(item.id)) {
				holder.priceView.setText(textPurchased);
				holder.priceView.setTextColor(colorPurchased);
			} else {
				holder.priceView.setText(String.format(textPrice, price));
				holder.priceView.setTextColor(colorPrice);
			}

			holder.freeCreditsView.setVisibility(View.GONE);
		} else {
			int freeCredits = Store.getFreeCreditsCount(profile, -price);
			holder.priceView.setText(String.format(textEarn, -price));

			if (freeCredits > 0) {
				holder.freeCreditsView.setText(String.format(textEarnFree, freeCredits));
				holder.freeCreditsView.setVisibility(View.VISIBLE);
			} else {
				holder.freeCreditsView.setVisibility(View.GONE);
			}

			if (item.id >= 0 && profile.isPurchased(item.id)) {
				holder.priceView.setTextColor(colorLocked);
			} else {
				holder.priceView.setTextColor(colorEarn);
			}
		}

		return viewGroup;
	}
}
