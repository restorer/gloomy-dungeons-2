package zame.game.store;

import android.util.FloatMath;
import zame.game.R;
import zame.game.store.products.Product;
import zame.game.store.products.ProductDifficulty;
import zame.game.store.products.ProductOnOff;
import zame.game.store.products.ProductText;

public class Store {
	public static final float FREE_CREDITS_MULT = 0.25f;

	public static final int EARN_GPLAY_100 = -1;
	public static final int EARN_GPLAY_250 = -2;
	public static final int EARN_GPLAY_500 = -3;
	public static final int EARN_GPLAY_1000 = -4;
	public static final int EARN_TAPJOY = -5;
	public static final int EARN_DEBUG = -6;
	public static final int EARN_GETJAR = -7;
	public static final int EARN_SPONSORPAY = -8;
	public static final int EARN_IFREE_SMS = -9;
	public static final int EARN_IFREE_PAYPAL = -10;
	public static final int EARN_TSTORE_100 = -11;
	public static final int EARN_TSTORE_250 = -12;
	public static final int EARN_TSTORE_500 = -13;
	public static final int EARN_TSTORE_1000 = -14;
	public static final int PRO_VERSION = -15;

	public static final int EPISODE_1 = 0;
	public static final int EPISODE_2 = 1;
	public static final int EPISODE_3 = 2;
	public static final int EPISODE_4 = 3;
	public static final int EPISODE_5 = 4;
	public static final int DBL_CHAINGUN = 5;
	public static final int ADDITIONAL_HEALTH = 6;
	public static final int DIFFICULTY = 7;
	public static final int SECRETS = 8;
	public static final int CODES = 9;
	// 10
	// 11
	public static final int ALWAYS_OPEN_MAP = 12;
	public static final int DISABLE_ADS = 13;
	public static final int ADDITIONAL_AMMO = 14;
	public static final int ADDITIONAL_ARMOR = 15;
	public static final int INITIAL_ARMOR = 16;
	public static final int DBL_PISTOL = 17;
	public static final int PDBL_SHOTGUN = 18;
	public static final int LAST = 19;

	public static final int DIFFICULTY_NORMAL = 0;
	public static final int DIFFICULTY_EASY = 1;
	public static final int DIFFICULTY_HARD = 2;
	public static final int DIFFICULTY_NEWBIE = 3;
	public static final int DIFFICULTY_ULTIMATE = 4;

	public static final int CATEGORY_LEVELS = 0;
	public static final int CATEGORY_UPGRADE = 1;
	public static final int CATEGORY_ADDITIONAL = 2;
	public static final int CATEGORY_EARN = 3;

	public static Product findProductById(int productId) {
		for (int i = 0, lenI = CATEGORIES.length; i < lenI; i++) {
			Product[] list = CATEGORIES[i];

			for (int j = 0, lenJ = list.length; j < lenJ; j++) {
				if (list[j].id == productId) {
					return list[j];
				}
			}
		}

		return null;
	}

	public static void setPurchased(Profile profile, int productId) {
		Product product = findProductById(productId);

		if (product != null) {
			product.setPurchased(profile);
		}
	}

	public static int getFreeCreditsCount(Profile profile, int amount) {
		if (profile.discountOfferTime > 0) {
			return (int)FloatMath.ceil((float)amount * FREE_CREDITS_MULT);
		}

		return 0;
	}

	public static final Product[][] CATEGORIES = {
		// CATEGORY_LEVELS
		new Product[] {
			new Product(
				EPISODE_1, -1, 50,
				R.string.pt_episode_1, R.string.pd_episode_1,
				0, R.drawable.store_episode_1
			),
			new Product(
				EPISODE_2, EPISODE_1, 50,
				R.string.pt_episode_2, R.string.pd_episode_2,
				0, R.drawable.store_episode_2
			),
			new Product(
				EPISODE_3, EPISODE_2, 50,
				R.string.pt_episode_3, R.string.pd_episode_3,
				0, R.drawable.store_episode_3
			),
			new Product(
				EPISODE_4, EPISODE_3, 50,
				R.string.pt_episode_4, R.string.pd_episode_4,
				0, R.drawable.store_episode_4
			),
			new Product(
				EPISODE_5, EPISODE_4, 50,
				R.string.pt_episode_5, R.string.pd_episode_5,
				0, R.drawable.store_episode_5
			),
		},
		// CATEGORY_UPGRADE
		new Product[] {
			new Product(
				INITIAL_ARMOR, -1, 100,
				R.string.pt_initial_armor, R.string.pd_initial_armor,
				0, R.drawable.store_additional_armor
			),
			new Product(
				ADDITIONAL_HEALTH, -1, 100,
				R.string.pt_additional_health, R.string.pd_additional_health,
				0, R.drawable.store_additional_health
			),
			new Product(
				ADDITIONAL_AMMO, -1, 100,
				R.string.pt_additional_ammo, R.string.pd_additional_ammo,
				0, R.drawable.store_additional_ammo
			),
			new Product(
				ADDITIONAL_AMMO, -1, 100,
				R.string.pt_additional_armor, R.string.pd_additional_armor,
				0, R.drawable.store_additional_armor
			),
			new Product(
				DBL_CHAINGUN, -1, 100,
				R.string.pt_dbl_chaingun, R.string.pd_dbl_chaingun,
				0, R.drawable.store_dbl_chaingun
			),
			new Product(
				DBL_PISTOL, -1, 150,
				R.string.pt_dbl_pistol, R.string.pt_dbl_pistol,
				0, R.drawable.store_dbl_pistol
			),
			new Product(
				PDBL_SHOTGUN, -1, 150,
				R.string.pt_prem_dbl_shotgun, R.string.pt_prem_dbl_shotgun,
				0, R.drawable.store_prem_dbl_shotgun
			),
		},
		// CATEGORY_ADDITIONAL
		new Product[] {
			new ProductText(
				SECRETS, -1, 150,
				R.string.pt_secrets, R.string.pd_secrets,
				0, R.drawable.store_secrets,
				R.string.store_secrets, R.drawable.store_secrets
			),
			new ProductOnOff(
				ALWAYS_OPEN_MAP, -1, 300,
				R.string.pt_always_open_map, R.string.pd_always_open_map,
				0, R.drawable.store_always_open_map
			),
			new ProductText(
				CODES, -1, 300,
				R.string.pt_codes, R.string.pd_codes,
				0, 0,
				R.string.store_codes, 0
			),
			new ProductDifficulty(
				DIFFICULTY, -1, 100,
				R.string.pt_difficulty, R.string.pd_difficulty,
				0, 0
			),
		},
		// CATEGORY_EARN
		StoreGPlayHelper.CATEGORY_EARN,
	};
}
