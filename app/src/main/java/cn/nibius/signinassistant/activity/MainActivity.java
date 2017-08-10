package cn.nibius.signinassistant.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.nibius.signinassistant.AppConfig;
import cn.nibius.signinassistant.R;
import cn.nibius.signinassistant.service.FloatWindowService;
import cn.nibius.signinassistant.service.MyAccService;
import cn.nibius.signinassistant.util.TouchEvent;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.text_hello)
    TextView textHello;
    @BindView(R.id.button_0)
    Button btn0;
    @BindView(R.id.button_open_cpu)
    Button btnCPU;
    @BindView(R.id.button_qq_sign)
    Button btnQQSign;
    @BindView(R.id.button_mi_market)
    Button btnMiMarket;
    @BindView(R.id.button_show_float_window)
    Button btnShowFloatWindow;

    private static String TAG = "nib";
    private static String DEVICE_CONFIG = "device_config";
    private static String TAP_EVENT_INDEX = "tap_event_index";
    private static String PIXEL_COUNT_X = "pixel_count_x";
    private static String PIXEL_COUNT_Y = "pixel_count_y";
    private static String GET_EVENT = "getevent -p";
    private static String OPEN_ACC1 = "settings put secure enabled_accessibility_services cn.nibius.signinassistant/cn.nibius.signinassistant.service.MyAccService";
    private static String OPEN_ACC2 = "settings put secure accessibility_enabled 1";
    private static String SHOW_CPU = "am startservice -n com.android.systemui/.LoadAverageService";
    private static String HIDE_CPU = "am stopservice -n com.android.systemui/.LoadAverageService";
    private static String START_QQ = "am start -W com.tencent.mobileqq/com.tencent.mobileqq.activity.SplashActivity";
    private static String START_MI_MARKET = "am start -W com.xiaomi.market/com.xiaomi.market.ui.MarketTabActivity";
    private boolean hasRoot = false;        // used to check if the phone has root access, not finished
    private static Handler handler;
    private static Runnable runnable;
    private SharedPreferences deviceConfig; // use SHaredPreferences to save the phone's x&y pixel counts
    private Thread QQSignThread, MiMarketThread;    // threads to process auto sign task, maybe moved to FloatWindowService

    public static View.OnClickListener startFunction, stopFunction, showCPU, hideCPU;
    public static AppConfig appConfig;
    public static TouchEvent touchEvent;    // an instance of TouchEvent which could execute tap and swipe operations globally

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
        checkDeviceConfig();
        initService();
    }

    private void initService() {
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            Log.i(TAG, "initService: disabled");
            /* two shell commands below can use root to open the accessibility service automatically with root */
            Shell.SU.run(OPEN_ACC1);
            Shell.SU.run(OPEN_ACC2);
            Log.i(TAG, "initService: enabled");
        }
        startService(new Intent(MainActivity.this, MyAccService.class));
    }

    private void initialize() {
        appConfig = new AppConfig();
        touchEvent = new TouchEvent();
        handler = new Handler();
        runnable = new Thread() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                touchEvent.swipeLine(200, 1000, 900, 1000, 0);
            }
        };
        startFunction = view -> {
            handler.post(runnable);
            view.setOnClickListener(stopFunction);
        };
        stopFunction = view -> {
            handler.removeCallbacks(runnable);
            view.setOnClickListener(startFunction);
        };
        showCPU = view -> {
            Shell.SU.run(SHOW_CPU);
            view.setOnClickListener(MainActivity.hideCPU);
        };
        hideCPU = view -> {
            Shell.SU.run(HIDE_CPU);
            view.setOnClickListener(MainActivity.showCPU);
        };
        QQSignThread = new Thread(() -> Shell.SU.run(START_QQ));
        MiMarketThread = new Thread(() -> Shell.SU.run(START_MI_MARKET));
        textHello.setText(R.string.string_hello);
        btn0.setOnClickListener(startFunction);
        btnCPU.setOnClickListener(showCPU);
        btnQQSign.setOnClickListener(view -> QQSignThread.start());
        btnMiMarket.setOnClickListener(view -> MiMarketThread.start());
        /* start FloatWindowService, that is, display a float window with some buttons.
         * we should move buttons above to this float window. */
        btnShowFloatWindow.setOnClickListener(view -> startService(new Intent(MainActivity.this, FloatWindowService.class)));
    }

    private void checkDeviceConfig() {
        deviceConfig = getSharedPreferences(DEVICE_CONFIG, MODE_APPEND);
        if (!deviceConfig.contains(TAP_EVENT_INDEX)) {
            CommandResult eventList = Shell.SU.run(GET_EVENT);
            String shellResult = eventList.getStdout();
            int deviceConfig[] = getDeviceConfig(shellResult);
            SharedPreferences.Editor deviceConfigEditor = this.deviceConfig.edit();
            deviceConfigEditor.putInt(TAP_EVENT_INDEX, deviceConfig[0]);
            deviceConfigEditor.putInt(PIXEL_COUNT_X, deviceConfig[1]);
            deviceConfigEditor.putInt(PIXEL_COUNT_Y, deviceConfig[2]);
            deviceConfigEditor.apply();
        }
        appConfig.setTapEventIndex(deviceConfig.getInt(TAP_EVENT_INDEX, -1));
        appConfig.setPixelCountX(deviceConfig.getInt(PIXEL_COUNT_X, -1));
        appConfig.setPixelCountY(deviceConfig.getInt(PIXEL_COUNT_Y, -1));
    }

    /* user regex to get current phone's x&y pixel count.*/
    private int[] getDeviceConfig(String shellResult) {
        String[] devices = shellResult.split("add device");
        String pattern = ".*/dev/input/event(\\d+)[\\s\\S]*0035.*max\\s(\\d+)[\\s\\S]*0036.*max\\s(\\d+)[\\s\\S]*";
        int result[] = new int[]{-2, -2, -2};
        for (String device : devices) {
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(device);
            boolean isMatch = m.find();
            if (isMatch) {
                result[0] = Integer.parseInt(m.group(1));
                result[1] = Integer.parseInt(m.group(2)) + 1;
                result[2] = Integer.parseInt(m.group(3)) + 1;
            }
        }
        return result;
    }

    /* check if the accessibility service is on.*/
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccService.class.getCanonicalName();
//        Log.i(TAG, "service name: " + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.i(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            Log.i(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(MainActivity.this, MyAccService.class));
    }
}
