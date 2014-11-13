package zame.game.managers;

import zame.game.BuildConfig;
import zame.game.MyApplication;

public abstract class Tracker {
    public static synchronized Tracker getInstance(boolean isWallpaper) {
        if (BuildConfig.DEBUG) {
            return new TrackerDummy();
        }

        if (isWallpaper) {
            return new TrackerDummy();
        }

        if (MyApplication.self.trackerInst == null) {
            MyApplication.self.trackerInst = new TrackerInst();
        }

        return MyApplication.self.trackerInst;
    }

    public abstract void sendView(final String appScreen);
    public abstract void sendEvent(final String category, final String action, final String label, final long value);
    public abstract void flushEvents();

    public void sendEventAndFlush(final String category, final String action, final String label, final long value) {
        sendEvent(category, action, label, value);
        flushEvents();
    }
}
