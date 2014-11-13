package zame.game.managers;

public abstract class Tracker {
    public static synchronized Tracker getInstance(boolean isWallpaper) {
        return new TrackerDummy();
    }

    public abstract void sendView(final String appScreen);
    public abstract void sendEvent(final String category, final String action, final String label, final long value);
    public abstract void flushEvents();

    public void sendEventAndFlush(final String category, final String action, final String label, final long value) {
        sendEvent(category, action, label, value);
        flushEvents();
    }
}
