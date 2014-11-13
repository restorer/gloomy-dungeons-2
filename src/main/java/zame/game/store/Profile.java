package zame.game.store;

import android.content.Intent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.engine.BaseState;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;
import zame.game.managers.Tracker;

public class Profile extends BaseState {
	public static final String BROADCAST_ACTION_UPDATED = "local:Profile.updated";

	protected static final int FIELD_BUILD = 1;
	protected static final int FIELD_CREDITS = 2;
	protected static final int FIELD_PRODUCTS = 3;
	// FIELD_ACHIEVEMENTS = 4
	protected static final int FIELD_EXP = 5;
	protected static final int FIELD_DISCOUNT_OFFER_TIME = 6;
	protected static final int FIELD_ACHIEVED = 7;
	// FIELD_STATS = 8
	protected static final int FIELD_DISCOUNT_OFFER_SUCCESS = 9;
	protected static final int FIELD_ALREADY_COMPLETED_LEVELS = 10;
	protected static final int FIELD_PLAYER_UID = 11;
	protected static final int FIELD_PLAYER_NAME = 12;

	public boolean autoSaveOnUpdate = true;
	public boolean isUnsavedUpdates = false;

	public int credits = 0;
	public int exp = 0;
	public long discountOfferTime = 0;
	public boolean discountOfferSuccess = false;
	public ProfileProduct[] products = new ProfileProduct[Store.LAST];
	public boolean[] achieved = new boolean[Achievements.LAST];
	public HashSet<String> alreadyCompletedLevels = new HashSet<String>();
	public String playerUid = "";
	public String playerName = "";

	public ProfileLevel[] levels = {
		new ProfileLevel("e00m00", -1),

		new ProfileLevel("e01m01", Store.EPISODE_1),
		new ProfileLevel("e01m02", Store.EPISODE_1),
		new ProfileLevel("e01m03", Store.EPISODE_1),
		new ProfileLevel("e01m04", Store.EPISODE_1),
		new ProfileLevel("e01m05", Store.EPISODE_1),
		new ProfileLevel("e01m06", Store.EPISODE_1),
		new ProfileLevel("e01m07", Store.EPISODE_1),
		new ProfileLevel("e01m08", Store.EPISODE_1),
		new ProfileLevel("e01m09", Store.EPISODE_1),
		new ProfileLevel("e01m10", Store.EPISODE_1),

		new ProfileLevel("e02m01", Store.EPISODE_2),
		new ProfileLevel("e02m02", Store.EPISODE_2),
		new ProfileLevel("e02m03", Store.EPISODE_2),
		new ProfileLevel("e02m04", Store.EPISODE_2),
		new ProfileLevel("e02m05", Store.EPISODE_2),
		new ProfileLevel("e02m06", Store.EPISODE_2),
		new ProfileLevel("e02m07", Store.EPISODE_2),
		new ProfileLevel("e02m08", Store.EPISODE_2),
		new ProfileLevel("e02m09", Store.EPISODE_2),
		new ProfileLevel("e02m10", Store.EPISODE_2),

		new ProfileLevel("e03m01", Store.EPISODE_3),
		new ProfileLevel("e03m02", Store.EPISODE_3),
		new ProfileLevel("e03m03", Store.EPISODE_3),
		new ProfileLevel("e03m04", Store.EPISODE_3),
		new ProfileLevel("e03m05", Store.EPISODE_3),
		new ProfileLevel("e03m06", Store.EPISODE_3),
		new ProfileLevel("e03m07", Store.EPISODE_3),
		new ProfileLevel("e03m08", Store.EPISODE_3),
		new ProfileLevel("e03m09", Store.EPISODE_3),
		new ProfileLevel("e03m10", Store.EPISODE_3),

		new ProfileLevel("e04m01", Store.EPISODE_4),
		new ProfileLevel("e04m02", Store.EPISODE_4),
		new ProfileLevel("e04m03", Store.EPISODE_4),
		new ProfileLevel("e04m04", Store.EPISODE_4),
		new ProfileLevel("e04m05", Store.EPISODE_4),
		new ProfileLevel("e04m06", Store.EPISODE_4),
		new ProfileLevel("e04m07", Store.EPISODE_4),
		new ProfileLevel("e04m08", Store.EPISODE_4),
		new ProfileLevel("e04m09", Store.EPISODE_4),
		new ProfileLevel("e04m10", Store.EPISODE_4),

		new ProfileLevel("e05m01", Store.EPISODE_5),
		new ProfileLevel("e05m02", Store.EPISODE_5),
		new ProfileLevel("e05m03", Store.EPISODE_5),
		new ProfileLevel("e05m04", Store.EPISODE_5),
		new ProfileLevel("e05m05", Store.EPISODE_5),
		new ProfileLevel("e05m06", Store.EPISODE_5),
		new ProfileLevel("e05m07", Store.EPISODE_5),
		new ProfileLevel("e05m08", Store.EPISODE_5),
		new ProfileLevel("e05m09", Store.EPISODE_5),
		new ProfileLevel("e05m10", Store.EPISODE_5),

		new ProfileLevel("e99m99", -2),

		new ProfileLevel("e77m01", -3),
		new ProfileLevel("e77m02", -3),
		new ProfileLevel("e77m03", -3),
		new ProfileLevel("e77m04", -3),
		new ProfileLevel("e77m05", -3),
		new ProfileLevel("e77m06", -3),
		new ProfileLevel("e77m07", -3),
		new ProfileLevel("e77m08", -3),
		new ProfileLevel("e77m09", -3),
		new ProfileLevel("e77m10", -3),
	};

	protected HashMap<String,ProfileLevel> levelsMap = new HashMap<String,ProfileLevel>();
	protected ProfileLevel dummyLevel = new ProfileLevel("", -1);
	protected boolean wasChangedOnVersionUpgrade = false;

	public Profile() {
		for (int i = 0, len = products.length; i < len; i++) {
			products[i] = new ProfileProduct();
		}

		HashMap<Integer,Integer> episodeIndices = new HashMap<Integer,Integer>();

		for (int i = 0, len = levels.length; i < len; i++) {
			int episodeIndex = episodeIndices.containsKey(levels[i].storeEpisodeId) ? episodeIndices.get(levels[i].storeEpisodeId) : 0;
			episodeIndices.put(levels[i].storeEpisodeId, episodeIndex + 1);

			levels[i].setProfile(this, i, episodeIndex, (i > 0 ? levels[i - 1] : null), (i < (len - 1) ? levels[i + 1] : null));
			levelsMap.put(levels[i].name, levels[i]);
		}

		for (int i = 0, len = levels.length; i < len; i++) {
			levels[i].episodeLevelsCount = episodeIndices.get(levels[i].storeEpisodeId);
		}

		dummyLevel.setProfile(this, 0, 0, null, null);
		dummyLevel.purchased = true;
	}

	public ProfileLevel getLevel(String name) {
		ProfileLevel level = levelsMap.get(name);
		return (level == null ? dummyLevel : level);
	}

	public boolean isPurchased(int productId) {
		/*
		if (productId == Store.EPISODE_1
			|| productId == Store.EPISODE_2
			|| productId == Store.EPISODE_3
			|| productId == Store.EPISODE_4
			|| productId == Store.EPISODE_5
			|| productId == Store.DIFFICULTY
		) {
			return true;
		}
		*/

		return products[productId]._purchased;
	}

	public void clear() {
		autoSaveOnUpdate = false;
		isUnsavedUpdates = false;

		credits = 0;
		exp = 0;
		discountOfferTime = 0;
		discountOfferSuccess = false;

		for (int i = 0, len = products.length; i < len; i++) {
			products[i]._purchased = false;
			products[i].value = 0;
		}

		for (int i = 0, len = achieved.length; i < len; i++) {
			achieved[i] = false;
		}

		alreadyCompletedLevels.clear();
		playerUid = "";
		playerName = "";

		update();
	}

	@Override
	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_BUILD, MyApplication.self.getVersionName());
		writer.write(FIELD_CREDITS, credits);
		writer.write(FIELD_PRODUCTS, products);
		writer.write(FIELD_EXP, exp);
		writer.write(FIELD_DISCOUNT_OFFER_TIME, discountOfferTime);
		writer.write(FIELD_ACHIEVED, achieved);
		writer.write(FIELD_DISCOUNT_OFFER_SUCCESS, discountOfferSuccess);
		writer.write(FIELD_ALREADY_COMPLETED_LEVELS, alreadyCompletedLevels.toArray(new String[0]));
		writer.write(FIELD_PLAYER_UID, playerUid);
		writer.write(FIELD_PLAYER_NAME, playerName);
	}

	@Override
	public void readFrom(DataReader reader) {
		credits = reader.readInt(FIELD_CREDITS);
		reader.readObjectArray(FIELD_PRODUCTS, products);
		exp = reader.readInt(FIELD_EXP);
		discountOfferTime = reader.readLong(FIELD_DISCOUNT_OFFER_TIME);
		achieved = reader.readBooleanArray(FIELD_ACHIEVED, Achievements.LAST);
		discountOfferSuccess = reader.readBoolean(FIELD_DISCOUNT_OFFER_SUCCESS);
		playerUid = reader.readString(FIELD_PLAYER_UID);
		playerName = reader.readString(FIELD_PLAYER_NAME);

		alreadyCompletedLevels.clear();

		alreadyCompletedLevels.addAll(
			Arrays.asList(
				(String[])Common.defaultize(
					reader.readStringArray(FIELD_ALREADY_COMPLETED_LEVELS),
					new String[0]
				)
			)
		);
	}

	public synchronized void earnCredits(int amount) {
		int freeCredits = Store.getFreeCreditsCount(this, amount);
		credits += amount + freeCredits;

		if (freeCredits > 0) {
			discountOfferSuccess = true;
		}
	}

	public String getAchievementsIdsString() {
		StringBuilder sb = new StringBuilder();
		boolean sep = false;

		for (int i = 0; i < Achievements.LAST; i++) {
			if (achieved[i]) {
				if (sep) {
					sb.append("-");
				}

				sb.append(i + 1);
				sep = true;
			}
		}

		return sb.toString();
	}

	public synchronized void update() {
		update(true);
	}

	public synchronized void update(boolean changed) {
		if (!products[Store.EPISODE_1]._purchased) {
			products[Store.EPISODE_1]._purchased = true;
			changed = true;
		}

		if (achieved[Achievements.QUICKY] && !products[Store.EPISODE_2]._purchased) {
			products[Store.EPISODE_2]._purchased = true;
			changed = true;
		}

		if (achieved[Achievements.P100_SECRETS_ROW_10] && !products[Store.DBL_PISTOL]._purchased) {
			products[Store.DBL_PISTOL]._purchased = true;
			changed = true;
		}

		ProfileLevel.updateProfileLevels(this);

		if (changed) {
			if (autoSaveOnUpdate) {
				save();
			} else {
				isUnsavedUpdates = true;
			}
		}

		MyApplication.self.getLocalBroadcastManager().sendBroadcast(new Intent(BROADCAST_ACTION_UPDATED));
	}

	@Override
	protected int getVersion() {
		return 5;
	}

	@Override
	protected void versionUpgrade(int version) {
	}

	public void load() {
		load(true);
	}

	public void load(boolean showErrorMessage) {
		if (isUnsavedUpdates) {
			save();
		}

		wasChangedOnVersionUpgrade = false;

		if (load(MyApplication.self.INTERNAL_ROOT + "profile.data") != LOAD_RESULT_SUCCESS) {
			int res = load(MyApplication.self.SAVES_ROOT + "profile.data");

			if (res == LOAD_RESULT_SUCCESS) {
				Common.copyFile(MyApplication.self.SAVES_ROOT + "profile.data", MyApplication.self.INTERNAL_ROOT + "profile.data", 0);
			} else if (res == LOAD_RESULT_ERROR && showErrorMessage) {
				Common.showToast(R.string.msg_cant_load_profile);
			}
		}

		update(wasChangedOnVersionUpgrade);
	}

	public void save() {
		autoSaveOnUpdate = true;
		isUnsavedUpdates = false;

		if (!save(MyApplication.self.INTERNAL_ROOT + "profile.data")) {
			Common.showToast(R.string.msg_cant_save_profile);
		} else {
			Common.copyFile(MyApplication.self.INTERNAL_ROOT + "profile.data", MyApplication.self.SAVES_ROOT + "profile.data", 0);
		}
	}
}
