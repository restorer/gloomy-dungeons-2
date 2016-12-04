package zame.game.store;

import zame.game.R;
import zame.game.store.products.Product;
import zame.game.store.products.ProductEarnDebug;
import zame.game.store.products.ProductEarnFacebook;

public class StoreGPlayHelper {
    public static final int EARN_FACEBOOK = 10;

    public static final Product[] CATEGORY_EARN = new Product[] {
        new ProductEarnDebug(
            Store.EARN_DEBUG, -1, -100,
            R.string.pt_earn_debug, R.string.pd_earn_debug,
            R.drawable.ic_earn, 0
        ),
        new ProductEarnFacebook(
            EARN_FACEBOOK, -1, 0,
            R.string.pt_earn_facebook, R.string.pd_earn_facebook,
            R.drawable.ic_facebook, 0
        ),
    };

    private StoreGPlayHelper() {
    }
}
