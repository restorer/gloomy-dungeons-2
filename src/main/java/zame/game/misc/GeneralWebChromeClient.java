package zame.game.misc;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import org.holoeverywhere.widget.ProgressBar;

public class GeneralWebChromeClient extends WebChromeClient {
	private ProgressBar progressBar;
	private boolean isProgressBarVisible;

	public GeneralWebChromeClient(ProgressBar progressBar) {
		super();
		this.progressBar = progressBar;
	}

	@Override
	public void onProgressChanged(WebView view, int progress) {
		if (progress >= 0 && progress < 100) {
			if (!isProgressBarVisible) {
				isProgressBarVisible = true;
				progressBar.setVisibility(View.GONE); // fix bug
				progressBar.setVisibility(View.VISIBLE);
				// progressBar.bringToFront();
			}
		} else {
			if (isProgressBarVisible) {
				isProgressBarVisible = false;
				progressBar.setVisibility(View.GONE);
			}
		}
	}
}
