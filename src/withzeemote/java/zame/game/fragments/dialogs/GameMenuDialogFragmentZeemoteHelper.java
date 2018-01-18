package zame.game.fragments.dialogs;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.Engine;
import zame.game.engine.controls.ControlsZeemoteHelper;

public class GameMenuDialogFragmentZeemoteHelper {
    private GameMenuDialogFragmentZeemoteHelper() {
    }

    public static void onCreateDialog(
        ViewGroup viewGroup,
        Engine engine,
        final MainActivity activity,
        final GameMenuDialogFragment gameMenuDialogFragment
    ) {
        if (engine.config.controlScheme == ControlsZeemoteHelper.SCHEME_ZEEMOTE) {
            viewGroup.findViewById(R.id.zeemote).setVisibility(View.VISIBLE);

            ((Button)viewGroup.findViewById(R.id.zeemote)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.tracker.sendEventAndFlush(Common.GA_CATEGORY, "ZeemoteUi", "", 0);
                    gameMenuDialogFragment.dismiss();
                    activity.gameFragment.zeemoteHelper.showZeemoteControllerUi();
                }
            });
        }
    }
}
