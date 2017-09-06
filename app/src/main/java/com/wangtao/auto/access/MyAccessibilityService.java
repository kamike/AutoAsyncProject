package com.wangtao.auto.access;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Environment;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.wangtao.auto.access.utils.LogUtils;
import com.wangtao.auto.access.utils.UtilsAccess;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedHashSet;


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
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

                break;
        }
        final AccessibilityNodeInfo node = getRootInActiveWindow();

        if (node == null) {
            return;
        }
//        LogUtils.i("添加元素的长度：" + node.getChildCount());

        listNode = new LinkedHashSet<>();

        AddAllToList(node);
        AddAllToList(event.getSource());

        doLog("==========页面添加元素的长度：" + node.getChildCount());
        // performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
//         performGlobalAction(AccessibilityService.GESTURE_SWIPE_LEFT);
//        getServerOnclickXY("540,960");

    }

    private LinkedHashSet<AccessibilityNodeInfo> listNode;

    private void AddAllToList(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo nodeChild = node.getChild(index);
            if (isCheckNode(nodeChild)) {
                listNode.add(nodeChild);
            }
            AddAllToList(nodeChild);
        }
    }

    /**
     * 检查可以点击在屏幕范围内才添加到列表
     *
     * @param nodeChild
     * @return
     */
    private boolean isCheckNode(AccessibilityNodeInfo nodeChild) {
        if (nodeChild == null) {
            return false;
        }
        if (TextUtils.equals(nodeChild.getClassName(), "android.widget.Button")) {
            return true;
        }
        if (!nodeChild.isVisibleToUser()) {
            return false;
        }
        if (nodeChild.isClickable()) {
            return true;
        }


        if (!nodeChild.isClickable()) {
            if (!TextUtils.isEmpty(nodeChild.getText()) || !TextUtils.isEmpty(nodeChild.getContentDescription())) {
                return true;
            }

            return false;
        }
        Rect rect = new Rect();
        nodeChild.getBoundsInScreen(rect);
        if (rect.left <= 0 || rect.top <= 0 || rect.bottom >= MainActivity.SCREEN_Height || rect.right >= MainActivity.SCREEN_Width) {
            return false;
        }

        return true;
    }


    private int OnclickX = 0, OnclickY = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getServerOnclickXY(String positionXY) {
        if (TextUtils.isEmpty(positionXY)) {
            doLog("服务器返回坐标为空");
            return;
        }
        String[] pos = positionXY.split(",");
        if (pos == null || pos.length <= 0) {
            Toast.makeText(this, "服务器发送的坐标错误:" + Arrays.toString(pos), Toast.LENGTH_SHORT).show();
            doLog("服务器发送的坐标错误:" + Arrays.toString(pos));
            return;
        }
        OnclickX = Integer.parseInt(pos[0]);
        OnclickY = Integer.parseInt(pos[1]);
        String serverMessage = null;
        if (pos.length > 2) {
            serverMessage = pos[2];
        }

        if (listNode.isEmpty()) {
            Toast.makeText(this, "未获取到页面数据，请重新打开此页面！", Toast.LENGTH_SHORT).show();
            doLog("未获取到页面数据，请重新打开此页面！");
            return;
        }

        AccessibilityNodeInfo nodeTarget = null;
        float currentSpace = 0;
        for (AccessibilityNodeInfo info : listNode) {
            if (nodeTarget != null) {
                Rect rect = new Rect();
                info.getBoundsInScreen(rect);
                //当前距离
                float space = (float) Math.sqrt(Math.pow(rect.centerX() - OnclickX, 2) + Math.pow(rect.centerY() - OnclickY, 2));
                if (currentSpace > space) {
                    currentSpace = space;
                    nodeTarget = info;
                }
            } else {
                Rect rect1 = new Rect();
                info.getBoundsInScreen(rect1);
                currentSpace = (float) Math.sqrt(Math.pow(rect1.centerX() - OnclickX, 2) + Math.pow(rect1.centerY() - OnclickY, 2));
                nodeTarget = info;
            }
        }
//        Toast.makeText(this, "坐标：" + UtilsAccess.toNodeString(nodeTarget), Toast.LENGTH_SHORT).show();
        doLog("从多少条node里面查找：" + listNode.size());
        //判断是否是微信界面
        Rect rect = UtilsAccess.findRect2String(listNode, "收起键盘");
        if (rect != null) {
            LogUtils.i("收起键盘的坐标：" + rect.toString());
            nodeTarget = getXYTagNode(rect, listNode);
        }

        doLog("点击最近的一个元素：" + UtilsAccess.toNodeString(nodeTarget));
        boolean isClickSuccess = false;
        try {
            isClickSuccess = nodeTarget.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        doLog("是否首次点击成功了：" + isClickSuccess);


        if (!isClickSuccess) {

            String tag = UtilsAccess.getNodeInfoString(nodeTarget);

            UtilsAccess.onclickTagNode(tag, getRootInActiveWindow());
        }
        if (!TextUtils.isEmpty(serverMessage)) {
            UtilsAccess.inputMessage(this, serverMessage, getRootInActiveWindow());
        }

    }

    private AccessibilityNodeInfo getXYTagNode(Rect rect, LinkedHashSet<AccessibilityNodeInfo> listNode) {
        //整个键盘的范围
        Rect all = new Rect();
        all.bottom = ScreenUtils.getScreenHeight();
        all.top = rect.bottom;
        LinkedHashSet<AccessibilityNodeInfo> listNumber = new LinkedHashSet<>();
        AccessibilityNodeInfo tar = null;
        for (AccessibilityNodeInfo node : listNode) {
            if (TextUtils.equals(node.getClassName(), "android.widget.Button")) {
                listNumber.add(node);
                tar = node;
            }

        }
        doLog("获取到密码框有多少个：" + listNumber);

        return tar;
    }


    public static void doLog(String str) {
        LogUtils.i("" + str);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/android_test");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir.getAbsolutePath() + "/log_temp.txt");
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileIOUtils.writeFileFromString(file, str+"\n", true);
    }


    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void AddAllToListSource(LinkedHashSet<AccessibilityNodeInfo> lsit, AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo nodeChild = node.getChild(index);
//            if (isCheckNode(nodeChild)) {

            lsit.add(nodeChild);
//            }
            AddAllToListSource(lsit, nodeChild);
        }
    }
}
