package zame.game.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import zame.game.MainActivity;

public abstract class BaseFragment extends Fragment {
	public MainActivity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
		this.activity.setupTabs();
	}

	public void onWindowFocusChanged(boolean hasWindowFocus) {
	}

	public void onBackPressed() {
	}
}
