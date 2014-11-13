package zame.game.managers;

public class TrackerDummy extends Tracker {
    @Override
	public void sendView(final String appScreen) {
	}

    @Override
	public void sendEvent(final String category, final String action, final String label, final long value) {
	}

    @Override
	public void flushEvents() {
	}
}
