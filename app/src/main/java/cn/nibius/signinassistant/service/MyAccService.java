package cn.nibius.signinassistant.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import cn.nibius.signinassistant.R;
import cn.nibius.signinassistant.activity.MainActivity;

/**
 * Created by Nibius at 2017/7/10 20:03.
 * This Accessibility service only onConnected when the accessibility switch in 设置->无障碍 is on.
 * It listens specific apps'(in res/xml/acc_service_config.xml) operation and do something in onAccessibilityEvent(...).
 * TODO: find the reason why sometimes this service doesn't work and fix it.
 */

public class MyAccService extends AccessibilityService {
    private static String TAG = "AccService";

    private boolean qqSteps[] = {false, false, false},
            neteaseSteps[] = {false, false};

    @Override
    public void onCreate() {
        Log.i(TAG, "service onCreate: ");
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "onServiceConnected: ");
        super.onServiceConnected();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "onKeyEvent: ");
        return super.onKeyEvent(event);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
//        Log.i(TAG, "onAccessibilityEvent: " + accessibilityEvent.getPackageName());
        AccessibilityNodeInfo rootNode, tmpNode;
        List<AccessibilityNodeInfo> nodeList;
        if (accessibilityEvent.getPackageName().toString().endsWith("mobileqq")) {
            /* mobile qq auto sign in operation(not finished) */
//            Log.i(TAG, String.valueOf(accessibilityEvent.getEventType()));
            Log.i(TAG, "onAccessibilityEvent: " + Arrays.toString(qqSteps));
            rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                nodeList = rootNode.findAccessibilityNodeInfosByText(getString(R.string.qq_account_settings));
                if (nodeList.size() > 0 && !qqSteps[0]) {
                    nodeList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    qqSteps[0] = true;
                }
                nodeList = rootNode.findAccessibilityNodeInfosByText(getString(R.string.sign));
                if (nodeList.size() > 0 && nodeList.get(0).getParent() != null)
                    if (!qqSteps[1]) {
                        tmpNode = nodeList.get(0).getParent();
                        tmpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i(TAG, "onAccessibilityEvent: click");
                        qqSteps[1] = true;
                    }
                /* TODO: find the '打卡' button and click it. */
            }
        } else if (accessibilityEvent.getPackageName().toString().endsWith("taobao")) {
            /* taobao auto get gold operation, not tested. */
            Log.i(TAG, String.valueOf(accessibilityEvent.getEventType()));
            rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                nodeList = rootNode.findAccessibilityNodeInfosByText(getString(R.string.taobao_gold_0));
                Log.i(TAG, "onAccessibilityEvent: gold0 = " + nodeList.size());
                if (nodeList.size() > 0) {
                    nodeList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                nodeList = rootNode.findAccessibilityNodeInfosByText(getString(R.string.taobao_gold));
                Log.i(TAG, "onAccessibilityEvent: gold = " + nodeList.size());
                if (nodeList.size() > 0) {
                    nodeList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        } else if (accessibilityEvent.getPackageName().toString().endsWith("cloudmusic")) {
            /* netease cloud music auto sign in operation. */
            Log.i(TAG, String.valueOf(accessibilityEvent.getEventType()));
            rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                if (!neteaseSteps[0]) {
                    MainActivity.touchEvent.tapPoint(10, 10, 0);
                    neteaseSteps[0] = true;
                }
                if (!neteaseSteps[1]) {
                    nodeList = rootNode.findAccessibilityNodeInfosByText(getString(R.string.netease_already_signed));
                    if (nodeList.size() > 0) {
                        Toast.makeText(getApplicationContext(), "网易云：今天已经签到了", Toast.LENGTH_SHORT).show();
                        neteaseSteps[1] = true;
                    }
                }
                if (!neteaseSteps[1]) {
                    nodeList = rootNode.findAccessibilityNodeInfosByText(getString(R.string.netease_sign_in));
                    Log.i(TAG, "onAccessibilityEvent: netease sign_in = " + nodeList.size());
                    if (nodeList.size() > 0) {
                        nodeList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        neteaseSteps[1] = true;
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt: ");
    }
}
