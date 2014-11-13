package zame.game.managers;

import android.os.Handler;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionParser;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import zame.game.Common;
import zame.game.MyApplication;

public class TrackerInst extends Tracker {
    private static class EventToTrack {
        public String category;
        public String action;
        public String label;
        public long value;

        public EventToTrack(String category, String action, String label, long value) {
            this.category = category;
            this.action = action;
            this.label = label;
            this.value = value;
        }
    }

    private static class MyExceptionParser implements ExceptionParser {
        public String getDescription(String threadName, Throwable t) {
            try {
                Common.log(t);

                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw, true));
                String description = sw.getBuffer().toString();

                try {
                    description = description.replaceAll(
                        "(at (?:java|android|com\\.android\\.internal|dalvik)\\.[a-zA-Z0-9.$]+\\([a-zA-Z0-9]+\\.java:)[0-9]+(\\))",
                        "$11$2"
                    );
                } catch (Throwable failAtReplacing) {
                    // do nothing
                }

                return description;
            } catch (Throwable fatality) {
                try {
                    return t.toString();
                } catch (Throwable failAtFailing) {
                    return "[Fail at failing]";
                }
            }
        }
    }

    private ArrayList<EventToTrack> eventsToTrack = new ArrayList<EventToTrack>();
    private com.google.analytics.tracking.android.Tracker tracker = null;
    private ExceptionReporter exceptionReporter = null;
    private final Handler handler = new Handler();

    public TrackerInst() {
        try {
            EasyTracker.getInstance().setContext(MyApplication.self);
            tracker = EasyTracker.getTracker();

            // Use custom exception reporter to avoid but in GA v2.0 beta 4
            exceptionReporter = new ExceptionReporter(
                tracker,
                GAServiceManager.getInstance(),
                Thread.getDefaultUncaughtExceptionHandler(),
                MyApplication.self
            );

            exceptionReporter.setExceptionParser(new MyExceptionParser());
            Thread.setDefaultUncaughtExceptionHandler(exceptionReporter);
        } catch (Exception ex) {
            Common.log(ex);
            tracker = null;
        }
    }

    public void sendView(final String appScreen) {
        handler.post(new Runnable() {
            public void run() {
                if (tracker != null) {
                    try {
                        tracker.sendView(appScreen);
                    } catch (Exception ex) {
                        Common.log(ex);
                    }
                }
            }
        });
    }

    public void sendEvent(final String category, final String action, final String label, final long value) {
        handler.post(new Runnable() {
            public void run() {
                if (tracker != null) {
                    eventsToTrack.add(new EventToTrack(category, action, label, value));

                    if (eventsToTrack.size() > 1000) {
                        flushEvents();
                    }
                }
            }
        });
    }

    public void flushEvents() {
        handler.post(new Runnable() {
            public void run() {
                if (tracker != null) {
                    for (EventToTrack ev : eventsToTrack) {
                        try {
                            tracker.sendEvent(ev.category, ev.action, ev.label, ev.value);
                        } catch (Exception ex) {
                            Common.log(ex);
                        }
                    }
                }

                eventsToTrack.clear();
            }
        });
    }
}
