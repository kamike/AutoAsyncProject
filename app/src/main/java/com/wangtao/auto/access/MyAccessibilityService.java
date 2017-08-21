package com.wangtao.auto.access;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.wangtao.auto.access.utils.LogUtils;
import com.wangtao.auto.access.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
        final AccessibilityNodeInfo node = getRootInActiveWindow();
        if (node == null) {
            return;
        }
        listNode = new ArrayList<>();
        AddAllToList(node);
        LogUtils.i("添加元素的长度：" + listNode.size());


    }

    ArrayList<AccessibilityNodeInfo> listNode;

    private void AddAllToList(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo nodeChild = node.getChild(index);
            listNode.add(nodeChild);
            AddAllToList(nodeChild);
        }
    }


    private int OnclickX = 540, OnclickY = 960;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getServerOnclickXY(String positionXY) {
        if (TextUtils.isEmpty(positionXY)) {
            LogUtils.i("服务器返回坐标为空");
            return;
        }
        String[] pos = positionXY.split(",");
        if (pos == null || pos.length > 2) {
            Toast.makeText(this, "服务器发送的坐标错误:" + Arrays.toString(pos), Toast.LENGTH_SHORT).show();
            return;
        }
        OnclickX = Integer.parseInt(pos[0]);
        OnclickY = Integer.parseInt(pos[1]);
        if (listNode.isEmpty()) {
            return;
        }
        Collections.sort(listNode, new Comparator<AccessibilityNodeInfo>() {
            @Override
            public int compare(AccessibilityNodeInfo node1, AccessibilityNodeInfo node2) {

                Rect rect1 = new Rect();
                node1.getBoundsInScreen(rect1);
                float space1 = (float) Math.sqrt(Math.pow(rect1.centerX() - OnclickX, 2) + Math.pow(rect1.centerY() - OnclickY, 2));

                Rect rect2 = new Rect();
                node2.getBoundsInScreen(rect2);
                float space2 = (float) Math.sqrt(Math.pow(rect2.centerX() - OnclickX, 2) + Math.pow(rect2.centerY() - OnclickY, 2));

                return (int) (space1 - space2);
            }
        });
        AccessibilityNodeInfo nodeTarget = listNode.get(0);
        doLog("点击第一个元素：" + Utils.toNodeString(nodeTarget));
        try {
            nodeTarget.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        doLog("执行点击==完毕");

    }


    public static void doLog(String str) {
        LogUtils.i("" + str);
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
