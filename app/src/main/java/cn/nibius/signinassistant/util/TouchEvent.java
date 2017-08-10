package cn.nibius.signinassistant.util;

import android.util.Log;

import com.jaredrummler.android.shell.Shell;

import java.util.Locale;

import cn.nibius.signinassistant.activity.MainActivity;

/**
 * Created by Nibius at 2017/7/11 21:40.
 * This tool can perform tap and swipe event globally.
 * The efficiency of swiping is too slow and looks terrible.
 */

public class TouchEvent {
    private String TAG = "TouchEvent: ";
    private int eventIndex = -1;

    private int track_id = 0x0f000000;

    private int EV_ABS = 0x0003;
    private int EV_KEY = 0x0001;
    private int EV_SYN = 0x0000;

    private int ABS_MT_TRACKING_ID = 0x0039;
    private int BTN_TOUCH = 0x014a;
    private int BTN_TOOL_FINGER = 0x0145;
    private int ABS_MT_POSITION_X = 0x0035;
    private int ABS_MT_POSITION_Y = 0x0036;
    private int SYN_REPORT = 0x0000;
    private int KEY_MENU = 0x008b;
    private int KEY_BACK = 0x009e;

    private int DOWN = 0x00000001;
    private int UP = 0x00000000;

    private int refreshEventIndex() {
        /* get current event index */
        if (this.eventIndex <= 0) {
            this.eventIndex = MainActivity.appConfig.getTapEventIndex();
        }
        return this.eventIndex;
    }

    /* click a point at (x, y) in screen with a delay(ms) */
    public void tapPoint(int px, int py, int delay) {
        refreshEventIndex();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "tapPoint: " + px + " " + py);
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_ABS, ABS_MT_TRACKING_ID, track_id++));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOUCH, DOWN));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOOL_FINGER, DOWN));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_ABS, ABS_MT_POSITION_X, px));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_ABS, ABS_MT_POSITION_Y, py));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_ABS, ABS_MT_TRACKING_ID, 0xffffffff));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOUCH, UP));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOOL_FINGER, UP));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
    }

    /* tap recent button with a delay */
    public void tapRecent(int delay) {
        refreshEventIndex();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "tapRecent: ");
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, KEY_MENU, DOWN));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, KEY_MENU, UP));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
    }

    /* tap back button with a delay */
    public void tapBack(int delay) {
        refreshEventIndex();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "tapBack: ");
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, KEY_BACK, DOWN));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, KEY_BACK, UP));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
    }

    /* swipe a line from (startX, startY) to (endX, endY) whit a delay,
     * efficiency is terrible yet. */
    public void swipeLine(int startX, int startY, int endX, int endY, int delay) {
        refreshEventIndex();
        if (delay > 0)
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        int tarX, tarY;
        Log.i(TAG, "swipeLine: " + startX + " " + startY + " " + endX + " " + endY);
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_ABS, ABS_MT_TRACKING_ID, track_id++));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOUCH, DOWN));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOOL_FINGER, DOWN));
        for (int i = 0; i < 7; i++) {
            tarX = startX + (endX - startX) / 6 * i;
            tarY = startY + (endY - startY) / 6 * i;
            Shell.SU.run(String.format(Locale.getDefault(),
                    "sendevent /dev/input/event%d %d %d %d",
                    eventIndex, EV_ABS, ABS_MT_POSITION_X, tarX));
            Shell.SU.run(String.format(Locale.getDefault(),
                    "sendevent /dev/input/event%d %d %d %d",
                    eventIndex, EV_ABS, ABS_MT_POSITION_Y, tarY));
            Shell.SU.run(String.format(Locale.getDefault(),
                    "sendevent /dev/input/event%d %d %d %d",
                    eventIndex, EV_SYN, SYN_REPORT, 0));
        }
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_ABS, ABS_MT_TRACKING_ID, 0xffffffff));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOUCH, UP));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_KEY, BTN_TOOL_FINGER, UP));
        Shell.SU.run(String.format(Locale.getDefault(),
                "sendevent /dev/input/event%d %d %d %d",
                eventIndex, EV_SYN, SYN_REPORT, 0));
    }
}
