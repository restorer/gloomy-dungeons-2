package zame.game.managers;

import android.content.Context;
import android.content.DialogInterface;
import org.holoeverywhere.app.ProgressDialog;

public class ProgressDialogManager {
	private ProgressDialogManager() {
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
		final SoundManager soundManager = SoundManager.getInstance(false);
		ProgressDialog progressDialog = ProgressDialog.show(context, title, message, indeterminate, cancelable);
		WindowCallbackManager.attachWindowCallback(progressDialog.getWindow(), soundManager, SoundManager.FOCUS_MASK_PROGRESS_DIALOG);

		progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				soundManager.instantPause = false;
				soundManager.onWindowFocusChanged(false, SoundManager.FOCUS_MASK_PROGRESS_DIALOG);
			}
		});

		return progressDialog;
	}
}
