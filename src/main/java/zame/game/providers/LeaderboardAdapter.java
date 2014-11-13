package zame.game.providers;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.store.Achievements;
import zame.game.store.Profile;

public class LeaderboardAdapter extends BaseAdapter {
	public static class ItemViewHolder {
		public TextView positionView;
		public TextView nameView;
		public TextView achievedView;
		public TextView expView;

		public ItemViewHolder(ViewGroup viewGroup) {
			positionView = (TextView)viewGroup.findViewById(R.id.position);
			nameView = (TextView)viewGroup.findViewById(R.id.name);
			achievedView = (TextView)viewGroup.findViewById(R.id.achieved);
			expView = (TextView)viewGroup.findViewById(R.id.exp);
		}
	}

	protected MainActivity activity;
	protected Profile profile;
	protected LayoutInflater layoutInflater;
	protected List<LeaderboardItem> items;
	protected String formatExp;
	protected String formatAchieved;
	protected int colorNormal;
	protected int colorOwner;
	protected int colorError;

	public LeaderboardAdapter(MainActivity activity, Profile profile) {
		this.activity = activity;
		this.profile = profile;
		this.layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		items = MyApplication.self.leaderboard;
		Resources resources = activity.getResources();

		formatExp = resources.getString(R.string.leaderboard_exp);
		formatAchieved = resources.getString(R.string.leaderboard_achieved);

		colorNormal = resources.getColor(R.color.gloomy_leaderboard_normal);
		colorOwner = resources.getColor(R.color.gloomy_leaderboard_owner);
		colorError = resources.getColor(R.color.gloomy_leaderboard_error);
	}

	@Override
	public int getCount() {
		return (items == null ? 0 : items.size());
	}

	@Override
	public Object getItem(int position) {
		return (items == null ? null : items.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup viewGroup;
		ItemViewHolder holder;

		if (convertView != null) {
			viewGroup = (ViewGroup)convertView;
			holder = (ItemViewHolder)viewGroup.getTag();
		} else {
			viewGroup = (ViewGroup)layoutInflater.inflate(R.layout.list_leaderboard, null);
			holder = new ItemViewHolder(viewGroup);
			viewGroup.setTag(holder);
		}

		if (items == null) {
			holder.nameView.setText("");
			holder.achievedView.setText("");
			holder.expView.setText("");
		} else {
			LeaderboardItem item = items.get(position);

			holder.positionView.setText(String.valueOf(position + 1) + ".");
			holder.nameView.setText(item.name);

			if (item.uid.length() == 0) {
				holder.positionView.setTextColor(colorError);
				holder.nameView.setTextColor(colorError);
				holder.achievedView.setText("");
				holder.expView.setText("");
			} else {
				holder.achievedView.setText(String.format(formatAchieved, item.achievementsCount, Achievements.LAST));
				holder.expView.setText(String.format(formatExp, item.exp));

				if (item.uid.equals(profile.playerUid)) {
					holder.positionView.setTextColor(colorOwner);
					holder.nameView.setTextColor(colorOwner);
					holder.achievedView.setTextColor(colorOwner);
					holder.expView.setTextColor(colorOwner);
				} else {
					holder.positionView.setTextColor(colorNormal);
					holder.nameView.setTextColor(colorNormal);
					holder.achievedView.setTextColor(colorNormal);
					holder.expView.setTextColor(colorNormal);
				}
			}
		}

		return viewGroup;
	}
}
