package com.wangtao.auto.access;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by wangtao on 2017/8/16.
 */

public class MyAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        doLog("onServiceConnected=======");
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_HAPTIC|AccessibilityServiceInfo.FEEDBACK_VISUAL;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        serviceInfo.packageNames = new String[]{"com.android.settings","com.huawei.android.launcher"};
        serviceInfo.notificationTimeout = 0;
//        setServiceInfo(serviceInfo);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //界面点击
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                //界面文字改动
                break;
        }
        AccessibilityNodeInfo node = event.getSource();

        doLog("getSource:" + node);
//        doLog("getSource:" + event.getPackageName());
        doLog("getRootInActiveWindow:" + getRootInActiveWindow());

        if (node != null) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
//            doLog("得到了矩形:" + rect.left + "," + rect.right + "," + rect.top + "," + rect.bottom);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getServerOnclickXY(String positionXY){
        if(TextUtils.isEmpty(positionXY)){
            LogUtils.i("服务器返回坐标为空");
            return;
        }
        String[] pos=positionXY.split(",");


    }



    public static void doLog(String str) {
        LogUtils.i("=======" + str);
    }


    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
