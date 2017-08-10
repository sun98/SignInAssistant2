package cn.nibius.signinassistant.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaredrummler.android.shell.Shell;

import cn.nibius.signinassistant.activity.MainActivity;
import cn.nibius.signinassistant.R;

/**
 * Created by Nibius at 2017/8/9 13:31.
 * This is a float window service.
 * Runs in background and control the float window's operations.
 */

public class FloatWindowService extends Service implements View.OnTouchListener {
    private static final String TAG = "FLOAT WINDOW SERVICE";
    /* this command aims at opening netease cloud music's MainActivity */
    private static String START_NETEASE_MUSIC = "am start -W com.netease.cloudmusic/com.netease.cloudmusic.activity.MainActivity";

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private View windowView;
    private Button btnShowCPU, btnNeteaseMusic;
    private Thread NeteaseMusicThread;

    /* save the position to solve the drag of the float window */
    private int mStartX, mStartY, mEndX, mEndY;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
        initWindowParams();
        initThread();
        initView();
        windowManager.addView(windowView, windowParams);
        initTouch();
    }

    private void initThread() {
        NeteaseMusicThread = new Thread(() -> {
            Shell.SU.run(START_NETEASE_MUSIC);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* close the float window when on destroy */
        if (windowView != null) {
            windowManager.removeView(windowView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initTouch() {
        btnShowCPU.setOnClickListener(MainActivity.showCPU);
        btnNeteaseMusic.setOnClickListener(view -> NeteaseMusicThread.start());
        btnNeteaseMusic.setOnTouchListener(this);
        btnShowCPU.setOnTouchListener(this);
    }

    private void initView() {
        windowView = LayoutInflater.from(getApplication()).inflate(R.layout.window_float, null);
        btnShowCPU = windowView.findViewById(R.id.button_show_CPU);
        btnNeteaseMusic = windowView.findViewById(R.id.button_netease_music);
    }

    private void initWindowParams() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowParams.gravity = Gravity.START | Gravity.TOP;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    /* if the distance of touch event is too small, pass this event to 'click',
     * otherwise perform as a move event(drag) */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        Log.i(TAG, "initTouch: onTouch");
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = (int) motionEvent.getRawX();
                mStartY = (int) motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mEndX = (int) motionEvent.getRawX();
                mEndY = (int) motionEvent.getRawY();
                if (Math.abs(mStartX - mEndX) > 30 || Math.abs(mStartY - mEndY) > 30) {
                    windowParams.x = (int) motionEvent.getRawX() - windowView.getMeasuredWidth() / 2;
                    windowParams.y = (int) motionEvent.getRawY() - windowView.getMeasuredHeight() / 2;
                    windowManager.updateViewLayout(windowView, windowParams);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(mStartX - mEndX) > 30 || Math.abs(mStartY - mEndY) > 30) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
