package zame.game.fragments.dialogs;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;
import zame.game.Common;
import zame.game.managers.SoundManager;
import zame.game.managers.WindowCallbackManager;

public abstract class BaseDialogFragment extends DialogFragment {
	protected Window window = null;
	protected SoundManager soundManager = null;
	protected boolean shouldSoundPauseInstantlyOnDismiss = false;

	public DialogFragment.DialogTransaction show(FragmentManager manager) {
		if (soundManager == null) {
			soundManager = SoundManager.getInstance(false);
		}

		soundManager.instantPause = false;

		try {
			return super.show(manager);
		} catch (Exception ex) {
			Common.log(ex);
			return null;
		}
	}

	public BaseDialogFragment() {
		setDialogType(DialogType.AlertDialog);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (soundManager == null) {
			soundManager = SoundManager.getInstance(false);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getDialog() != null) {
			getDialog().setCanceledOnTouchOutside(true);

			if (window != getDialog().getWindow()) {
				window = getDialog().getWindow();
				WindowCallbackManager.attachWindowCallback(window, soundManager, getFocusMask());
			}
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		soundManager.instantPause = shouldSoundPauseInstantlyOnDismiss;
		soundManager.onWindowFocusChanged(false, getFocusMask());
	}

	public abstract int getFocusMask();
}
