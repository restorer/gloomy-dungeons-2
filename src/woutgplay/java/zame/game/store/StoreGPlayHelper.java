package zame.game.store;

import zame.game.R;
import zame.game.store.products.Product;
import zame.game.store.products.ProductEarnDebug;

public class StoreGPlayHelper {
    public static final Product[] CATEGORY_EARN = new Product[] {
        new ProductEarnDebug(
            Store.EARN_DEBUG, -1, -100,
            R.string.pt_earn_debug, R.string.pd_earn_debug,
            R.drawable.ic_earn, 0
        ),
    };
}
